package io.bidmachine.ads.networks.tapjoy;

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

    public TapjoyConfig(@NonNull final String sdkKey) {
        super(new HashMap<String, String>() {{
            put(KEY_SDK, sdkKey);
        }});
    }

    @SuppressWarnings("unused")
    public TapjoyConfig(@Nullable Map<String, String> networkConfig) {
        super(networkConfig);
    }

    @NonNull
    @Override
    protected NetworkAdapter createNetworkAdapter() {
        return new TapjoyAdapter();
    }

    public TapjoyConfig withMediationConfig(@NonNull AdsFormat format,
                                            @NonNull String placementName) {
        return withMediationConfig(format, null, placementName);
    }

    @SuppressWarnings("WeakerAccess")
    public TapjoyConfig withMediationConfig(@NonNull AdsFormat format,
                                            @Nullable final String sdkKey,
                                            @NonNull final String placementName) {
        return withMediationConfig(format, new HashMap<String, String>() {{
            if (!TextUtils.isEmpty(sdkKey)) {
                put(KEY_SDK, sdkKey);
            }
            put(KEY_PLACEMENT_NAME, placementName);
        }});
    }

}
