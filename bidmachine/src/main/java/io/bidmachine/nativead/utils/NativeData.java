package io.bidmachine.nativead.utils;

import android.support.annotation.Nullable;

public interface NativeData extends NativePublicData {
    @Nullable
    String getIconUrl();

    @Nullable
    String getImageUrl();

    @Nullable
    String getClickUrl();

    @Nullable
    String getVideoUrl();

    @Nullable
    String getVideoAdm();
}
