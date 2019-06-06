package io.bidmachine.models;

import io.bidmachine.banner.BannerSize;

public interface IBannerRequestBuilder<SelfType> {

    SelfType setSize(BannerSize bannerSize);

}
