package io.bidmachine;

import android.content.Context;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.explorestack.protobuf.adcom.Ad;
import com.explorestack.protobuf.openrtb.Response;
import io.bidmachine.core.Logger;
import io.bidmachine.core.Utils;
import io.bidmachine.models.AdObject;
import io.bidmachine.models.AdObjectParams;
import io.bidmachine.models.AuctionResult;
import io.bidmachine.rewarded.RewardedAd;
import io.bidmachine.rewarded.RewardedListener;
import io.bidmachine.utils.BMError;

import java.util.ArrayList;
import java.util.List;

public abstract class BidMachineAd<
        SelfType extends IAd,
        AdRequestType extends AdRequest<AdRequestType, ?>,
        AdObjectType extends AdObject<AdObjectParamsType>,
        AdObjectParamsType extends AdObjectParams,
        AdListenerType extends AdListener<SelfType>>
        implements IAd<SelfType, AdRequestType> {

    @NonNull
    private final Context context;
    @NonNull
    private final AdsType adsType;
    @Nullable
    private AdRequestType adRequest;
    @Nullable
    private AdListenerType listener;
    @Nullable
    private AdObjectType loadedObject;
    @NonNull
    private State currentState = State.Idle;

    private boolean isShownTracked;
    private boolean isImpressionTracked;
    private boolean isFinishTracked;

    private final ContextProvider contextProvider = new ContextProvider.SimpleContextProvider() {
        @NonNull
        @Override
        public Context getContext() {
            return context;
        }
    };

    private final TrackingObject trackingObject = new TrackingObject() {
        @Override
        public Object getTrackingKey() {
            AuctionResult auctionResult = getAuctionResult();
            if (auctionResult != null) {
                return auctionResult.getId();
            }
            return "-1";
        }

        @Nullable
        @Override
        public List<String> getTrackingUrls(@NonNull TrackEventType eventType) {
            ArrayList<String> outList = new ArrayList<>();
            List<String> urls = loadedObject != null && loadedObject.getParams() != null
                    ? loadedObject.getParams().getTrackUrls(eventType) : null;
            if (urls != null) {
                outList.addAll(urls);
            }
            List<String> baseUrls = BidMachineImpl.get().getTrackingUrls(eventType);
            if (baseUrls != null) {
                outList.addAll(baseUrls);
            }
            return outList;
        }
    };

    public BidMachineAd(@NonNull Context context, @NonNull AdsType adsType) {
        this.context = context;
        this.adsType = adsType;
    }

    @NonNull
    Context getContext() {
        return context;
    }

    @Nullable
    @Override
    public AuctionResult getAuctionResult() {
        return adRequest != null ? adRequest.getAuctionResult() : null;
    }

    @Nullable
    protected final AdObjectType getLoadedObject() {
        return loadedObject;
    }

    @SuppressWarnings("unchecked")
    public SelfType setListener(@Nullable AdListenerType listener) {
        this.listener = listener;
        return (SelfType) this;
    }

    @NonNull
    protected final AdsType getType() {
        return adsType;
    }

    @Nullable
    public AdRequestType getAdRequest() {
        return adRequest;
    }

    /*
    Processing logic
     */

    @Override
    @SuppressWarnings("unchecked")
    public SelfType load(AdRequestType request) {
        processCallback.log("load requested");
        if (!BidMachineImpl.get().isInitialized()) {
            processRequestFail(BMError.NotInitialized);
            return (SelfType) this;
        }
        if (currentState != State.Idle) {
            processCallback.log("request process abort because it's already processing");
            return (SelfType) this;
        }
        if (request == null) {
            processRequestFail(BMError.paramError("No Request"));
            return (SelfType) this;
        }
        detachRequest(adRequest);
        adRequest = request;
        attachRequest(adRequest);
        processRequest(request);
        return (SelfType) this;
    }

    @Override
    public void destroy() {
        processCallback.processDestroy();
        SessionTracker.clear(trackingObject);
    }

    @Override
    public boolean isLoading() {
        return currentState == State.Requesting || currentState == State.Loading;
    }

    @Override
    public boolean isLoaded() {
        return loadedObject != null && currentState == State.Success;
    }

    @Override
    public boolean canShow() {
        return isLoaded();
    }

    @Override
    public boolean isDestroyed() {
        return currentState == State.Destroyed;
    }

    @Override
    public boolean isExpired() {
        return currentState == State.Expired;
    }

    boolean prepareShow() {
        if (isDestroyed()) {
            processCallback.processShowFail(BMError.Destroyed);
            return false;
        } else if (isExpired()) {
            processCallback.processShowFail(BMError.Expired);
            return false;
        } else if (!isLoaded() || loadedObject == null) {
            processCallback.processShowFail(BMError.NotLoaded);
            return false;
        } else if (isShownTracked) {
            processCallback.processShowFail(BMError.AlreadyShown);
            return false;
        }
        return true;
    }

    /*
   Processing methods
    */

    private void processRequest(@NonNull final AdRequestType request) {
        processCallback.log("process request start");
        final AuctionResult auctionResult = getAuctionResult();
        if (auctionResult != null) {
            if (request.isExpired()) {
                processCallback.log("AuctionResult expired, please request new one");
                processRequestFail(BMError.Expired);
            } else {
                processRequestSuccess(request,
                        request.seatBidResult,
                        request.bidResult,
                        request.adResult);
            }
            return;
        }
        currentState = State.Requesting;
        request.request(getContext());
    }

    private void processRequestSuccess(@Nullable AdRequestType request,
                                       @Nullable Response.Seatbid seatbid,
                                       @Nullable Response.Seatbid.Bid bid,
                                       @Nullable Ad ad) {
        if (currentState.ordinal() > State.Loading.ordinal()) return;
        SessionTracker.eventStart(trackingObject, TrackEventType.Load, getType());
        currentState = State.Loading;
        if (request == null || seatbid == null || bid == null || ad == null) {
            processRequestFail(BMError.Internal);
        } else {
            BMError processResult = processResponseSuccess(seatbid, bid, ad, request);
            if (processResult != null) {
                processCallback.processLoadFail(processResult);
            }
        }
    }

    private void attachRequest(@Nullable AdRequestType request) {
        if (request != null) {
            request.addListener(adRequestListener);
        }
    }

    private void detachRequest(@Nullable AdRequestType request) {
        if (request != null) {
            request.removeListener(adRequestListener);
        }
    }

    @Nullable
    private BMError processResponseSuccess(@NonNull Response.Seatbid seatbid,
                                           @NonNull Response.Seatbid.Bid bid,
                                           @NonNull Ad ad,
                                           @NonNull AdRequestType adRequest) {
        NetworkConfig networkConfig = getType().obtainNetworkConfig(contextProvider, adRequest.getUnifiedRequestParams(), ad);
        if (networkConfig != null) {
            AdObjectParams adObjectParams = getType().createAdObjectParams(contextProvider, seatbid, bid, ad, adRequest);
            if (adObjectParams != null && adObjectParams.isValid()) {
                loadedObject = createAdObject(contextProvider, adRequest, networkConfig.getAdapter(), adObjectParams, processCallback);
                if (loadedObject != null) {
                    networkConfig.getAdapter().load(contextProvider, loadedObject, null);
                    return null;
                }
            }
            return BMError.IncorrectAdUnit;
        }
        return BMError.adapterNotFoundError("for Ad with id: " + ad.getId());
    }

    protected abstract AdObjectType createAdObject(@NonNull ContextProvider contextProvider,
                                                   @NonNull AdRequestType adRequest,
                                                   @NonNull NetworkAdapter adapter,
                                                   @NonNull AdObjectParams adObjectParams,
                                                   @NonNull AdProcessCallback processCallback);

    private void processRequestFail(BMError error) {
        if (currentState.ordinal() > State.Loading.ordinal()) return;
        SessionTracker.eventStart(trackingObject, TrackEventType.Load, getType());
        processCallback.processLoadFail(error);
    }

    private final AdRequest.AdRequestListener<AdRequestType> adRequestListener =
            new AdRequest.AdRequestListener<AdRequestType>() {
                @Override
                public void onRequestSuccess(@NonNull AdRequestType request,
                                             @NonNull AuctionResult auctionResult) {
                    if (request == adRequest) {
                        processRequestSuccess(request,
                                request.seatBidResult,
                                request.bidResult,
                                request.adResult);
                    }
                }

                @Override
                public void onRequestFailed(@NonNull AdRequestType request,
                                            @NonNull BMError error) {
                    if (request == adRequest) {
                        processRequestFail(error);
                    }
                }

                @Override
                public void onRequestExpired(@NonNull AdRequestType request) {
                    if (request == adRequest) {
                        processCallback.processExpired();
                    }
                }
            };

    @CallSuper
    protected void onImpression() {
    }

    @CallSuper
    protected void onDestroy() {
    }

    final AdProcessCallback processCallback = new AdProcessCallback() {

        @Override
        @SuppressWarnings("unchecked")
        public void processLoadSuccess() {
            if (currentState.ordinal() > State.Loading.ordinal()) {
                return;
            }
            log("processLoadSuccess");
            currentState = State.Success;
            trackEvent(TrackEventType.Load, null);
            Utils.onUiThread(new Runnable() {
                @Override
                public void run() {
                    if (listener != null) {
                        log("notify AdLoaded");
                        listener.onAdLoaded((SelfType) BidMachineAd.this);
                    }
                }
            });
        }

        @Override
        @SuppressWarnings("unchecked")
        public void processLoadFail(final BMError error) {
            log("processLoadFail - " + error.getMessage());
            currentState = State.Failed;
            trackEvent(TrackEventType.Load, error);
            Utils.onUiThread(new Runnable() {
                @Override
                public void run() {
                    if (listener != null) {
                        log("notify AdLoadFailed");
                        listener.onAdLoadFailed((SelfType) BidMachineAd.this, error);
                    }
                }
            });
        }

        @Override
        @SuppressWarnings("unchecked")
        public void processShown() {
            if (currentState.ordinal() > State.Success.ordinal()) {
                return;
            }
            if (isShownTracked) {
                return;
            }
            isShownTracked = true;
            if (adRequest != null) {
                adRequest.processShown();
            }
            if (loadedObject != null) {
                loadedObject.onShown();
            }
            log("processShown");
            trackEvent(TrackEventType.Show, null);
            Utils.onUiThread(new Runnable() {
                @Override
                public void run() {
                    if (listener != null) {
                        log("notify AdShown");
                        listener.onAdShown((SelfType) BidMachineAd.this);
                    }
                }
            });
        }

        @Override
        @SuppressWarnings("unchecked")
        public void processShowFail(final BMError error) {
            log("processShowFail");
            trackEvent(TrackEventType.Show, error);
            Utils.onUiThread(new Runnable() {
                @Override
                public void run() {
                    if (listener instanceof AdFullScreenListener) {
                        log("notify AdShowFailed");
                        ((AdFullScreenListener) listener).onAdShowFailed(BidMachineAd.this, error);
                    }
                }
            });
        }

        @Override
        @SuppressWarnings("unchecked")
        public void processClicked() {
            if (currentState.ordinal() > State.Success.ordinal()) {
                return;
            }
            if (loadedObject != null) {
                loadedObject.onClicked();
            }
            log("processClicked");
            trackEvent(TrackEventType.Click, null);
            Utils.onUiThread(new Runnable() {
                @Override
                public void run() {
                    if (listener != null) {
                        log("notify AdClicked");
                        listener.onAdClicked((SelfType) BidMachineAd.this);
                    }
                }
            });
        }

        @Override
        @SuppressWarnings("unchecked")
        public void processImpression() {
            if (currentState.ordinal() > State.Success.ordinal()) {
                return;
            }
            if (isImpressionTracked) {
                return;
            }
            isImpressionTracked = true;
            if (loadedObject != null) {
                loadedObject.onImpression();
            }
            onImpression();
            log("processImpression");
            trackEvent(TrackEventType.Impression, null);
            Utils.onUiThread(new Runnable() {
                @Override
                public void run() {
                    if (listener != null) {
                        log("notify AdImpression");
                        listener.onAdImpression((SelfType) BidMachineAd.this);
                    }
                }
            });
        }

        @Override
        public void processFinished() {
            if (currentState.ordinal() > State.Success.ordinal()) {
                return;
            }
            isFinishTracked = true;
            log("processFinished");
            Utils.onUiThread(new Runnable() {
                @Override
                public void run() {
                    if (listener instanceof RewardedListener) {
                        log("notify AdRewarded");
                        ((RewardedListener) listener).onAdRewarded((RewardedAd) BidMachineAd.this);
                    }
                }
            });
        }

        @Override
        @SuppressWarnings("unchecked")
        public void processClosed() {
            if (currentState.ordinal() > State.Success.ordinal()) {
                return;
            }
            log("processClosed (" + isFinishTracked + ")");
            trackEvent(TrackEventType.Close, null);
            Utils.onUiThread(new Runnable() {
                @Override
                public void run() {
                    if (listener instanceof AdFullScreenListener) {
                        log("notify AdClosed");
                        ((AdFullScreenListener) listener).onAdClosed(BidMachineAd.this, isFinishTracked);
                    }
                }
            });
        }

        @Override
        @SuppressWarnings("unchecked")
        public void processExpired() {
            if (currentState.ordinal() > State.Success.ordinal()) {
                return;
            }
            log("processExpired");
            currentState = State.Expired;
            trackEvent(TrackEventType.Expired, null);
            Utils.onUiThread(new Runnable() {
                @Override
                public void run() {
                    if (listener != null) {
                        log("notify AdExpired");
                        listener.onAdExpired((SelfType) BidMachineAd.this);
                    }
                }
            });
        }

        @Override
        public void processDestroy() {
            log("destroy requested");
            trackEvent(TrackEventType.Destroy, null);
            currentState = State.Destroyed;
            if (adRequest != null) {
                adRequest.cancel();
                detachRequest(adRequest);
            }
            if (loadedObject != null) {
                loadedObject.onDestroy();
            }
            onDestroy();
        }

        @Override
        public void log(String message) {
            Logger.log(String.format("%s: %s", toStringShort(), message));
        }
    };

    private void trackEvent(TrackEventType eventType, @Nullable BMError error) {
        SessionTracker.eventFinish(trackingObject, eventType, getType(), error);
    }

    @NonNull
    @Override
    public String toString() {
        return toStringShort() + ": state=" + currentState + ", auctionResult=" + getAuctionResult();
    }

    /*
    Logger helpers
     */

    private String cachedClassTag;

    protected String toStringShort() {
        if (cachedClassTag == null) {
            cachedClassTag = getClass().getSimpleName() + "[@" + Integer.toHexString(
                    hashCode()) + "]";
        }
        return cachedClassTag;
    }

    /*
    Inner classes
     */

    enum State {
        Idle, Requesting, Loading, Success, Failed, Destroyed, Expired
    }

}
