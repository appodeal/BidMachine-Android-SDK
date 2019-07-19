package io.bidmachine.unified;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Map;

public interface UnifiedAd<
        UnifiedAdCallbackType extends UnifiedAdCallback,
        UnifiedAdRequestParamsType extends UnifiedAdRequestParams> {

    void load(@NonNull Context context,
              @NonNull UnifiedAdCallbackType callback,
              @NonNull UnifiedAdRequestParamsType requestParams,
              @NonNull UnifiedMediationParams mediationParams,
              @Nullable Map<String, Object> localExtra);

    void onDestroy();

}
