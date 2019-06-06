package io.bidmachine;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;

import io.bidmachine.core.Utils;

class AdvertisingPersonalData {

    private static String deviceAdvertisingId;
    private static boolean deviceAdvertisingIdWasGenerated;
    private static boolean limitAdTrackingEnabled = false;

    @VisibleForTesting
    public final static String DEFAULT_ADVERTISING_ID = "00000000-0000-0000-0000-000000000000";

    static boolean isLimitAdTrackingEnabled() {
        return limitAdTrackingEnabled;
    }

    static void setLimitAdTrackingEnabled(boolean limitAdTrackingEnabled) {
        AdvertisingPersonalData.limitAdTrackingEnabled = limitAdTrackingEnabled;
    }

    static void setDeviceAdvertisingId(String deviceAdvertisingId) {
        AdvertisingPersonalData.deviceAdvertisingId = deviceAdvertisingId;
    }

    @Nullable
    static String getAdvertisingId(Context context, boolean blocked) {
        if (blocked) {
            return DEFAULT_ADVERTISING_ID;
        } else if (AdvertisingPersonalData.deviceAdvertisingId == null) {
            deviceAdvertisingIdWasGenerated = true;
            String uuid = Utils.getAdvertisingUUID(context);
            if (uuid != null) {
                return uuid;
            }
            return DEFAULT_ADVERTISING_ID;
        } else {
            deviceAdvertisingIdWasGenerated = false;
            return AdvertisingPersonalData.deviceAdvertisingId;
        }
    }

    public static boolean isDeviceAdvertisingIdWasGenerated() {
        return deviceAdvertisingIdWasGenerated;
    }

}