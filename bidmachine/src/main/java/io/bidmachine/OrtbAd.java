package io.bidmachine;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import com.explorestack.protobuf.adcom.Ad;
import com.explorestack.protobuf.openrtb.Response;
import io.bidmachine.adapters.OrtbAdapter;
import io.bidmachine.core.Logger;
import io.bidmachine.core.Utils;
import io.bidmachine.models.AdObject;
import io.bidmachine.models.AdObjectParams;
import io.bidmachine.models.AuctionResult;
import io.bidmachine.utils.BMError;

public abstract class OrtbAd<
        SelfType extends IAd,
        AdRequestType extends AdRequest<AdRequestType>,
        AdObjectType extends AdObject<SelfType, AdObjectParamsType>,
        AdObjectParamsType extends AdObjectParams,
        AdListenerType extends AdListener<SelfType>>
        implements IAd<SelfType, AdRequestType>, TrackingObject {

    @NonNull
    private Context context;
    @Nullable
    private AdRequestType adRequest;
    @Nullable
    private AdListenerType listener;
    @Nullable
    private AdObjectType loadedObject;
    @NonNull
    private State currentState = State.Idle;

    private boolean wasShown = false;

    public OrtbAd(@NonNull Context context) {
        this.context = context;
    }

    @NonNull
    public Context getContext() {
        if (context instanceof Activity) {
            return context;
        } else {
            return BidMachineImpl.getTopActivity();
        }
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
    protected abstract AdsType getType();

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
        Logger.log(toStringShort() + ": load requested");
        if (!BidMachineImpl.get().isInitialized()) {
            processRequestFail(BMError.NotInitialized);
            return (SelfType) this;
        }
        if (currentState != State.Idle) {
            Logger.log(toStringShort() + ": request process abort because it's already processing");
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
        SessionTracker.clear(this);
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

    protected boolean isDestroyed() {
        return currentState == State.Destroyed;
    }

    protected boolean isExpired() {
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
        } else if (wasShown) {
            processCallback.processShowFail(BMError.AlreadyShown);
            return false;
        }
        return true;
    }

    /*
   Processing methods
    */

    private void processRequest(@NonNull final AdRequestType request) {
        Logger.log(toStringShort() + ": process request start");
        final AuctionResult auctionResult = getAuctionResult();
        if (auctionResult != null) {
            if (request.isExpired()) {
                Logger.log(toStringShort() + ": AuctionResult expired, please request new one");
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
        SessionTracker.eventStart(this, TrackEventType.Load, getType());
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
    @SuppressWarnings("unchecked")
    private BMError processResponseSuccess(@NonNull Response.Seatbid seatbid,
                                           @NonNull Response.Seatbid.Bid bid,
                                           @NonNull Ad ad,
                                           @NonNull @Deprecated AdRequestType adRequest) {
        OrtbAdapter adapter = getType().findAdapter(context, ad);
        if (adapter != null) {
            AdObjectParams adObjectParams = getType().createAdObjectParams(getContext(), seatbid,
                                                                           bid, ad, adRequest);
            if (adObjectParams != null && adObjectParams.isValid()) {
                loadedObject = (AdObjectType) getType().createAdObject(adapter, adObjectParams);
                if (loadedObject != null) {
                    loadedObject.attachAd((SelfType) this);
                    adapter.load(loadedObject);
                    return null;
                }
            }
            return BMError.IncorrectAdUnit;
        }
        return BMError.adapterNotFoundError("for Ad with id: " + ad.getId());
    }

    private void processRequestFail(BMError error) {
        if (currentState.ordinal() > State.Loading.ordinal()) return;
        SessionTracker.eventStart(this, TrackEventType.Load, getType());
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

    final AdProcessCallback processCallback = new AdProcessCallback() {

        @Override
        @SuppressWarnings("unchecked")
        public void processLoadSuccess() {
            if (currentState.ordinal() > State.Loading.ordinal()) {
                return;
            }
            Logger.log(toStringShort() + ": processLoadSuccess");
            currentState = State.Success;
            trackEvent(TrackEventType.Load, null);
            Utils.onUiThread(new Runnable() {
                @Override
                public void run() {
                    if (listener != null) {
                        listener.onAdLoaded((SelfType) OrtbAd.this);
                    }
                }
            });
        }

        @Override
        @SuppressWarnings("unchecked")
        public void processLoadFail(final BMError error) {
            Logger.log(toStringShort() + ": processLoadFail - " + error.getMessage());
            currentState = State.Failed;
            trackEvent(TrackEventType.Load, error);
            Utils.onUiThread(new Runnable() {
                @Override
                public void run() {
                    if (listener != null) {
                        listener.onAdLoadFailed((SelfType) OrtbAd.this, error);
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
            wasShown = true;
            if (adRequest != null) {
                adRequest.processShown();
            }
            Logger.log(toStringShort() + ": processShown");
            trackEvent(TrackEventType.Show, null);
            Utils.onUiThread(new Runnable() {
                @Override
                public void run() {
                    if (listener != null) {
                        listener.onAdShown((SelfType) OrtbAd.this);
                    }
                }
            });
        }

        @Override
        @SuppressWarnings("unchecked")
        public void processShowFail(final BMError error) {
            Logger.log(toStringShort() + ": processShowFail");
            trackEvent(TrackEventType.Show, error);
            Utils.onUiThread(new Runnable() {
                @Override
                public void run() {
                    if (listener instanceof AdFullScreenListener) {
                        ((AdFullScreenListener) listener).onAdShowFailed(OrtbAd.this, error);
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
            Logger.log(toStringShort() + ": processClicked");
            trackEvent(TrackEventType.Click, null);
            Utils.onUiThread(new Runnable() {
                @Override
                public void run() {
                    if (listener != null) {
                        listener.onAdClicked((SelfType) OrtbAd.this);
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
            Logger.log(toStringShort() + ": processImpression");
            trackEvent(TrackEventType.Impression, null);
            Utils.onUiThread(new Runnable() {
                @Override
                public void run() {
                    if (listener != null) {
                        listener.onAdImpression((SelfType) OrtbAd.this);
                    }
                }
            });
        }

        @Override
        @SuppressWarnings("unchecked")
        public void processFinished() {
            if (currentState.ordinal() > State.Success.ordinal()) {
                return;
            }
            Logger.log(toStringShort() + ": processFinished");
            Utils.onUiThread(new Runnable() {
                @Override
                public void run() {
                    if (listener instanceof AdRewardedListener) {
                        ((AdRewardedListener) listener).onAdRewarded(OrtbAd.this);
                    }
                }
            });
        }

        @Override
        @SuppressWarnings("unchecked")
        public void processClosed(final boolean finished) {
            if (currentState.ordinal() > State.Success.ordinal()) {
                return;
            }
            Logger.log(toStringShort() + ": processClosed(" + finished + ")");
            trackEvent(TrackEventType.Close, null);
            Utils.onUiThread(new Runnable() {
                @Override
                public void run() {
                    if (listener instanceof AdFullScreenListener) {
                        ((AdFullScreenListener) listener).onAdClosed(OrtbAd.this, finished);
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
            Logger.log(toStringShort() + ": processExpired");
            currentState = State.Expired;
            trackEvent(TrackEventType.Expired, null);
            Utils.onUiThread(new Runnable() {
                @Override
                public void run() {
                    if (listener != null) {
                        listener.onAdExpired((SelfType) OrtbAd.this);
                    }
                }
            });
        }

        @Override
        public void processDestroy() {
            Logger.log(toStringShort() + ": destroy requested");
            trackEvent(TrackEventType.Destroy, null);
            currentState = State.Destroyed;
            if (adRequest != null) {
                adRequest.cancel();
                detachRequest(adRequest);
            }
            if (loadedObject != null) {
                loadedObject.destroy();
            }
        }
    };

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

    private void trackEvent(TrackEventType eventType, @Nullable BMError error) {
        SessionTracker.eventFinish(OrtbAd.this, eventType, getType(), error);
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
