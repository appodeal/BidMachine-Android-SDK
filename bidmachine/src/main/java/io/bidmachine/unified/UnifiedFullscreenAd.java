package io.bidmachine.unified;

import android.content.Context;
import android.support.annotation.NonNull;

public interface UnifiedFullscreenAd extends UnifiedAd<UnifiedFullscreenAdCallback, UnifiedFullscreenAdRequestParams> {

    void show(@NonNull Context context, @NonNull UnifiedFullscreenAdCallback callback);
}
