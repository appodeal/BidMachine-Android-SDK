package io.bidmachine.adapters.my_target;

import android.support.annotation.NonNull;
import io.bidmachine.AdsFormat;
import io.bidmachine.NetworkConfig;

import java.util.HashMap;

public class MyTargetConfig extends NetworkConfig {

    static final String KEY_SLOT_ID = "slot_id";
    static final String KEY_BID_ID = "bid_id";

    public MyTargetConfig() {
        super(new MyTargetAdapter());
    }

    public MyTargetConfig withMediationConfig(@NonNull AdsFormat format, @NonNull final String slotId) {
        return withMediationConfig(format, new HashMap<String, Object>() {{
            put(KEY_SLOT_ID, slotId);
        }});
    }

}
