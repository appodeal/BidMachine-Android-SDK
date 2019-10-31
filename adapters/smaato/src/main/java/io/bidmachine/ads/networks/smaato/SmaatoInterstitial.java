package io.bidmachine.ads.networks.smaato;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.smaato.sdk.interstitial.EventListener;
import com.smaato.sdk.interstitial.Interstitial;
import com.smaato.sdk.interstitial.InterstitialAd;
import com.smaato.sdk.interstitial.InterstitialError;
import com.smaato.sdk.interstitial.InterstitialRequestError;

import java.lang.ref.WeakReference;

import io.bidmachine.ContextProvider;
import io.bidmachine.unified.UnifiedFullscreenAd;
import io.bidmachine.unified.UnifiedFullscreenAdCallback;
import io.bidmachine.unified.UnifiedFullscreenAdRequestParams;
import io.bidmachine.unified.UnifiedMediationParams;
import io.bidmachine.utils.BMError;

public class SmaatoInterstitial extends UnifiedFullscreenAd {

    private WeakReference<ContextProvider> contextProviderWeakReference;
    private InterstitialAdContainer interstitialAdContainer;

    @Override
    public void load(@NonNull ContextProvider context,
                     @NonNull UnifiedFullscreenAdCallback callback,
                     @NonNull UnifiedFullscreenAdRequestParams requestParams,
                     @NonNull UnifiedMediationParams mediationParams) throws Throwable {
        SmaatoParams smaatoParams = new SmaatoParams(mediationParams);
        if (!smaatoParams.isValid(callback)) {
            return;
        }
        assert smaatoParams.adSpaceId != null;
        contextProviderWeakReference = new WeakReference<>(context);
        interstitialAdContainer = new InterstitialAdContainer(callback);
        Interstitial.loadAd(smaatoParams.adSpaceId, interstitialAdContainer);
    }

    @Override
    public void show(@NonNull Context context, @NonNull UnifiedFullscreenAdCallback callback) {
        Activity activity = null;
        if (context instanceof Activity) {
            activity = (Activity) context;
        } else {
            if (contextProviderWeakReference != null) {
                ContextProvider contextProvider = contextProviderWeakReference.get();
                if (contextProvider != null) {
                    activity = contextProvider.getActivity();
                }
            }
        }
        if (activity == null) {
            callback.onAdShowFailed(BMError.Internal);
            return;
        }

        InterstitialAd interstitialAd = interstitialAdContainer.getAd();
        if (interstitialAd != null && interstitialAd.isAvailableForPresentation()) {
            interstitialAdContainer.setShown(true);
            interstitialAd.showAd(activity);
        } else {
            callback.onAdShowFailed(BMError.NotLoaded);
        }
    }

    @Override
    public void onDestroy() {
        if (contextProviderWeakReference != null) {
            contextProviderWeakReference = null;
        }
        if (interstitialAdContainer != null) {
            interstitialAdContainer.destroy();
            interstitialAdContainer = null;
        }
    }

    private static final class InterstitialAdContainer extends AdContainer<InterstitialAd> implements EventListener {

        private final UnifiedFullscreenAdCallback callback;

        InterstitialAdContainer(@NonNull UnifiedFullscreenAdCallback callback) {
            this.callback = callback;
        }

        @Override
        public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
            setAd(interstitialAd);
            callback.onAdLoaded();
        }

        @Override
        public void onAdFailedToLoad(@Nullable InterstitialRequestError interstitialRequestError) {
            onFailedToLoad(interstitialRequestError != null
                                   ? interstitialRequestError.getInterstitialError()
                                   : null);
        }

        @Override
        public void onAdError(@NonNull InterstitialAd interstitialAd,
                              @Nullable InterstitialError interstitialError) {
            if (isShown()) {
                callback.onAdShowFailed(BMError.Internal);
            } else {
                onFailedToLoad(interstitialError);
            }
        }

        @Override
        public void onAdOpened(@NonNull InterstitialAd interstitialAd) {
            callback.onAdShown();
        }

        @Override
        public void onAdClosed(@NonNull InterstitialAd interstitialAd) {
            callback.onAdClosed();
        }

        @Override
        public void onAdClicked(@NonNull InterstitialAd interstitialAd) {
            callback.onAdClicked();
        }

        @Override
        public void onAdImpression(@NonNull InterstitialAd interstitialAd) {
            //ignore because shown tracked by onAdOpened
        }

        @Override
        public void onAdTTLExpired(@NonNull InterstitialAd interstitialAd) {
            callback.onAdExpired();
        }

        private void onFailedToLoad(@Nullable InterstitialError interstitialError) {
            if (interstitialError != null) {
                switch (interstitialError) {
                    case NETWORK_ERROR:
                        callback.onAdLoadFailed(BMError.Connection);
                        break;
                    case INVALID_REQUEST:
                        callback.onAdLoadFailed(BMError.IncorrectAdUnit);
                        break;
                    case AD_UNLOADED:
                    case CREATIVE_RESOURCE_EXPIRED:
                    case INTERNAL_ERROR:
                        callback.onAdLoadFailed(BMError.Internal);
                        break;
                    default:
                        callback.onAdLoadFailed(BMError.noFillError(null));
                }
            } else {
                callback.onAdLoadFailed(BMError.noFillError(null));
            }
        }

    }

}