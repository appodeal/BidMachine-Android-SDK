package io.bidmachine.adapters.vast;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import com.explorestack.iab.vast.VastRequest;
import com.explorestack.iab.vast.VideoType;
import io.bidmachine.ContextProvider;
import io.bidmachine.unified.UnifiedFullscreenAd;
import io.bidmachine.unified.UnifiedFullscreenAdCallback;
import io.bidmachine.unified.UnifiedFullscreenAdRequestParams;
import io.bidmachine.unified.UnifiedMediationParams;
import io.bidmachine.utils.BMError;
import io.bidmachine.utils.IabUtils;

class VastFullScreenAd extends UnifiedFullscreenAd {

    @NonNull
    private VideoType videoType;
    @Nullable
    private VastRequest vastRequest;
    private VastFullScreenAdapterListener vastListener;

    VastFullScreenAd(@NonNull VideoType videoType) {
        this.videoType = videoType;
    }

    @Override
    public void load(@NonNull ContextProvider contextProvider,
                     @NonNull UnifiedFullscreenAdCallback callback,
                     @NonNull UnifiedFullscreenAdRequestParams requestParams,
                     @NonNull UnifiedMediationParams mediationParams) {
        final String creativeAdm = mediationParams.getString(IabUtils.KEY_CREATIVE_ADM);
        if (TextUtils.isEmpty(creativeAdm)) {
            callback.onAdLoadFailed(BMError.IncorrectAdUnit);
            return;
        }
        assert creativeAdm != null;
        int skipAfterTimeSec = mediationParams.getInt(IabUtils.KEY_SKIP_AFTER_TIME_SEC);
        vastListener = new VastFullScreenAdapterListener(callback);
        vastRequest = VastRequest.newBuilder()
                .setPreCache(true)
                .setCloseTime(skipAfterTimeSec)
                .build();
        assert vastRequest != null;
        vastRequest.loadVideoWithData(contextProvider.getContext(), creativeAdm, vastListener);
    }

    @Override
    public void show(@NonNull Context context, @NonNull UnifiedFullscreenAdCallback callback) {
        if (vastRequest != null && vastRequest.checkFile()) {
            vastRequest.display(context, videoType, vastListener);
        } else {
            callback.onAdShowFailed(BMError.NotLoaded);
        }
    }

    @Override
    public void onDestroy() {
        if (vastRequest != null) {
            vastRequest = null;
        }
    }

}
