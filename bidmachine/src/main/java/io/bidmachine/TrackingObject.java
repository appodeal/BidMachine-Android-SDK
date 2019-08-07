package io.bidmachine;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

public abstract class TrackingObject {

    public abstract Object getTrackingKey();

    @Nullable
    List<String> getTrackingUrls(@NonNull TrackEventType eventType) {
        return null;
    }

}