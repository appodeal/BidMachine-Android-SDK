package io.bidmachine.adapters.adcolony;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.adcolony.sdk.AdColony;
import com.adcolony.sdk.AdColonyInterstitial;
import io.bidmachine.unified.UnifiedFullscreenAd;
import io.bidmachine.unified.UnifiedFullscreenAdCallback;
import io.bidmachine.unified.UnifiedFullscreenAdRequestParams;
import io.bidmachine.unified.UnifiedMediationParams;
import io.bidmachine.utils.BMError;

import java.util.Map;

class AdColonyFullscreenAd implements UnifiedFullscreenAd {

    private AdColonyInterstitial adColonyInterstitial;
    private AdColonyFullscreenAdListener listener;
    private boolean isRewarded;

    AdColonyFullscreenAd(boolean rewarded) {
        isRewarded = rewarded;
    }

    @Override
    public void load(@NonNull Context context,
                     @NonNull UnifiedFullscreenAdCallback callback,
                     @NonNull UnifiedFullscreenAdRequestParams requestParams,
                     @NonNull UnifiedMediationParams mediationParams,
                     @Nullable Map<String, Object> localExtra) {
        String zoneId = mediationParams.getString("zone_id");
        listener = new AdColonyFullscreenAdListener(zoneId, this, callback);
        if (isRewarded) {
            AdColonyRewardListenerWrapper.get().addListener(listener);
        }
        AdColony.requestInterstitial(zoneId, listener, AdColonyAdapter.createAdOptions(requestParams));
    }

    @Override
    public void show(@NonNull Context context,
                     @NonNull UnifiedFullscreenAdCallback callback) {
        if (adColonyInterstitial != null && !adColonyInterstitial.isExpired()) {
            adColonyInterstitial.show();
        } else {
            callback.onAdShowFailed(BMError.NotLoaded);
        }
    }

    @Override
    public void onDestroy() {
        if (adColonyInterstitial != null) {
            if (isRewarded) {
                AdColonyRewardListenerWrapper.get().removeListener(listener);
            }
            adColonyInterstitial.destroy();
            adColonyInterstitial = null;
        }
    }

    void setAdColonyInterstitial(AdColonyInterstitial adColonyInterstitial) {
        this.adColonyInterstitial = adColonyInterstitial;
    }
}
