package io.bidmachine.nativead;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.bidmachine.AdRequest;
import io.bidmachine.AdsType;
import io.bidmachine.MediaAssetType;
import io.bidmachine.models.INativeRequestBuilder;

public final class NativeRequest extends AdRequest<NativeRequest> {

    private List<MediaAssetType> mediaAssetTypes = new ArrayList<>(MediaAssetType.values().length);

    public boolean containsAssetType(MediaAssetType assetType) {
        return mediaAssetTypes.isEmpty()
                || mediaAssetTypes.contains(assetType)
                || mediaAssetTypes.contains(MediaAssetType.All);
    }

    @NonNull
    @Override
    protected AdsType getType() {
        return AdsType.Native;
    }

    public static final class Builder extends AdRequestBuilderImpl<Builder, NativeRequest>
            implements INativeRequestBuilder<Builder> {

        @Override
        protected NativeRequest createRequest() {
            return new NativeRequest();
        }

        @Override
        public Builder setMediaAssetTypes(@NonNull MediaAssetType... types) {
            prepareRequest();
            params.mediaAssetTypes.clear();
            params.mediaAssetTypes.addAll(Arrays.asList(types));
            return this;
        }

    }

    public interface AdRequestListener extends AdRequest.AdRequestListener<NativeRequest> {
    }

}
