package io.bidmachine.ads.networks.smaato;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.smaato.sdk.banner.ad.AutoReloadInterval;
import com.smaato.sdk.banner.ad.BannerAdSize;
import com.smaato.sdk.banner.widget.BannerError;
import com.smaato.sdk.banner.widget.BannerView;

import io.bidmachine.ContextProvider;
import io.bidmachine.unified.UnifiedBannerAd;
import io.bidmachine.unified.UnifiedBannerAdCallback;
import io.bidmachine.unified.UnifiedBannerAdRequestParams;
import io.bidmachine.unified.UnifiedMediationParams;
import io.bidmachine.utils.BMError;

public class SmaatoBanner extends UnifiedBannerAd {

    private BannerView bannerView;

    @Override
    public void load(@NonNull ContextProvider context,
                     @NonNull UnifiedBannerAdCallback callback,
                     @NonNull UnifiedBannerAdRequestParams requestParams,
                     @NonNull UnifiedMediationParams mediationParams) throws Throwable {
        SmaatoParams smaatoParams = new SmaatoParams(mediationParams);
        if (!smaatoParams.isValid(callback)) {
            return;
        }
        assert smaatoParams.adSpaceId != null;
        BannerAdSize bannerAdSize;
        switch (requestParams.getBannerSize()) {
            case Size_300x250:
                bannerAdSize = BannerAdSize.MEDIUM_RECTANGLE_300x250;
                break;
            case Size_728x90:
                bannerAdSize = BannerAdSize.LEADERBOARD_728x90;
                break;
            default:
                bannerAdSize = BannerAdSize.XX_LARGE_320x50;
        }
        bannerView = new BannerView(context.getContext());
        bannerView.setEventListener(new Listener(callback));
        bannerView.setAutoReloadInterval(AutoReloadInterval.DISABLED);
        bannerView.loadAd(smaatoParams.adSpaceId, bannerAdSize);
    }

    @Override
    public void onDestroy() {
        if (bannerView != null) {
            bannerView.setEventListener(null);
            bannerView.destroy();
            bannerView = null;
        }
    }

    private static final class Listener implements BannerView.EventListener {

        private final UnifiedBannerAdCallback callback;

        Listener(@NonNull UnifiedBannerAdCallback callback) {
            this.callback = callback;
        }

        @Override
        public void onAdLoaded(@NonNull BannerView bannerView) {
            callback.onAdLoaded(bannerView);
        }

        @Override
        public void onAdFailedToLoad(@NonNull BannerView bannerView,
                                     @Nullable BannerError bannerError) {
            if (bannerError != null) {
                switch (bannerError) {
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

        @Override
        public void onAdImpression(@NonNull BannerView bannerView) {
            //ignore
        }

        @Override
        public void onAdClicked(@NonNull BannerView bannerView) {
            callback.onAdClicked();
        }

        @Override
        public void onAdTTLExpired(@NonNull BannerView bannerView) {
            callback.onAdExpired();
        }

    }

}