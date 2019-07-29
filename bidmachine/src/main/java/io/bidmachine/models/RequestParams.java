package io.bidmachine.models;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public abstract class RequestParams<SelfType extends RequestParams<SelfType>> {

    public abstract void merge(@NonNull SelfType instance);

    public static <T extends RequestParams<T>> T resolveParams(@Nullable T primary, @Nullable T secondary) {
        if (primary == null) {
            return secondary;
        }
        if (secondary != null) {
            primary.merge(secondary);
        }
        return primary;
    }
}
