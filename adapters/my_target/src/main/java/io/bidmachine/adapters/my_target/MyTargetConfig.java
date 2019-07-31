package io.bidmachine.adapters.my_target;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import io.bidmachine.AdsFormat;
import io.bidmachine.NetworkAdapter;
import io.bidmachine.NetworkConfig;

import java.util.HashMap;
import java.util.Map;

public class MyTargetConfig extends NetworkConfig {

    static final String KEY_SLOT_ID = "slot_id";
    static final String KEY_BID_ID = "bid_id";

    public MyTargetConfig() {
        this(null);
    }

    @SuppressWarnings("WeakerAccess")
    public MyTargetConfig(@Nullable Map<String, String> networkConfig) {
        super(networkConfig);
    }

    @Override
    protected NetworkAdapter createNetworkAdapter() {
        return new MyTargetAdapter();
    }

    public MyTargetConfig withMediationConfig(@NonNull AdsFormat format, @NonNull final String slotId) {
        return withMediationConfig(format, new HashMap<String, String>() {{
            put(KEY_SLOT_ID, slotId);
        }});
    }

}
