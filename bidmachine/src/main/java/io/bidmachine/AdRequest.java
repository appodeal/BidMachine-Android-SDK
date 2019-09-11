package io.bidmachine;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;

import com.explorestack.protobuf.adcom.Ad;
import com.explorestack.protobuf.adcom.Context;
import com.explorestack.protobuf.adcom.Placement;
import com.explorestack.protobuf.openrtb.Request;
import com.explorestack.protobuf.openrtb.Response;
import com.explorestack.protobuf.Any;
import com.explorestack.protobuf.InvalidProtocolBufferException;
import com.explorestack.protobuf.Message;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import io.bidmachine.core.Logger;
import io.bidmachine.core.NetworkRequest;
import io.bidmachine.displays.PlacementBuilder;
import io.bidmachine.models.AuctionResult;
import io.bidmachine.models.DataRestrictions;
import io.bidmachine.models.RequestBuilder;
import io.bidmachine.models.RequestParams;
import io.bidmachine.models.TargetingInfo;
import io.bidmachine.protobuf.RequestExtension;
import io.bidmachine.unified.UnifiedAdRequestParams;
import io.bidmachine.utils.BMError;

import static io.bidmachine.Utils.getOrDefault;
import static io.bidmachine.core.Utils.oneOf;

public abstract class AdRequest<SelfType extends AdRequest, UnifiedAdRequestParamsType extends UnifiedAdRequestParams> {

    private static final Executor buildExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);

    private static final long DEF_EXPIRATION_TIME = TimeUnit.MINUTES.toSeconds(29);

    private final String trackingId = UUID.randomUUID().toString();

    @NonNull
    private final AdsType adsType;

    PriceFloorParams priceFloorParams;
    TargetingParams targetingParams;

    @VisibleForTesting
    UserRestrictionParams userRestrictionParams;
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
    @Nullable
    private UnifiedAdRequestParamsType unifiedAdRequestParams;

    private long expirationTime = -1;

    private boolean isExpired;
    private boolean isExpireTrackerSubscribed;

    private final Runnable expiredRunnable = new Runnable() {
        @Override
        public void run() {
            processExpired();
        }
    };

    private final TrackingObject trackingObject = new TrackingObject() {
        @Override
        public Object getTrackingKey() {
            return trackingId;
        }
    };

    protected AdRequest(@NonNull AdsType adsType) {
        this.adsType = adsType;
    }

    @VisibleForTesting
    Object build(final android.content.Context context, AdsType adsType) {
        final String sellerId = BidMachineImpl.get().getSellerId();
        if (TextUtils.isEmpty(sellerId)) {
            return BMError.paramError("Seller Id not provided");
        }
        assert sellerId != null;

        BMError implVerifyError = verifyRequest();
        if (implVerifyError != null) {
            return implVerifyError;
        }

        final BidMachineImpl bidMachine = BidMachineImpl.get();

        final Request.Builder requestBuilder = Request.newBuilder();
        final TargetingParams targetingParams =
                RequestParams.resolveParams(this.targetingParams, bidMachine.getTargetingParams());
        final BlockedParams blockedParams = targetingParams.getBlockedParams();
        final UserRestrictionParams userRestrictionParams =
                RequestParams.resolveParams(this.userRestrictionParams, bidMachine.getUserRestrictionParams());
        unifiedAdRequestParams = createUnifiedAdRequestParams(targetingParams, userRestrictionParams);

        //PriceFloor params
        final PriceFloorParams priceFloorParams = oneOf(this.priceFloorParams, bidMachine.getPriceFloorParams());
        final Map<String, Double> priceFloorsMap =
                priceFloorParams.getPriceFloors() == null || priceFloorParams.getPriceFloors().size() == 0
                        ? bidMachine.getPriceFloorParams().getPriceFloors() : priceFloorParams.getPriceFloors();

        if (priceFloorsMap == null) {
            return BMError.paramError("PriceFloors not provided");
        }

        final ArrayList<Message.Builder> placements = new ArrayList<>();
        adsType.collectDisplayPlacements(
                new SimpleContextProvider(context), this, unifiedAdRequestParams, placements);

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
        targetingParams.build(context, appBuilder);

        contextBuilder.setApp(appBuilder);

        //Context -> Restrictions
        if (blockedParams != null) {
            final Context.Restrictions.Builder restrictionsBuilder = Context.Restrictions.newBuilder();
            blockedParams.build(restrictionsBuilder);
            contextBuilder.setRestrictions(restrictionsBuilder);
        }

        //Context -> User
        final Context.User.Builder userBuilder = Context.User.newBuilder();
        userRestrictionParams.build(userBuilder);
        if (userRestrictionParams.canSendUserInfo()) {
            targetingParams.build(userBuilder);
        }
        contextBuilder.setUser(userBuilder);

        //Context -> Regs
        final Context.Regs.Builder regsBuilder = Context.Regs.newBuilder();
        userRestrictionParams.build(regsBuilder);
        contextBuilder.setRegs(regsBuilder);

        //Context -> Device
        final Context.Device.Builder deviceBuilder = Context.Device.newBuilder();
        bidMachine.getDeviceParams().build(context, deviceBuilder, targetingParams,
                bidMachine.getTargetingParams(), userRestrictionParams);
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
    protected final AdsType getType() {
        return adsType;
    }

    boolean isValid() {
        return !TextUtils.isEmpty(BidMachineImpl.get().getSellerId());
    }

    boolean isPlacementBuilderMatch(PlacementBuilder placementBuilder) {
        return true;
    }

    @Nullable
    @SuppressWarnings("WeakerAccess")
    public AuctionResult getAuctionResult() {
        return auctionResult;
    }

    public void request(@NonNull final android.content.Context context) {
        if (!BidMachineImpl.get().isInitialized()) {
            processRequestFail(BMError.NotInitialized);
            return;
        }
        BidMachineEvents.eventStart(trackingObject, TrackEventType.AuctionRequest, getType());
        try {
            if (currentApiRequest != null) {
                currentApiRequest.cancel();
            }
            Logger.log(toString() + ": api request start");
            buildExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    Object requestBuildResult = build(context, getType());
                    if (requestBuildResult instanceof Request) {
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
                                        BidMachineEvents.eventFinish(
                                                trackingObject,
                                                TrackEventType.AuctionRequestCancel,
                                                getType(),
                                                null);
                                        BidMachineEvents.clearEvent(
                                                trackingObject,
                                                TrackEventType.AuctionRequest);
                                    }
                                })
                                .request();
                    } else {
                        processRequestFail(requestBuildResult instanceof BMError
                                ? (BMError) requestBuildResult
                                : BMError.Internal);
                    }
                }
            });
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
    @SuppressWarnings("WeakerAccess")
    public boolean isExpired() {
        return isExpired;
    }

    @SuppressWarnings("WeakerAccess")
    public void addListener(@Nullable AdRequestListener<SelfType> listener) {
        if (adRequestListeners == null) {
            adRequestListeners = new ArrayList<>(2);
        }
        if (listener != null) {
            adRequestListeners.add(listener);
        }
    }

    @SuppressWarnings("WeakerAccess")
    public void removeListener(@Nullable AdRequestListener<SelfType> listener) {
        if (adRequestListeners != null && listener != null) {
            adRequestListeners.remove(listener);
        }
    }

    void onShown() {
        unsubscribeExpireTracker();
    }

    void onExpired() {
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
            if (seatbid == null || seatbid.getBidCount() == 0) {
                Logger.log(toString() + ": Seatbid not found or not valid");
                processRequestFail(BMError.requestError("Seatbid not found or not valid"));
                return;
            }
            final Response.Seatbid.Bid bid = seatbid.getBid(0);
            if (bid == null) {
                Logger.log(toString() + ": Bid not found or not valid");
                processRequestFail(BMError.requestError("Bid not found or not valid"));
                return;
            }
            Any media = bid.getMedia();
            if (media == null || !media.is(Ad.class)) {
                Logger.log(toString() + ": Media not found or not valid");
                processRequestFail(BMError.requestError("Media not found or not valid"));
                return;
            }
            try {
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
                    BidMachineEvents.eventFinish(
                            trackingObject,
                            TrackEventType.AuctionRequest,
                            getType(),
                            null);
                    return;
                } else {
                    Logger.log(toString() + ": Ad not found or not valid");
                }
            } catch (InvalidProtocolBufferException e) {
                Logger.log(e);
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
        BidMachineEvents.eventFinish(
                trackingObject,
                TrackEventType.AuctionRequest,
                getType(),
                error);
    }

    @NonNull
    protected abstract UnifiedAdRequestParamsType createUnifiedAdRequestParams(@NonNull TargetingParams targetingParams,
                                                                               @NonNull DataRestrictions dataRestrictions);

    @Nullable
    final UnifiedAdRequestParamsType getUnifiedRequestParams() {
        return unifiedAdRequestParams;
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

    protected static class BaseUnifiedRequestParams implements UnifiedAdRequestParams {

        private final DataRestrictions dataRestrictions;
        private final TargetingInfo targetingInfo;

        public BaseUnifiedRequestParams(@NonNull TargetingParams targetingParams,
                                        @NonNull DataRestrictions dataRestrictions) {
            this.targetingInfo = new TargetingInfoImpl(dataRestrictions, targetingParams);
            this.dataRestrictions = dataRestrictions;
        }

        @Override
        public DataRestrictions getDataRestrictions() {
            return dataRestrictions;
        }

        @Override
        public TargetingInfo getTargetingParams() {
            return targetingInfo;
        }

        @Override
        public boolean isTestMode() {
            return BidMachineImpl.get().isTestMode();
        }
    }

}