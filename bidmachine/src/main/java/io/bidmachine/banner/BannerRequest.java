package io.bidmachine.banner;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import io.bidmachine.AdRequest;
import io.bidmachine.AdsType;
import io.bidmachine.R;
import io.bidmachine.models.IBannerRequestBuilder;
import io.bidmachine.unified.UnifiedBannerAdRequestParams;
import io.bidmachine.utils.BMError;

public final class BannerRequest extends AdRequest<BannerRequest, UnifiedBannerAdRequestParams> {

    private BannerSize bannerSize;

    private BannerRequest() {
        super(AdsType.Banner);
    }

    public BannerSize getSize() {
        return bannerSize;
    }

    @Override
    protected BMError verifyRequest() {
        if (bannerSize == null) {
            return BMError.paramError("BannerSize not provided");
        }
        return super.verifyRequest();
    }

    @Override
    public UnifiedBannerAdRequestParams getUnifiedRequestParams() {
        return new BannerUnifiedRequestParams();
    }

    public static final class Builder extends AdRequestBuilderImpl<Builder, BannerRequest>
            implements IBannerRequestBuilder<Builder> {

        public Builder() {
        }

        Builder(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
            TypedArray params = context.obtainStyledAttributes(attrs, R.styleable.BannerRequest, defStyleAttr, 0);
            setSize(BannerSize.values()[params.getInt(R.styleable.BannerRequest_bannerSize, 0)]);
            params.recycle();
        }

        @Override
        protected BannerRequest createRequest() {
            return new BannerRequest();
        }

        @Override
        public Builder setSize(BannerSize bannerSize) {
            prepareRequest();
            params.bannerSize = bannerSize;
            return this;
        }
    }

    public interface AdRequestListener extends AdRequest.AdRequestListener<BannerRequest> {
    }

    private class BannerUnifiedRequestParams extends BaseUnifiedRequestParams implements UnifiedBannerAdRequestParams {

        @Override
        public BannerSize getBannerSize() {
            return BannerRequest.this.getSize();
        }
    }

}
