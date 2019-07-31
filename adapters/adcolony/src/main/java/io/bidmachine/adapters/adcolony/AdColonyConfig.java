package io.bidmachine.adapters.adcolony;

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

    @Nullable
    private final String appId;
    @Nullable
    private final String storeId;

    public AdColonyConfig(@NonNull String appId) {
        this(appId, DEFAULT_STORE_ID);
    }

    @SuppressWarnings("WeakerAccess")
    public AdColonyConfig(@NonNull final String appId, @NonNull String storeId) {
        super(new HashMap<String, String>() {{
            put(KEY_APP_ID, appId);
        }});
        this.appId = appId;
        this.storeId = storeId;
    }

    @SuppressWarnings("unused")
    public AdColonyConfig(@Nullable Map<String, String> networkConfig) {
        super(networkConfig);
        if (networkConfig != null) {
            appId = networkConfig.get(KEY_APP_ID);
            storeId = networkConfig.containsKey(KEY_STORE_ID)
                    ? networkConfig.get(KEY_STORE_ID) : DEFAULT_STORE_ID;
        } else {
            appId = null;
            storeId = DEFAULT_STORE_ID;
        }
    }

    @Override
    protected NetworkAdapter createNetworkAdapter() {
        return new AdColonyAdapter();
    }

    public AdColonyConfig withMediationParams(@NonNull AdsFormat adsFormat,
                                              @NonNull String zoneId) {
        return withMediationParams(adsFormat, appId, zoneId);
    }

    @SuppressWarnings("WeakerAccess")
    public AdColonyConfig withMediationParams(@NonNull AdsFormat adsFormat,
                                              @Nullable String appId,
                                              @NonNull String zoneId) {
        return withMediationParams(adsFormat, appId, zoneId, storeId);
    }

    @SuppressWarnings("WeakerAccess")
    public AdColonyConfig withMediationParams(@NonNull AdsFormat adsFormat,
                                              @Nullable final String appId,
                                              @NonNull final String zoneId,
                                              @Nullable final String storeId) {
        return withMediationConfig(adsFormat, new HashMap<String, String>() {{
            if (TextUtils.isEmpty(appId)) {
                put(KEY_APP_ID, appId);
            }
            put(KEY_ZONE_ID, zoneId);
            if (!TextUtils.isEmpty(storeId)) {
                put(KEY_STORE_ID, storeId);
            }
        }});
    }

    @Override
    protected void onMediationConfigAdded(@NonNull AdsFormat adsFormat,
                                          @NonNull Map<String, String> config) {
        if (!config.containsKey(KEY_APP_ID) && !TextUtils.isEmpty(appId)) {
            assert appId != null;
            config.put(KEY_APP_ID, appId);
        }
        if (!config.containsKey(KEY_STORE_ID) && !TextUtils.isEmpty(storeId)) {
            assert storeId != null;
            config.put(KEY_STORE_ID, storeId);
        }
    }
}
