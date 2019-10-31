package io.bidmachine.ads.networks.smaato;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.smaato.sdk.rewarded.EventListener;
import com.smaato.sdk.rewarded.RewardedError;
import com.smaato.sdk.rewarded.RewardedInterstitial;
import com.smaato.sdk.rewarded.RewardedInterstitialAd;
import com.smaato.sdk.rewarded.RewardedRequestError;

import io.bidmachine.ContextProvider;
import io.bidmachine.unified.UnifiedFullscreenAd;
import io.bidmachine.unified.UnifiedFullscreenAdCallback;
import io.bidmachine.unified.UnifiedFullscreenAdRequestParams;
import io.bidmachine.unified.UnifiedMediationParams;
import io.bidmachine.utils.BMError;

public class SmaatoRewarded extends UnifiedFullscreenAd {

    private RewardedAdContainer rewardedAdContainer;

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
        rewardedAdContainer = new RewardedAdContainer(callback);
        RewardedInterstitial.loadAd(smaatoParams.adSpaceId, rewardedAdContainer);
    }

    @Override
    public void show(@NonNull Context context, @NonNull UnifiedFullscreenAdCallback callback) {
        RewardedInterstitialAd rewardedInterstitialAd = null;
        if (rewardedAdContainer != null) {
            rewardedInterstitialAd = rewardedAdContainer.getAd();
        }
        if (rewardedInterstitialAd != null && rewardedInterstitialAd.isAvailableForPresentation()) {
            rewardedAdContainer.setShown(true);
            rewardedInterstitialAd.showAd();
        } else {
            callback.onAdShowFailed(BMError.NotLoaded);
        }
    }

    @Override
    public void onDestroy() {
        if (rewardedAdContainer != null) {
            rewardedAdContainer.destroy();
            rewardedAdContainer = null;
        }
    }

    private static final class RewardedAdContainer extends AdContainer<RewardedInterstitialAd> implements EventListener {

        private final UnifiedFullscreenAdCallback callback;

        RewardedAdContainer(@NonNull UnifiedFullscreenAdCallback callback) {
            this.callback = callback;
        }

        @Override
        public void onAdLoaded(@NonNull RewardedInterstitialAd rewardedInterstitialAd) {
            setAd(rewardedInterstitialAd);
            callback.onAdLoaded();
        }

        @Override
        public void onAdFailedToLoad(@Nullable RewardedRequestError rewardedRequestError) {
            onFailedToLoad(rewardedRequestError != null
                                   ? rewardedRequestError.getRewardedError()
                                   : null);
        }

        @Override
        public void onAdError(@NonNull RewardedInterstitialAd rewardedInterstitialAd,
                              @Nullable RewardedError rewardedError) {
            if (isShown()) {
                callback.onAdShowFailed(BMError.Internal);
            } else {
                onFailedToLoad(rewardedError);
            }
        }

        @Override
        public void onAdClosed(@NonNull RewardedInterstitialAd rewardedInterstitialAd) {
            callback.onAdClosed();
        }

        @Override
        public void onAdClicked(@NonNull RewardedInterstitialAd rewardedInterstitialAd) {
            callback.onAdClicked();
        }

        @Override
        public void onAdStarted(@NonNull RewardedInterstitialAd rewardedInterstitialAd) {
            callback.onAdShown();
        }

        @Override
        public void onAdReward(@NonNull RewardedInterstitialAd rewardedInterstitialAd) {
            callback.onAdFinished();
        }

        @Override
        public void onAdTTLExpired(@NonNull RewardedInterstitialAd rewardedInterstitialAd) {
            callback.onAdExpired();
        }

        private void onFailedToLoad(@Nullable RewardedError rewardedError) {
            if (rewardedError != null) {
                switch (rewardedError) {
                    case NETWORK_ERROR:
                        callback.onAdLoadFailed(BMError.Connection);
                        break;
                    case INVALID_REQUEST:
                        callback.onAdLoadFailed(BMError.IncorrectAdUnit);
                        break;
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