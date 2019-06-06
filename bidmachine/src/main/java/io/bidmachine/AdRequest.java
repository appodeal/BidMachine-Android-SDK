package io.bidmachine;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import io.bidmachine.core.Logger;
import io.bidmachine.core.NetworkRequest;
import io.bidmachine.displays.PlacementBuilder;
import io.bidmachine.models.AuctionResult;
import io.bidmachine.models.RequestBuilder;
import io.bidmachine.models.RequestParamsRestrictions;
import io.bidmachine.protobuf.Any;
import io.bidmachine.protobuf.InvalidProtocolBufferException;
import io.bidmachine.protobuf.Message;
import io.bidmachine.protobuf.RequestExtension;
import io.bidmachine.protobuf.adcom.Ad;
import io.bidmachine.protobuf.adcom.Context;
import io.bidmachine.protobuf.adcom.Placement;
import io.bidmachine.protobuf.openrtb.Request;
import io.bidmachine.protobuf.openrtb.Response;
import io.bidmachine.utils.BMError;

import static io.bidmachine.Utils.getOrDefault;
import static io.bidmachine.core.Utils.oneOf;

public abstract class AdRequest<SelfType extends AdRequest> implements TrackingObject {

    private static final long DEF_EXPIRATION_TIME = TimeUnit.MINUTES.toSeconds(29);

    private final String trackingId = UUID.randomUUID().toString();

    PriceFloorParams priceFloorParams;
    TargetingParams targetingParams;

    private UserRestrictionParams userRestrictionParams;
    private ExtraParams extraParams;

    @Nullable
    Ad adResult;
    @Nullable
    Response.Seatbid seatBidResult;
    @Nullable
    Response.Seatbid.Bid bidResult;

    @Nullable
    private AuctionResult auctionResult;

    @Nullable
    private ApiRequest<Request, Response> currentApiRequest;
    @Nullable
    private ArrayList<AdRequestListener<SelfType>> adRequestListeners;

    private long expirationTime = -1;

    private boolean isExpired;
    private boolean isExpireTrackerSubscribed;

    private final Runnable expiredRunnable = new Runnable() {
        @Override
        public void run() {
            processExpired();
        }
    };

    protected AdRequest() {
    }

    Object build(android.content.Context context, AdsType adsType) {
        final String sellerId = BidMachineImpl.get().getSellerId();
        if (TextUtils.isEmpty(sellerId)) {
            return BMError.paramError("Seller Id not provided");
        }
        BMError implVerifyError = verifyRequest();
        if (implVerifyError != null) {
            return implVerifyError;
        }

        final BidMachineImpl bidMachine = BidMachineImpl.get();

        final Request.Builder requestBuilder = Request.newBuilder();
        final TargetingParams targetingParams = oneOf(this.targetingParams, bidMachine.getTargetingParams());
        final UserRestrictionParams userRestrictionParams = this.userRestrictionParams != null
                ? this.userRestrictionParams : bidMachine.getUserRestrictionParams();
        final BlockedParams blockedParams = targetingParams.getBlockedParams() != null
                ? targetingParams.getBlockedParams()
                : bidMachine.getTargetingParams().getBlockedParams();
        final RequestParamsRestrictions restrictions = UserRestrictionParams.createRestrictions(userRestrictionParams);

        //PriceFloor params
        final ArrayList<Message.Builder> placements = new ArrayList<>();
        adsType.collectDisplayPlacements(context, this, placements);

        final PriceFloorParams priceFloorParams = this.priceFloorParams != null
                ? this.priceFloorParams : bidMachine.getPriceFloorParams();
        final Map<String, Double> priceFloorsMap = priceFloorParams.getPriceFloors() == null
                || priceFloorParams.getPriceFloors().size() == 0
                ? bidMachine.getPriceFloorParams().getPriceFloors() : priceFloorParams.getPriceFloors();

        if (priceFloorsMap == null) {
            return BMError.paramError("PriceFloors not provided");
        }

        final Request.Item.Builder itemBuilder = Request.Item.newBuilder();
        itemBuilder.setId(UUID.randomUUID().toString());
        itemBuilder.setQty(1);

        for (Map.Entry<String, Double> bid : priceFloorsMap.entrySet()) {
            final Request.Item.Deal.Builder dealBuilder = Request.Item.Deal.newBuilder();
            dealBuilder.setId(bid.getKey());
            dealBuilder.setFlr(bid.getValue());
            dealBuilder.setFlrcur("USD");
            itemBuilder.addDeal(dealBuilder);
        }

        final Placement.Builder placementBuilder = Placement.newBuilder();
        placementBuilder.setSsai(0);
        placementBuilder.setSdk(BidMachine.NAME);
        placementBuilder.setSdkver(BidMachine.VERSION);
        for (Message.Builder displayBuilder : placements) {
            if (displayBuilder instanceof Placement.DisplayPlacement.Builder) {
                placementBuilder.setDisplay((Placement.DisplayPlacement.Builder) displayBuilder);
            } else if (displayBuilder instanceof Placement.VideoPlacement.Builder) {
                placementBuilder.setVideo((Placement.VideoPlacement.Builder) displayBuilder);
            } else {
                throw new IllegalArgumentException("Unsupported display type: " + displayBuilder);
            }

        }

        onBuildPlacement(placementBuilder);
        itemBuilder.setSpec(Any.pack(placementBuilder.build()));

        requestBuilder.addItem(itemBuilder.build());

        //Context
        final Context.Builder contextBuilder = Context.newBuilder();

        //Context -> App
        final Context.App.Builder appBuilder = Context.App.newBuilder();
        targetingParams.build(context, appBuilder, bidMachine.getTargetingParams(), restrictions);

        contextBuilder.setApp(appBuilder);

        //Context -> Restrictions
        if (blockedParams != null) {
            final Context.Restrictions.Builder restrictionsBuilder = Context.Restrictions.newBuilder();
            blockedParams.build(context,
                    restrictionsBuilder,
                    bidMachine.getTargetingParams().getBlockedParams(),
                    restrictions);
            contextBuilder.setRestrictions(restrictionsBuilder);
        }

        //Context -> User
        final Context.User.Builder userBuilder = Context.User.newBuilder();
        userRestrictionParams.build(context, userBuilder, bidMachine.getUserRestrictionParams(), restrictions);
        if (restrictions.canSendUserInfo()) {
            targetingParams.build(context, userBuilder, bidMachine.getTargetingParams(), restrictions);
        }
        contextBuilder.setUser(userBuilder);

        //Context -> Regs
        final Context.Regs.Builder regsBuilder = Context.Regs.newBuilder();
        userRestrictionParams.build(context, regsBuilder, bidMachine.getUserRestrictionParams(),
                restrictions);
        contextBuilder.setRegs(regsBuilder);

        //Context -> Device
        final Context.Device.Builder deviceBuilder = Context.Device.newBuilder();
        bidMachine.getDeviceParams().build(context, deviceBuilder, targetingParams,
                bidMachine.getTargetingParams(), restrictions);
        userRestrictionParams.build(context, regsBuilder, bidMachine.getUserRestrictionParams(),
                restrictions);
        contextBuilder.setDevice(deviceBuilder);

        requestBuilder.setContext(Any.pack(contextBuilder.build()));

        requestBuilder.setTest(bidMachine.isTestMode());
        requestBuilder.addCur("USD");
        requestBuilder.setAt(2);
        requestBuilder.setTmax(10000);

        //Request
        final RequestExtension.Builder requestExtensionBuilder = RequestExtension.newBuilder();
        requestExtensionBuilder.setSellerId(sellerId);

        requestBuilder.addExt(Any.pack(requestExtensionBuilder.build()));

        return requestBuilder.build();
    }

    protected void onBuildPlacement(Placement.Builder builder) {
    }

    protected BMError verifyRequest() {
        return null;
    }

    @NonNull
    protected abstract AdsType getType();

    boolean isValid() {
        return !TextUtils.isEmpty(BidMachineImpl.get().getSellerId());
    }

    boolean isPlacementBuilderMatch(PlacementBuilder placementBuilder) {
        return true;
    }

    @Nullable
    public AuctionResult getAuctionResult() {
        return auctionResult;
    }

    public void request(@NonNull final android.content.Context context) {
        if (!BidMachineImpl.get().isInitialized()) {
            processRequestFail(BMError.NotInitialized);
            return;
        }
        SessionTracker.eventStart(this, TrackEventType.AuctionRequest, getType());
        try {
            if (currentApiRequest != null) {
                currentApiRequest.cancel();
            }
            final Object requestBuildResult = build(context, getType());
            if (requestBuildResult instanceof Request) {
                Logger.log(toString() + ": api request start");
                currentApiRequest = new ApiRequest.Builder<Request, Response>()
                        .url(BidMachineImpl.get().getAuctionUrl())
                        .setRequestData((Request) requestBuildResult)
                        .setDataBinder(getType().getBinder())
                        .setCallback(new NetworkRequest.Callback<Response, BMError>() {
                            @Override
                            public void onSuccess(@Nullable Response result) {
                                Logger.log(toString() + ": api request success");
                                currentApiRequest = null;
                                processRequestSuccess(result);
                            }

                            @Override
                            public void onFail(@Nullable BMError result) {
                                result = BMError.noFillError(result);
                                Logger.log(toString() + ": api request fail - " + result);
                                currentApiRequest = null;
                                processRequestFail(result);
                            }
                        })
                        .setCancelCallback(new NetworkRequest.CancelCallback() {
                            @Override
                            public void onCanceled() {
                                SessionTracker.eventFinish(
                                        AdRequest.this,
                                        TrackEventType.AuctionRequestCancel,
                                        getType(),
                                        null);
                                SessionTracker.clearEvent(
                                        AdRequest.this,
                                        TrackEventType.AuctionRequest);
                            }
                        })
                        .request();
            } else {
                processRequestFail(requestBuildResult instanceof BMError
                        ? (BMError) requestBuildResult
                        : BMError.Internal);
            }
        } catch (Exception e) {
            Logger.log(e);
            processRequestFail(BMError.Internal);
        }
    }

    void cancel() {
        if (currentApiRequest != null) {
            currentApiRequest.cancel();
            currentApiRequest = null;
        }
    }

    @SuppressWarnings("unchecked")
    private void processExpired() {
        isExpired = true;
        unsubscribeExpireTracker();
        if (adRequestListeners != null) {
            for (AdRequestListener<SelfType> listener : adRequestListeners) {
                listener.onRequestExpired((SelfType) this);
            }
        }
    }

    /**
     * @return true if Ads was expired
     */
    public boolean isExpired() {
        return isExpired;
    }

    public void addListener(@Nullable AdRequestListener<SelfType> listener) {
        if (adRequestListeners == null) {
            adRequestListeners = new ArrayList<>(2);
        }
        if (listener != null) {
            adRequestListeners.add(listener);
        }
    }

    public void removeListener(@Nullable AdRequestListener<SelfType> listener) {
        if (adRequestListeners != null && listener != null) {
            adRequestListeners.remove(listener);
        }
    }

    public void processShown() {
        unsubscribeExpireTracker();
    }

    private void subscribeExpireTracker() {
        final long expTime = expirationTime * 1000;
        if (expTime > 0) {
            if (!isExpireTrackerSubscribed) {
                isExpireTrackerSubscribed = true;
                io.bidmachine.core.Utils.onBackgroundThread(expiredRunnable, expTime);
            }
        }
    }

    private void unsubscribeExpireTracker() {
        isExpireTrackerSubscribed = false;
        io.bidmachine.core.Utils.cancelBackgroundThreadTask(expiredRunnable);
    }

    @SuppressWarnings("unchecked")
    private void processRequestSuccess(@Nullable Response response) {
        if (response != null && response.getSeatbidCount() > 0) {
            final Response.Seatbid seatbid = response.getSeatbid(0);
            if (seatbid != null && seatbid.getBidCount() > 0) {
                final Response.Seatbid.Bid bid = seatbid.getBid(0);
                if (bid != null) {
                    if (bid.getMedia() != null && bid.getMedia().is(Ad.class)) {
                        try {
                            if (bid.getMedia().is(Ad.class)) {
                                Ad ad = bid.getMedia().unpack(Ad.class);
                                if (ad != null) {
                                    adResult = ad;
                                    bidResult = bid;
                                    seatBidResult = seatbid;
                                    auctionResult = new AuctionResultImpl(seatbid, bid, ad);
                                    expirationTime = getOrDefault(bid.getExp(),
                                            Response.Seatbid.Bid.getDefaultInstance().getExp(),
                                            DEF_EXPIRATION_TIME);
                                    subscribeExpireTracker();
                                    Logger.log(toString() + ": Request finished (" + auctionResult + ")");
                                    if (adRequestListeners != null) {
                                        for (AdRequestListener listener : adRequestListeners) {
                                            listener.onRequestSuccess(this, auctionResult);
                                        }
                                    }
                                    SessionTracker.eventFinish(
                                            AdRequest.this,
                                            TrackEventType.AuctionRequest,
                                            getType(),
                                            null);
                                    return;
                                }
                            }
                        } catch (InvalidProtocolBufferException e) {
                            Logger.log(e);
                        }
                    } else {
                        Logger.log(toString() + ": Media not found or not valid");
                    }
                } else {
                    Logger.log(toString() + ": Bid not found or not valid");
                }
            } else {
                Logger.log(toString() + ": Seatbid not found or not valid");
            }
        } else {
            Logger.log(toString() + ": Response not found or not valid");
        }
        processRequestFail(BMError.Internal);
    }

    @SuppressWarnings("unchecked")
    private void processRequestFail(BMError error) {
        if (adRequestListeners != null) {
            for (AdRequestListener listener : adRequestListeners) {
                listener.onRequestFailed(this, error);
            }
        }
        SessionTracker.eventFinish(
                AdRequest.this,
                TrackEventType.AuctionRequest,
                getType(),
                error);
    }

    @Override
    public Object getTrackingKey() {
        return trackingId;
    }

    @Nullable
    @Override
    public List<String> getTrackingUrls(@NonNull TrackEventType eventType) {
        return BidMachineImpl.get().getTrackingUrls(eventType);
    }

    @NonNull
    @Override
    public String toString() {
        return getClass().getSimpleName() + "[@" + Integer.toHexString(hashCode()) + "]";
    }

    public interface AdRequestListener<AdRequestType extends AdRequest> {
        /**
         * Called when AdRequest was requested successfully
         *
         * @param request       - AdRequest instance
         * @param auctionResult - AuctionResult info
         */
        void onRequestSuccess(@NonNull AdRequestType request, @NonNull AuctionResult auctionResult);

        /**
         * Called when AdRequest request failed
         *
         * @param request - AdRequest instance
         * @param error   - BMError with additional info about error
         */
        void onRequestFailed(@NonNull AdRequestType request, @NonNull BMError error);

        /**
         * Called when AdRequest expired
         *
         * @param request - AdRequest instance
         */
        void onRequestExpired(@NonNull AdRequestType request);
    }

    protected static abstract class AdRequestBuilderImpl<
            SelfType extends RequestBuilder,
            ReturnType extends AdRequest>
            implements RequestBuilder<SelfType, ReturnType> {

        protected ReturnType params;

        protected AdRequestBuilderImpl() {
        }

        @Override
        @SuppressWarnings("unchecked")
        public SelfType setPriceFloorParams(PriceFloorParams priceFloorParams) {
            prepareRequest();
            params.priceFloorParams = priceFloorParams;
            return (SelfType) this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public SelfType setTargetingParams(TargetingParams userParams) {
            prepareRequest();
            params.targetingParams = userParams;
            return (SelfType) this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public SelfType setListener(AdRequestListener<ReturnType> listener) {
            prepareRequest();
            params.addListener(listener);
            return (SelfType) this;
        }

//        @Override
//        @SuppressWarnings("unchecked")
//        public SelfType setExtraParams(ExtraParams extraParams) {
//            prepareRequest();
//            params.extraParams = extraParams;
//            return (SelfType) this;
//        }

        @Override
        public ReturnType build() {
            try {
                prepareRequest();
                return params;
            } finally {
                params = null;
            }
        }

        protected void prepareRequest() {
            if (params == null) {
                params = createRequest();
            }
        }

        protected abstract ReturnType createRequest();

    }

}