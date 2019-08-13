package io.bidmachine.ads.networks.facebook;

import android.support.annotation.Keep;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import io.bidmachine.AdsFormat;
import io.bidmachine.NetworkAdapter;
import io.bidmachine.NetworkConfig;

import java.util.HashMap;
import java.util.Map;

@Keep
public class FacebookConfig extends NetworkConfig {

    static final String KEY_APP_ID = "app_id";
    static final String KEY_PLACEMENT_ID = "facebook_key";
    static final String KEY_TOKEN = "token";
    static final String KEY_BID_PAYLOAD = "bid_payload";

    public FacebookConfig(@NonNull final String appId) {
        super(new HashMap<String, String>() {{
            put(KEY_APP_ID, appId);
        }});
    }

    public FacebookConfig(@Nullable Map<String, String> networkParams) {
        super(networkParams);
    }

    @NonNull
    @Override
    protected NetworkAdapter createNetworkAdapter() {
        return new FacebookAdapter();
    }

    public FacebookConfig withMediationConfig(@NonNull AdsFormat adsFormat,
                                              @NonNull String placementId) {
        return withMediationConfig(adsFormat, null, placementId);
    }

    @SuppressWarnings("WeakerAccess")
    public FacebookConfig withMediationConfig(@NonNull AdsFormat adsFormat,
                                              @Nullable final String appId,
                                              @NonNull final String placementId) {
        return withMediationConfig(adsFormat, new HashMap<String, String>() {{
            if (!TextUtils.isEmpty(appId)) {
                put(KEY_APP_ID, appId);
            }
            put(KEY_PLACEMENT_ID, placementId);
        }});
    }
}
