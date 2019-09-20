package io.bidmachine;

import android.support.annotation.NonNull;

public interface HeaderBiddingAdRequestParams {

    @NonNull
    AdsType getAdsType();

    @NonNull
    AdContentType getAdContentType();
}
