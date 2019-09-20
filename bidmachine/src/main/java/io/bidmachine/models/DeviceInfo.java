package io.bidmachine.models;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public interface DeviceInfo {

    @Nullable
    String getHttpAgent(@NonNull Context context);

    @Nullable
    String getIfa(@NonNull Context context);

    boolean isLimitAdTrackingEnabled();
}
