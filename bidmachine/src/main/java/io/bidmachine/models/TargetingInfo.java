package io.bidmachine.models;

import android.content.Context;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import io.bidmachine.utils.Gender;

public interface TargetingInfo {

    @Nullable
    String getUserId();

    @Nullable
    Gender getGender();

    @Nullable
    Integer getUserBirthdayYear();

    @Nullable
    Integer getUserAge();

    @Nullable
    String[] getKeywords();

    @Nullable
    Location getDeviceLocation();

    @Nullable
    String getCountry();

    @Nullable
    String getCity();

    @Nullable
    String getZip();

    @Nullable
    String getStoreUrl();

    @Nullable
    Boolean isPaid();

    String getHttpAgent(@NonNull Context context);

    String getIfa(@NonNull Context context);

    boolean isLimitAdTrackingEnabled();
}