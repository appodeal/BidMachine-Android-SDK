package io.bidmachine.banner;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import io.bidmachine.AdListener;
import io.bidmachine.AdsType;
import io.bidmachine.ViewAd;
import io.bidmachine.ViewAdObject;

@VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
public final class BannerAd extends ViewAd<
        BannerAd,
        BannerRequest,
        ViewAdObject<BannerAd>,
        AdListener<BannerAd>> {

    @VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
    public BannerAd(Context context) {
        super(context);
    }

    @NonNull
    @Override
    protected AdsType getType() {
        return AdsType.Banner;
    }

}
