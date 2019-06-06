package io.bidmachine.nativead.utils;

import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.Nullable;

public interface NativeMediaPublicData {

    @Nullable
    Uri getIconUri();

    @Nullable
    Bitmap getIconBitmap();

    @Nullable
    Uri getImageUri();

    @Nullable
    Bitmap getImageBitmap();

    @Nullable
    Uri getVideoUri();

    boolean hasVideo();

}
