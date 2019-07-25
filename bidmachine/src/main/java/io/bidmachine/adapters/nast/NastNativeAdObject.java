package io.bidmachine.adapters.nast;

import android.support.annotation.NonNull;
import io.bidmachine.ContextProvider;
import io.bidmachine.unified.UnifiedMediationParams;
import io.bidmachine.unified.UnifiedNativeAd;
import io.bidmachine.unified.UnifiedNativeAdCallback;
import io.bidmachine.unified.UnifiedNativeAdRequestParams;
import io.bidmachine.utils.IabUtils;

class NastNativeAdObject implements UnifiedNativeAd {

    @Override
    public void load(@NonNull ContextProvider contextProvider,
                     @NonNull UnifiedNativeAdCallback callback,
                     @NonNull UnifiedNativeAdRequestParams adRequestParams,
                     @NonNull UnifiedMediationParams mediationParams) {
        callback.onAdLoaded(IabUtils.nativeDataFromMediationParams(mediationParams));
    }

    @Override
    public void onDestroy() {
        //ignore
    }
}