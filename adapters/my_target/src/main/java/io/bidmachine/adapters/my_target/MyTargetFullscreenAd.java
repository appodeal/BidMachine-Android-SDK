package io.bidmachine.adapters.my_target;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.my.target.ads.InterstitialAd;
import io.bidmachine.unified.UnifiedFullscreenAd;
import io.bidmachine.unified.UnifiedFullscreenAdCallback;
import io.bidmachine.unified.UnifiedFullscreenAdRequestParams;
import io.bidmachine.unified.UnifiedMediationParams;
import io.bidmachine.utils.BMError;

import java.util.Map;

public class MyTargetFullscreenAd implements UnifiedFullscreenAd {

    @Nullable
    private InterstitialAd interstitialAd;

    @Override
    public void load(@NonNull Context context,
                     @NonNull UnifiedFullscreenAdCallback callback,
                     @NonNull UnifiedFullscreenAdRequestParams requestParams,
                     @NonNull UnifiedMediationParams mediationParams,
                     @Nullable Map<String, Object> localExtra) {
        MyTargetParams params = new MyTargetParams(mediationParams);
        if (!params.isValid(callback)) {
            return;
        }
        interstitialAd = new InterstitialAd(params.slotId, context);
        interstitialAd.setListener(new MyTargetFullscreenListener(callback));
        MyTargetAdapter.updateTargeting(requestParams, interstitialAd.getCustomParams());
        assert params.bidId != null; // it's shouldn't be null since we already check it in {@link MyTargetParams}
        interstitialAd.loadFromBid(params.bidId);
    }

    @Override
    public void show(@NonNull Context context, @NonNull UnifiedFullscreenAdCallback callback) {
        if (interstitialAd != null) {
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

    private final class MyTargetFullscreenListener implements InterstitialAd.InterstitialAdListener {

        private UnifiedFullscreenAdCallback callback;

        MyTargetFullscreenListener(UnifiedFullscreenAdCallback callback) {
            this.callback = callback;
        }

        @Override
        public void onLoad(@NonNull InterstitialAd interstitialAd) {
            callback.onAdLoaded();
        }

        @Override
        public void onNoAd(@NonNull String s, @NonNull InterstitialAd interstitialAd) {
            callback.onAdLoadFailed(BMError.noFillError(null));
        }

        @Override
        public void onClick(@NonNull InterstitialAd interstitialAd) {
            callback.onAdClicked();
        }

        @Override
        public void onDismiss(@NonNull InterstitialAd interstitialAd) {
            callback.onAdClosed();
        }

        @Override
        public void onVideoCompleted(@NonNull InterstitialAd interstitialAd) {
            callback.onAdFinished();
        }

        @Override
        public void onDisplay(@NonNull InterstitialAd interstitialAd) {
            callback.onAdShown();
        }
    }

}
