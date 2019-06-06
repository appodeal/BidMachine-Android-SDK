package io.bidmachine.banner;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import io.bidmachine.AdView;
import io.bidmachine.ViewAdObject;
import io.bidmachine.models.RequestBuilder;

public class BannerView extends AdView<
        BannerView,
        BannerAd,
        BannerRequest,
        ViewAdObject<BannerAd>,
        BannerListener> {

    public BannerView(@NonNull Context context) {
        super(context);
    }

    public BannerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public BannerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected RequestBuilder<? extends RequestBuilder, BannerRequest> createAdRequest(
            @NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        return new BannerRequest.Builder(context, attrs, defStyleAttr);
    }

    @Override
    protected BannerAd createAd(Context context) {
        return new BannerAd(context);
    }

}