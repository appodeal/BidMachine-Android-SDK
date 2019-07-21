package io.bidmachine.models;

import android.support.annotation.NonNull;

public abstract class RequestParams<SelfType extends RequestParams<SelfType>> {

    public abstract void merge(@NonNull SelfType instance);
}
