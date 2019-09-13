package io.bidmachine;

import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Calendar;

import io.bidmachine.core.DeviceInfo;
import io.bidmachine.models.DataRestrictions;
import io.bidmachine.models.TargetingInfo;
import io.bidmachine.utils.Gender;

class TargetingInfoImpl implements TargetingInfo {

    @NonNull
    private DataRestrictions dataRestrictions;
    @NonNull
    private TargetingParams targetingParams;

    TargetingInfoImpl(@NonNull DataRestrictions dataRestrictions,
                      @NonNull TargetingParams targetingParams) {
        this.dataRestrictions = dataRestrictions;
        this.targetingParams = targetingParams;
    }

    @Nullable
    @Override
    public String getUserId() {
        if (dataRestrictions.canSendUserInfo()) {
            return targetingParams.getUserId();
        }
        return null;
    }

    @Nullable
    @Override
    public Gender getGender() {
        if (dataRestrictions.canSendUserInfo()) {
            return targetingParams.getGender();
        }
        return null;
    }

    @Nullable
    @Override
    public Integer getUserBirthdayYear() {
        if (dataRestrictions.canSendUserInfo()) {
            return targetingParams.getBirthdayYear();
        }
        return null;
    }

    @Nullable
    public Integer getUserAge() {
        Integer birthdayYear = getUserBirthdayYear();
        if (birthdayYear != null) {
            int currentYear = Calendar.getInstance().get(Calendar.YEAR);
            return currentYear - birthdayYear;
        }
        return null;
    }

    @Nullable
    @Override
    public String[] getKeywords() {
        if (dataRestrictions.canSendUserInfo()) {
            return targetingParams.getKeywords();
        }
        return null;
    }

    @Nullable
    @Override
    public Location getDeviceLocation() {
        if (dataRestrictions.canSendGeoPosition()) {
            return targetingParams.getDeviceLocation();
        }
        return null;
    }

    @Nullable
    @Override
    public String getCountry() {
        if (dataRestrictions.canSendGeoPosition()) {
            return targetingParams.getCountry();
        }
        return null;
    }

    @Nullable
    @Override
    public String getCity() {
        if (dataRestrictions.canSendGeoPosition()) {
            return targetingParams.getCity();
        }
        return null;
    }

    @Nullable
    @Override
    public String getZip() {
        if (dataRestrictions.canSendGeoPosition()) {
            return targetingParams.getZip();
        }
        return null;
    }

    @Nullable
    @Override
    public String getStoreUrl() {
        return targetingParams.getStoreUrl();
    }

    @Nullable
    @Override
    public Boolean isPaid() {
        return targetingParams.getPaid();
    }

    @Override
    public String getHttpAgent(@NonNull android.content.Context context) {
        if (dataRestrictions.canSendDeviceInfo()) {
            return DeviceInfo.obtain(context).httpAgent;
        }
        return null;
    }

    @Override
    public String getIfa(@NonNull android.content.Context context) {
        return AdvertisingPersonalData.getAdvertisingId(context, !dataRestrictions.canSendIfa());
    }

    @Override
    public boolean isLimitAdTrackingEnabled() {
        return AdvertisingPersonalData.isLimitAdTrackingEnabled();
    }
}