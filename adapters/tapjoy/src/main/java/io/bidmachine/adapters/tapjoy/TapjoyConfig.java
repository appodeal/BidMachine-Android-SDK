package io.bidmachine.adapters.tapjoy;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import io.bidmachine.AdsFormat;
import io.bidmachine.NetworkAdapter;
import io.bidmachine.NetworkConfig;

import java.util.HashMap;
import java.util.Map;

public class TapjoyConfig extends NetworkConfig {

    static final String KEY_SDK = "sdk_key";
    static final String KEY_PLACEMENT_NAME = "placement_name";
    static final String KEY_TOKEN = "token";

    @Nullable
    private final String sdkKey;

    public TapjoyConfig(@NonNull final String sdkKey) {
        super(new HashMap<String, String>() {{
            put(KEY_SDK, sdkKey);
        }});
        this.sdkKey = sdkKey;
    }

    @SuppressWarnings("unused")
    public TapjoyConfig(@Nullable Map<String, String> networkConfig) {
        super(networkConfig);
        if (networkConfig != null) {
            sdkKey = networkConfig.get(KEY_SDK);
        } else {
            sdkKey = null;
        }
    }

    @Override
    protected NetworkAdapter createNetworkAdapter() {
        return new TapjoyAdapter();
    }

    public TapjoyConfig withMediationConfig(@NonNull AdsFormat format,
                                            @NonNull String placementName) {
        return withMediationConfig(format, sdkKey, placementName);
    }

    @SuppressWarnings("WeakerAccess")
    public TapjoyConfig withMediationConfig(@NonNull AdsFormat format,
                                            @Nullable final String sdkKey,
                                            @NonNull final String placementName) {
        withMediationConfig(format, new HashMap<String, String>() {{
            if (!TextUtils.isEmpty(sdkKey)) {
                put(KEY_SDK, sdkKey);
            }
            put(KEY_PLACEMENT_NAME, placementName);
        }});
        return this;
    }

    @Override
    protected void onMediationConfigAdded(@NonNull AdsFormat adsFormat,
                                          @NonNull Map<String, String> config) {
        if (!config.containsKey(KEY_SDK) && !TextUtils.isEmpty(sdkKey)) {
            assert sdkKey != null;
            config.put(KEY_SDK, sdkKey);
        }
    }
}
