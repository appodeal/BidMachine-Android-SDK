package io.bidmachine.models;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

import io.bidmachine.TrackEventType;

public interface AdObjectParams {

    String getCreativeId();

    String getCreativeAdm();

    @Nullable
    List<String> getTrackUrls(@NonNull TrackEventType eventType);

    int getWidth();

    int getHeight();

    boolean isValid();

    boolean canPreload();

}
