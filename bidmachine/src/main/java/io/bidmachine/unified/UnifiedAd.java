package io.bidmachine.unified;

import android.support.annotation.NonNull;
import io.bidmachine.ContextProvider;

public interface UnifiedAd<
        UnifiedAdCallbackType extends UnifiedAdCallback,
        UnifiedAdRequestParamsType extends UnifiedAdRequestParams> {

    void load(@NonNull ContextProvider context,
              @NonNull UnifiedAdCallbackType callback,
              @NonNull UnifiedAdRequestParamsType requestParams,
              @NonNull UnifiedMediationParams mediationParams);

    void onDestroy();

}
