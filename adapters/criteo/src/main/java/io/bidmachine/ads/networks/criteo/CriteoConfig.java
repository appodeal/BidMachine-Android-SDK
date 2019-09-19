package io.bidmachine.ads.networks.criteo;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

import io.bidmachine.NetworkAdapter;
import io.bidmachine.NetworkConfig;

public class CriteoConfig extends NetworkConfig {

    static final String SENDER_ID = "sender_id";

    public CriteoConfig(final String senderId) {
        super(new HashMap<String, String>() {{
            put(SENDER_ID, senderId);
        }});
    }

    public CriteoConfig(@Nullable Map<String, String> networkParams) {
        super(networkParams);
    }

    @NonNull
    @Override
    protected NetworkAdapter createNetworkAdapter() {
        return new CriteoAdapter();
    }
}
