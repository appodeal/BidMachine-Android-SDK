package io.bidmachine.adapters.tapjoy;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import io.bidmachine.AdsFormat;
import io.bidmachine.NetworkConfig;

import java.util.HashMap;

public class TapjoyNetworkConfig extends NetworkConfig {

    static final String KEY_SDK = "sdk_key";
    static final String KEY_PLACEMENT_NAME = "placement_name";
    static final String KEY_TOKEN = "token";

    @NonNull
    private final String sdkKey;

    public TapjoyNetworkConfig(@NonNull final String sdkKey) {
        super(new TapjoyAdapter());
        this.sdkKey = sdkKey;
        withNetworkConfig(new HashMap<String, String>() {{
            put(KEY_SDK, sdkKey);
        }});
    }

    public TapjoyNetworkConfig withMediationConfig(@NonNull AdsFormat format, @NonNull String placementName) {
        return withMediationConfig(format, sdkKey, placementName);
    }

    @SuppressWarnings("WeakerAccess")
    public TapjoyNetworkConfig withMediationConfig(@NonNull AdsFormat format, @Nullable final String sdkKey, @NonNull final String placementName) {
        withMediationConfig(format, new HashMap<String, String>() {{
            if (!TextUtils.isEmpty(sdkKey)) {
                put(KEY_SDK, sdkKey);
            }
            put(KEY_PLACEMENT_NAME, placementName);
        }});
        return this;
    }

}
