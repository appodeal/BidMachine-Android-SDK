package io.bidmachine.adapters.adcolony;

import android.support.annotation.NonNull;
import io.bidmachine.AdsFormat;
import io.bidmachine.NetworkConfig;

import java.util.HashMap;

public class AdColonyNetworkConfig extends NetworkConfig {

    static final String KEY_APP_ID = "app_id";
    static final String KEY_ZONE_ID = "zone_id";
    static final String KEY_STORE_ID = "store_id";

    @NonNull
    private final String appId;
    @NonNull
    private final String storeId;

    public AdColonyNetworkConfig(@NonNull String appId) {
        this(appId, "google");
    }

    @SuppressWarnings("WeakerAccess")
    public AdColonyNetworkConfig(@NonNull String appId, @NonNull String storeId) {
        super(new AdColonyAdapter());
        this.appId = appId;
        this.storeId = storeId;
    }

    public AdColonyNetworkConfig withMediationParams(@NonNull AdsFormat adsFormat,
                                                     @NonNull String zoneId) {
        return withMediationParams(adsFormat, appId, zoneId);
    }

    @SuppressWarnings("WeakerAccess")
    public AdColonyNetworkConfig withMediationParams(@NonNull AdsFormat adsFormat,
                                                     @NonNull String appId,
                                                     @NonNull String zoneId) {
        return withMediationParams(adsFormat, appId, zoneId, storeId);
    }

    @SuppressWarnings("WeakerAccess")
    public AdColonyNetworkConfig withMediationParams(@NonNull AdsFormat adsFormat,
                                                     @NonNull final String appId,
                                                     @NonNull final String zoneId,
                                                     @NonNull final String storeId) {
        return withMediationConfig(adsFormat, new HashMap<String, String>() {{
            put(KEY_APP_ID, appId);
            put(KEY_ZONE_ID, zoneId);
            put(KEY_STORE_ID, storeId);
        }});
    }

}
