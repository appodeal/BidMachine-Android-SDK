package io.bidmachine.adapters.vast;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import io.bidmachine.ContextProvider;
import io.bidmachine.unified.UnifiedFullscreenAd;
import io.bidmachine.unified.UnifiedFullscreenAdCallback;
import io.bidmachine.unified.UnifiedFullscreenAdRequestParams;
import io.bidmachine.unified.UnifiedMediationParams;
import io.bidmachine.utils.BMError;
import io.bidmachine.utils.IabUtils;
import org.nexage.sourcekit.util.Video;
import org.nexage.sourcekit.vast.view.AppodealVASTPlayer;

class VastFullScreenAd implements UnifiedFullscreenAd {

    private Video.Type videoType;
    @Nullable
    private AppodealVASTPlayer vastPlayer;
    @Nullable
    private VastFullScreenAdapterListener vastListener;

    VastFullScreenAd(Video.Type videoType) {
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
        int skipAfterTimeSec = mediationParams.getInt(IabUtils.KEY_SKIP_AFTER_TIME_SEC);
        vastPlayer = new AppodealVASTPlayer(contextProvider.getContext());
        vastPlayer.setPrecache(true);
        vastPlayer.setCloseTime(skipAfterTimeSec);
        vastListener = new VastFullScreenAdapterListener(callback);
        vastPlayer.loadVideoWithData(creativeAdm, vastListener);
    }

    @Override
    public void show(@NonNull Context context, @NonNull UnifiedFullscreenAdCallback callback) {
        if (vastPlayer != null && vastPlayer.checkFile()) {
            vastPlayer.play(context, videoType, vastListener);
        } else {
            callback.onAdShowFailed(BMError.NotLoaded);
        }
    }

    @Override
    public void onDestroy() {
        if (vastPlayer != null) {
            vastPlayer = null;
        }
    }
}
