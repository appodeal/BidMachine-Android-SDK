package io.bidmachine.banner;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import io.bidmachine.*;
import io.bidmachine.models.AdObjectParams;
import io.bidmachine.unified.UnifiedBannerAd;
import io.bidmachine.unified.UnifiedBannerAdRequestParams;
import io.bidmachine.ContextProvider;

@VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
public final class BannerAd extends ViewAd<
        BannerAd,
        BannerRequest,
        ViewAdObject<BannerRequest, UnifiedBannerAd, UnifiedBannerAdRequestParams>,
        AdListener<BannerAd>> {

    @VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
    public BannerAd(@NonNull Context context) {
        super(context, AdsType.Banner);
    }

    @Override
    protected ViewAdObject<BannerRequest, UnifiedBannerAd, UnifiedBannerAdRequestParams> createAdObject(
            @NonNull ContextProvider contextProvider,
            @NonNull BannerRequest adRequest,
            @NonNull NetworkAdapter adapter,
            @NonNull AdObjectParams adObjectParams,
            @NonNull AdProcessCallback processCallback
    ) {
        UnifiedBannerAd unifiedAd = adapter.createBanner();
        if (unifiedAd == null) {
            return null;
        }
        ViewAdObject<BannerRequest, UnifiedBannerAd, UnifiedBannerAdRequestParams> adObject =
                new ViewAdObject<>(contextProvider, processCallback, adRequest, adObjectParams, unifiedAd);
        BannerSize bannerSize = adRequest.getSize();
        adObject.setWidth(bannerSize.width);
        adObject.setHeight(bannerSize.height);
        return adObject;
    }
}
