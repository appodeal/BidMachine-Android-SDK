package io.bidmachine.unified;

import android.support.annotation.Nullable;
import android.view.View;

public interface UnifiedBannerAdCallback extends UnifiedAdCallback {

    void onAdLoaded(@Nullable View adView);

}
