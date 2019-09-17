package io.bidmachine;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import io.bidmachine.models.DataRestrictions;
import io.bidmachine.models.DeviceInfo;

class DeviceInfoImpl implements DeviceInfo {

    @NonNull
    private final DataRestrictions dataRestrictions;

    DeviceInfoImpl(@NonNull DataRestrictions dataRestrictions) {
        this.dataRestrictions = dataRestrictions;
    }

    @Nullable
    @Override
    public String getHttpAgent(@NonNull android.content.Context context) {
        if (dataRestrictions.canSendDeviceInfo()) {
            return io.bidmachine.core.DeviceInfo.obtain(context).httpAgent;
        }
        return null;
    }

    @Nullable
    @Override
    public String getIfa(@NonNull android.content.Context context) {
        return AdvertisingPersonalData.getAdvertisingId(context, !dataRestrictions.canSendIfa());
    }

    @Override
    public boolean isLimitAdTrackingEnabled() {
        return AdvertisingPersonalData.isLimitAdTrackingEnabled();
    }
}
