package io.bidmachine.ads.networks.facebook;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import com.facebook.ads.Ad;
import com.facebook.ads.AdSize;
import com.facebook.ads.AdView;
import io.bidmachine.ContextProvider;
import io.bidmachine.unified.UnifiedBannerAd;
import io.bidmachine.unified.UnifiedBannerAdCallback;
import io.bidmachine.unified.UnifiedBannerAdRequestParams;
import io.bidmachine.unified.UnifiedMediationParams;

class FacebookBanner extends UnifiedBannerAd {

    @Nullable
    private AdView adView;

    @Override
    public void load(@NonNull ContextProvider context,
                     @NonNull UnifiedBannerAdCallback callback,
                     @NonNull UnifiedBannerAdRequestParams requestParams,
                     @NonNull UnifiedMediationParams mediationParams) throws Throwable {
        FacebookParams params = new FacebookParams(mediationParams);
        if (!params.isValid(callback)) {
            return;
        }
        AdSize adSize;
        switch (requestParams.getBannerSize()) {
            case Size_728x90: {
                adSize = AdSize.BANNER_HEIGHT_90;
                break;
            }
            case Size_300x250: {
                adSize = AdSize.RECTANGLE_HEIGHT_250;
                break;
            }
            default: {
                adSize = AdSize.BANNER_HEIGHT_50;
                break;
            }
        }
        adView = new AdView(context.getContext(), params.placementId, adSize);
        adView.setAdListener(new FacebookListener(callback));
        adView.loadAdFromBid(params.bidPayload);
    }

    @Override
    public void onDestroy() {
        if (adView != null) {
            adView.destroy();
            adView = null;
        }
    }

    private static final class FacebookListener extends BaseFacebookListener<UnifiedBannerAdCallback> {

        FacebookListener(@NonNull UnifiedBannerAdCallback callback) {
            super(callback);
        }

        @Override
        public void onAdLoaded(Ad ad) {
            getCallback().onAdLoaded((View) ad);
        }

    }

}
