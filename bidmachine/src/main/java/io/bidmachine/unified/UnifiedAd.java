package io.bidmachine.unified;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import io.bidmachine.ContextProvider;

import java.util.Map;

public interface UnifiedAd<
        UnifiedAdCallbackType extends UnifiedAdCallback,
        UnifiedAdRequestParamsType extends UnifiedAdRequestParams> {

    void load(@NonNull ContextProvider context,
              @NonNull UnifiedAdCallbackType callback,
              @NonNull UnifiedAdRequestParamsType requestParams,
              @NonNull UnifiedMediationParams mediationParams,
              @Nullable Map<String, Object> localExtra);

    void onDestroy();

}
