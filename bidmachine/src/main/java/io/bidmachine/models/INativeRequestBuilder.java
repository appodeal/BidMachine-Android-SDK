package io.bidmachine.models;

import android.support.annotation.NonNull;

import io.bidmachine.MediaAssetType;

public interface INativeRequestBuilder<SelfType extends INativeRequestBuilder> {

    @SuppressWarnings("UnusedReturnValue")
    SelfType setMediaAssetTypes(@NonNull MediaAssetType... types);

}
