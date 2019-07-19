package io.bidmachine.nativead;

import android.support.annotation.NonNull;
import io.bidmachine.AdRequest;
import io.bidmachine.AdsType;
import io.bidmachine.MediaAssetType;
import io.bidmachine.models.INativeRequestBuilder;
import io.bidmachine.unified.UnifiedNativeAdRequestParams;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class NativeRequest extends AdRequest<NativeRequest, UnifiedNativeAdRequestParams> {

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

    @Override
    public UnifiedNativeAdRequestParams getUnifiedRequestParams() {
        return new NativeUnifiedRequestParams();
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

    private class NativeUnifiedRequestParams extends BaseUnifiedRequestParams implements UnifiedNativeAdRequestParams {
        @Override
        public boolean containsAssetType(MediaAssetType assetType) {
            return NativeRequest.this.containsAssetType(assetType);
        }
    }

}
