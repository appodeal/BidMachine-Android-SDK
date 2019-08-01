package io.bidmachine.adapters.tapjoy;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import com.tapjoy.TJConnectListener;
import com.tapjoy.Tapjoy;
import io.bidmachine.*;
import io.bidmachine.models.DataRestrictions;
import io.bidmachine.models.TargetingInfo;
import io.bidmachine.unified.UnifiedAdRequestParams;
import io.bidmachine.unified.UnifiedFullscreenAd;
import io.bidmachine.utils.BMError;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

class TapjoyAdapter extends NetworkAdapter implements HeaderBiddingAdapter {

    TapjoyAdapter() {
        super("tapjoy", Tapjoy.getVersion(), new AdsType[]{AdsType.Interstitial, AdsType.Rewarded});
    }

    @Override
    public UnifiedFullscreenAd createInterstitial() {
        return new TapjoyFullscreenAd();
    }

    @Override
    public UnifiedFullscreenAd createRewarded() {
        return new TapjoyFullscreenAd();
    }

    @Override
    protected void onInitialize(@NonNull ContextProvider contextProvider,
                                @NonNull UnifiedAdRequestParams adRequestParams,
                                @NonNull NetworkConfig networkConfig) {
        configure(adRequestParams);
        Map<String, String> networkParams = networkConfig.getNetworkParams();
        if (networkParams != null) {
            final String sdkKey = networkParams.get(TapjoyConfig.KEY_SDK);
            if (!TextUtils.isEmpty(sdkKey)) {
                assert sdkKey != null;
                initializeTapjoy(contextProvider, sdkKey, null);
            }
        }
    }

    @Override
    public void collectHeaderBiddingParams(@NonNull ContextProvider contextProvider,
                                           @NonNull UnifiedAdRequestParams adRequestParams,
                                           @NonNull final HeaderBiddingCollectParamsCallback callback,
                                           @NonNull Map<String, String> mediationConfig) {
        final String sdkKey = mediationConfig.get(TapjoyConfig.KEY_SDK);
        if (TextUtils.isEmpty(sdkKey)) {
            callback.onCollectFail(BMError.requestError("sdk_key not provided"));
            return;
        }
        assert sdkKey != null;
        final String placementName = mediationConfig.get(TapjoyConfig.KEY_PLACEMENT_NAME);
        if (TextUtils.isEmpty(placementName)) {
            callback.onCollectFail(BMError.requestError("placement_name not provided"));
            return;
        }
        assert placementName != null;
        configure(adRequestParams);
        initializeTapjoy(contextProvider, sdkKey, new TapjoyInitializeListener() {
            @Override
            public void onInitialized() {
                Map<String, String> params = new HashMap<>();
                params.put(TapjoyConfig.KEY_SDK, sdkKey);
                params.put(TapjoyConfig.KEY_PLACEMENT_NAME, placementName);
                params.put(TapjoyConfig.KEY_TOKEN, Tapjoy.getUserToken());
                callback.onCollectFinished(params);
            }

            @Override
            public void onInitializationFail(BMError error) {
                callback.onCollectFail(error);
            }
        });
    }

    private static void configure(@NonNull UnifiedAdRequestParams adRequestParams) {
        DataRestrictions dataRestrictions = adRequestParams.getDataRestrictions();
        if (dataRestrictions.isUserInGdprScope()) {
            Tapjoy.subjectToGDPR(true);
            Tapjoy.setUserConsent(dataRestrictions.isUserHasConsent() ? "1" : "0");
        } else {
            Tapjoy.subjectToGDPR(false);
        }
        Tapjoy.belowConsentAge(dataRestrictions.isUserAgeRestricted());
        TargetingInfo targetingInfo = adRequestParams.getTargetingParams();
        String userId = targetingInfo.getUserId();
        if (userId != null) {
            Tapjoy.setUserID(targetingInfo.getUserId());
        }
    }

    private static synchronized void initializeTapjoy(@NonNull final ContextProvider contextProvider,
                                                      @NonNull final String sdkKey,
                                                      @Nullable final TapjoyInitializeListener listener) {
        if (finalizeInitialization(listener)) {
            return;
        }
        new Thread() {
            @Override
            public void run() {
                super.run();
                synchronized (TapjoyAdapter.class) {
                    if (finalizeInitialization(listener)) {
                        return;
                    }
                    final CountDownLatch syncLock = new CountDownLatch(1);
                    Tapjoy.limitedConnect(contextProvider.getContext(), sdkKey, new TJConnectListener() {
                        @Override
                        public void onConnectSuccess() {
                            if (listener != null) {
                                listener.onInitialized();
                            }
                            syncLock.countDown();
                        }

                        @Override
                        public void onConnectFailure() {
                            if (listener != null) {
                                listener.onInitializationFail(BMError.IncorrectAdUnit);
                            }
                            syncLock.countDown();
                        }
                    });
                    try {
                        syncLock.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    private static boolean finalizeInitialization(@Nullable TapjoyInitializeListener listener) {
        if (Tapjoy.isLimitedConnected()) {
            if (listener != null) {
                listener.onInitialized();
            }
            return true;
        }
        return false;
    }

    private interface TapjoyInitializeListener {
        void onInitialized();

        void onInitializationFail(BMError error);
    }

}
