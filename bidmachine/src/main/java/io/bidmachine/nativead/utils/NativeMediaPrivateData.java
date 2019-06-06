package io.bidmachine.nativead.utils;

import android.graphics.Bitmap;
import android.net.Uri;

import org.nexage.sourcekit.vast.model.VASTModel;

public interface NativeMediaPrivateData extends NativeMediaPublicData {

    void setIconUri(Uri uri);

    void setIconBitmap(Bitmap bitmap);

    void setImageUri(Uri uri);

    void setImageBitmap(Bitmap bitmap);

    void setVideoUri(Uri uri);

    void setVideoVastModel(VASTModel vastModel);

    VASTModel getVideoVastModel();

}
