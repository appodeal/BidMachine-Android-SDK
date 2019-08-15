package io.bidmachine.unified;

import android.content.Context;
import android.support.annotation.NonNull;

public abstract class UnifiedFullscreenAd extends UnifiedAd<UnifiedFullscreenAdCallback, UnifiedFullscreenAdRequestParams> {

    public abstract void show(@NonNull Context context,
                              @NonNull UnifiedFullscreenAdCallback callback);

    public void onFinished() {
    }

    public void onClosed(boolean finished) {
    }
}
