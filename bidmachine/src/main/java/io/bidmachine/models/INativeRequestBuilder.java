package io.bidmachine.models;

import android.support.annotation.NonNull;

import io.bidmachine.MediaAssetType;

public interface INativeRequestBuilder<SelfType extends INativeRequestBuilder> {

    SelfType setMediaAssetTypes(@NonNull MediaAssetType... types);

}
