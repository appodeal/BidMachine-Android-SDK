package io.bidmachine.ads.networks.smaato;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import io.bidmachine.AdsFormat;
import io.bidmachine.NetworkAdapter;
import io.bidmachine.NetworkConfig;

import java.util.HashMap;
import java.util.Map;

public class SmaatoConfig extends NetworkConfig {

    static final String KEY_PUBLISHER_ID = "publisher_id";
    static final String KEY_AD_SPACE_ID = "ad_space_id";
    static final String KEY_BID_PRICE = "bid_price";

    public SmaatoConfig(@NonNull final String publisherId) {
        super(new HashMap<String, String>() {{
            put(KEY_PUBLISHER_ID, publisherId);
        }});
    }

    @SuppressWarnings("unused")
    public SmaatoConfig(@Nullable Map<String, String> networkConfig) {
        super(networkConfig);
    }

    @NonNull
    @Override
    protected NetworkAdapter createNetworkAdapter() {
        return new SmaatoAdapter();
    }

    public SmaatoConfig withMediationConfig(@NonNull AdsFormat format,
                                            @NonNull String adSpaceId) {
        return withMediationConfig(format, adSpaceId, null);
    }

    @SuppressWarnings("WeakerAccess")
    public SmaatoConfig withMediationConfig(@NonNull AdsFormat format,
                                            @NonNull final String adSpaceId,
                                            @Nullable final String publisherId) {
        return withMediationConfig(format, new HashMap<String, String>() {{
            put(KEY_AD_SPACE_ID, adSpaceId);
            if (!TextUtils.isEmpty(publisherId)) {
                put(KEY_PUBLISHER_ID, publisherId);
            }
        }});
    }

}
