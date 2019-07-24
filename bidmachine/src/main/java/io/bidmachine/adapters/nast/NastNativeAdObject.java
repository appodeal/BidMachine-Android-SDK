package io.bidmachine.adapters.nast;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import io.bidmachine.unified.UnifiedMediationParams;
import io.bidmachine.unified.UnifiedNativeAd;
import io.bidmachine.unified.UnifiedNativeAdCallback;
import io.bidmachine.unified.UnifiedNativeAdRequestParams;
import io.bidmachine.ContextProvider;
import io.bidmachine.utils.IabUtils;

import java.util.Map;

class NastNativeAdObject implements UnifiedNativeAd {

    @Override
    public void load(@NonNull ContextProvider contextProvider,
                     @NonNull UnifiedNativeAdCallback callback,
                     @NonNull UnifiedNativeAdRequestParams requestParams,
                     @NonNull UnifiedMediationParams mediationParams,
                     @Nullable Map<String, Object> localExtra) {
        callback.onAdLoaded(IabUtils.nativeDataFromMediationParams(mediationParams));
    }

    @Override
    public void onDestroy() {
        //ignore
    }
}