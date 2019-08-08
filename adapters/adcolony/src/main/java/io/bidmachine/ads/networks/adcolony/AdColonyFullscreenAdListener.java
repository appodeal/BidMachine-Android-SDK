package io.bidmachine.ads.networks.adcolony;

import android.support.annotation.NonNull;
import com.adcolony.sdk.*;
import io.bidmachine.unified.UnifiedFullscreenAdCallback;
import io.bidmachine.utils.BMError;

final class AdColonyFullscreenAdListener extends AdColonyInterstitialListener implements AdColonyRewardListener {

    @NonNull
    private String zoneId;
    @NonNull
    private AdColonyFullscreenAd adColonyFullscreenAd;
    @NonNull
    private UnifiedFullscreenAdCallback callback;
    private boolean isLoaded;
    private boolean isShown;

    AdColonyFullscreenAdListener(@NonNull String zoneId,
                                 @NonNull AdColonyFullscreenAd adColonyFullscreenAd,
                                 @NonNull UnifiedFullscreenAdCallback callback) {
        this.zoneId = zoneId;
        this.adColonyFullscreenAd = adColonyFullscreenAd;
        this.callback = callback;
    }

    @Override
    public void onRequestFilled(AdColonyInterstitial adColonyInterstitial) {
        isLoaded = true;
        adColonyFullscreenAd.setAdColonyInterstitial(adColonyInterstitial);
        callback.onAdLoaded();
    }

    @Override
    public void onRequestNotFilled(AdColonyZone zone) {
        callback.onAdLoadFailed(BMError.NoContent);
    }

    @Override
    public void onOpened(AdColonyInterstitial ad) {
        isShown = true;
        callback.onAdShown();
    }

    @Override
    public void onClicked(AdColonyInterstitial ad) {
        callback.onAdClicked();
    }

    @Override
    public void onClosed(AdColonyInterstitial ad) {
        callback.onAdClosed();
    }

    @Override
    public void onExpiring(AdColonyInterstitial ad) {
        callback.onAdExpired();
    }

    @Override
    public void onReward(AdColonyReward adColonyReward) {
        if (adColonyReward == null || adColonyReward.success()) {
            if (isShown) {
                callback.onAdFinished();
            } else if (isLoaded) {
                // Since we can request and fill same zone multiple time, we should expire all other states to prevent
                // errors
                callback.onAdExpired();
            }
        }
    }

    @NonNull
    String getZoneId() {
        return zoneId;
    }
}