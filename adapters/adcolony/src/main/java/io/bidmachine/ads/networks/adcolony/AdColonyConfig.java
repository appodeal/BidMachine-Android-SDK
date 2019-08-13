package io.bidmachine.ads.networks.adcolony;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import io.bidmachine.AdsFormat;
import io.bidmachine.NetworkAdapter;
import io.bidmachine.NetworkConfig;

import java.util.HashMap;
import java.util.Map;

public class AdColonyConfig extends NetworkConfig {

    static final String KEY_APP_ID = "app_id";
    static final String KEY_ZONE_ID = "zone_id";
    static final String KEY_STORE_ID = "store_id";

    private static final String DEFAULT_STORE_ID = "google";

    public AdColonyConfig(@NonNull String appId) {
        this(appId, DEFAULT_STORE_ID);
    }

    @SuppressWarnings("WeakerAccess")
    public AdColonyConfig(@NonNull final String appId, @NonNull final String storeId) {
        super(new HashMap<String, String>() {{
            put(KEY_APP_ID, appId);
            put(KEY_STORE_ID, storeId);
        }});
    }

    @SuppressWarnings("unused")
    public AdColonyConfig(@Nullable Map<String, String> networkParams) {
        super(networkParams);
    }

    @NonNull
    @Override
    protected NetworkAdapter createNetworkAdapter() {
        return new AdColonyAdapter();
    }

    public AdColonyConfig withMediationConfig(@NonNull AdsFormat adsFormat,
                                              @NonNull String zoneId) {
        return withMediationConfig(adsFormat, null, zoneId);
    }

    @SuppressWarnings("WeakerAccess")
    public AdColonyConfig withMediationConfig(@NonNull AdsFormat adsFormat,
                                              @Nullable String appId,
                                              @NonNull String zoneId) {
        return withMediationConfig(adsFormat, appId, null, zoneId);
    }

    @SuppressWarnings("WeakerAccess")
    public AdColonyConfig withMediationConfig(@NonNull AdsFormat adsFormat,
                                              @Nullable final String appId,
                                              @Nullable final String storeId,
                                              @NonNull final String zoneId) {
        return withMediationConfig(adsFormat, new HashMap<String, String>() {{
            if (!TextUtils.isEmpty(appId)) {
                put(KEY_APP_ID, appId);
            }
            if (!TextUtils.isEmpty(storeId)) {
                put(KEY_STORE_ID, storeId);
            }
            put(KEY_ZONE_ID, zoneId);
        }});
    }
}
