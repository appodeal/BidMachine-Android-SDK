package io.bidmachine.nativead;

import android.support.annotation.NonNull;
import io.bidmachine.AdRequest;
import io.bidmachine.AdsType;
import io.bidmachine.MediaAssetType;
import io.bidmachine.TargetingParams;
import io.bidmachine.models.DataRestrictions;
import io.bidmachine.models.INativeRequestBuilder;
import io.bidmachine.unified.UnifiedNativeAdRequestParams;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class NativeRequest extends AdRequest<NativeRequest, UnifiedNativeAdRequestParams> {

    private List<MediaAssetType> mediaAssetTypes = new ArrayList<>(MediaAssetType.values().length);

    @SuppressWarnings("WeakerAccess")
    public NativeRequest() {
        super(AdsType.Native);
    }

    @SuppressWarnings("WeakerAccess")
    public boolean containsAssetType(MediaAssetType assetType) {
        return mediaAssetTypes.isEmpty()
                || mediaAssetTypes.contains(assetType)
                || mediaAssetTypes.contains(MediaAssetType.All);
    }

    @NonNull
    @Override
    protected UnifiedNativeAdRequestParams createUnifiedAdRequestParams(@NonNull TargetingParams targetingParams,
                                                                        @NonNull DataRestrictions dataRestrictions) {
        return new NativeUnifiedRequestParams(targetingParams, dataRestrictions);
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

        NativeUnifiedRequestParams(@NonNull TargetingParams targetingParams,
                                   @NonNull DataRestrictions dataRestrictions) {
            super(targetingParams, dataRestrictions);
        }

        @Override
        public boolean containsAssetType(MediaAssetType assetType) {
            return NativeRequest.this.containsAssetType(assetType);
        }
    }

}
