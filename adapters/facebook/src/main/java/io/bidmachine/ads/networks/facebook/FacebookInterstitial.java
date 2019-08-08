package io.bidmachine.ads.networks.facebook;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.facebook.ads.Ad;
import com.facebook.ads.InterstitialAd;
import com.facebook.ads.InterstitialAdListener;
import io.bidmachine.ContextProvider;
import io.bidmachine.unified.UnifiedFullscreenAd;
import io.bidmachine.unified.UnifiedFullscreenAdCallback;
import io.bidmachine.unified.UnifiedFullscreenAdRequestParams;
import io.bidmachine.unified.UnifiedMediationParams;
import io.bidmachine.utils.BMError;

class FacebookInterstitial extends UnifiedFullscreenAd {

    @Nullable
    private InterstitialAd interstitialAd;

    @Override
    public void load(@NonNull ContextProvider context,
                     @NonNull UnifiedFullscreenAdCallback callback,
                     @NonNull UnifiedFullscreenAdRequestParams requestParams,
                     @NonNull UnifiedMediationParams mediationParams) throws Throwable {
        FacebookParams params = new FacebookParams(mediationParams);
        if (!params.isValid(callback)) {
            return;
        }
        interstitialAd = new InterstitialAd(context.getContext(), params.placementId);
        interstitialAd.setAdListener(new FacebookListener(callback));
        interstitialAd.loadAdFromBid(params.bidPayload);
    }

    @Override
    public void show(@NonNull Context context, @NonNull UnifiedFullscreenAdCallback callback) {
        if (interstitialAd != null && interstitialAd.isAdLoaded() && !interstitialAd.isAdInvalidated()) {
            interstitialAd.show();
        } else {
            callback.onAdShowFailed(BMError.NotLoaded);
        }
    }

    @Override
    public void onDestroy() {
        if (interstitialAd != null) {
            interstitialAd.destroy();
            interstitialAd = null;
        }
    }

    private static final class FacebookListener
            extends BaseFacebookListener<UnifiedFullscreenAdCallback>
            implements InterstitialAdListener {

        FacebookListener(@NonNull UnifiedFullscreenAdCallback callback) {
            super(callback);
        }

        @Override
        public void onInterstitialDisplayed(Ad ad) {
            getCallback().onAdShown();
        }

        @Override
        public void onInterstitialDismissed(Ad ad) {
            getCallback().onAdClosed();
            ad.destroy();
        }

        @Override
        public void onAdLoaded(Ad ad) {
            getCallback().onAdLoaded();
        }
    }
}
