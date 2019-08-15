package io.bidmachine.unified;

import android.support.annotation.NonNull;
import io.bidmachine.nativead.utils.NativeData;

public interface UnifiedNativeAdCallback extends UnifiedAdCallback {

    void onAdLoaded(@NonNull NativeData nativeData);

}
