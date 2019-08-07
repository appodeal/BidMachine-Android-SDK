package io.bidmachine.test.app;

import android.support.annotation.NonNull;
import io.bidmachine.NetworkConfig;
import org.json.JSONObject;

import java.util.Arrays;

class OptionalNetwork {

    final int id;
    @NonNull
    final String displayName;
    @NonNull
    final NetworkConfig networkConfig;
    @NonNull
    final String jsonData;

    OptionalNetwork(int id,
                    @NonNull String displayName,
                    @NonNull NetworkConfig networkConfig,
                    @NonNull String jsonData) {
        this.id = id;
        this.displayName = displayName;
        this.networkConfig = networkConfig;
        this.jsonData = jsonData;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OptionalNetwork network = (OptionalNetwork) o;
        return id == network.id;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(new long[]{id});
    }
}
