package io.bidmachine.test.app;

import android.support.annotation.NonNull;
import io.bidmachine.NetworkConfig;

import java.util.Arrays;

class OptionalNetwork {

    final int id;
    @NonNull
    final String displayName;
    @NonNull
    final NetworkConfig networkConfig;

    OptionalNetwork(int id, @NonNull String displayName, @NonNull NetworkConfig networkConfig) {
        this.id = id;
        this.displayName = displayName;
        this.networkConfig = networkConfig;
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
