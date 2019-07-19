package io.bidmachine.adapters.my_target;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.my.target.ads.MyTargetView;
import io.bidmachine.banner.BannerSize;
import io.bidmachine.unified.UnifiedBannerAd;
import io.bidmachine.unified.UnifiedBannerAdCallback;
import io.bidmachine.unified.UnifiedBannerAdRequestParams;
import io.bidmachine.unified.UnifiedMediationParams;
import io.bidmachine.utils.BMError;

import java.util.Map;

class MyTargetViewAd implements UnifiedBannerAd {

    @Nullable
    private MyTargetView adView;

    @Override
    public void load(@NonNull Context context,
                     @NonNull UnifiedBannerAdCallback callback,
                     @NonNull UnifiedBannerAdRequestParams requestParams,
                     @NonNull UnifiedMediationParams mediationParams,
                     @Nullable Map<String, Object> localExtra) {
        BannerSize size = requestParams.getBannerSize();
        int adSize;
        switch (size) {
            case Size_728_90: {
                adSize = MyTargetView.AdSize.BANNER_728x90;
                break;
            }
            case Size_300_250: {
                adSize = MyTargetView.AdSize.BANNER_300x250;
                break;
            }
            default: {
                adSize = MyTargetView.AdSize.BANNER_320x50;
                break;
            }
        }
        if (!mediationParams.contains("slot_id")) {
            callback.onAdLoadFailed(BMError.requestError("slot_id not provided"));
            return;
        }
        MyTargetParams params = new MyTargetParams(mediationParams);
        if (!params.isValid(callback)) {
            return;
        }
        adView = new MyTargetView(context);
        adView.init(params.slotId, adSize, false);
        adView.setListener(new MyTargetListener(callback));
        adView.loadFromBid(params.bidId);
    }

    @Override
    public void onDestroy() {
        if (adView != null) {
            adView.destroy();
            adView = null;
        }
    }

    private final class MyTargetListener implements MyTargetView.MyTargetViewListener {

        private UnifiedBannerAdCallback callback;

        MyTargetListener(UnifiedBannerAdCallback callback) {
            this.callback = callback;
        }

        @Override
        public void onLoad(@NonNull MyTargetView myTargetView) {
            callback.onAdLoaded(myTargetView);
        }

        @Override
        public void onNoAd(@NonNull String s, @NonNull MyTargetView myTargetView) {
            callback.onAdLoadFailed(BMError.noFillError(null));
        }

        @Override
        public void onShow(@NonNull MyTargetView myTargetView) {
            //ignore
        }

        @Override
        public void onClick(@NonNull MyTargetView myTargetView) {
            callback.onAdClicked();
        }
    }

}
