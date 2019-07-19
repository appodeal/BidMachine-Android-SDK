package io.bidmachine.adapters.mraid;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import io.bidmachine.unified.UnifiedFullscreenAd;
import io.bidmachine.unified.UnifiedFullscreenAdCallback;
import io.bidmachine.unified.UnifiedFullscreenAdRequestParams;
import io.bidmachine.unified.UnifiedMediationParams;
import io.bidmachine.utils.BMError;
import org.nexage.sourcekit.mraid.MRAIDInterstitial;
import org.nexage.sourcekit.util.Video;

import java.util.Map;

import static io.bidmachine.core.Utils.onUiThread;

class MraidFullScreenAd implements UnifiedFullscreenAd {

    private Video.Type videoType;
    private MRAIDInterstitial mraidInterstitial;
    private MraidActivity showingActivity;
    private MraidFullScreenAdapterListener adapterListener;
    @Nullable
    private UnifiedFullscreenAdCallback callback;
    @Nullable
    private MraidParams mraidParams;
    private int skipAfterTimeSec;

    MraidFullScreenAd(Video.Type videoType) {
        this.videoType = videoType;
    }

    @Override
    public void load(@NonNull final Context context,
                     @NonNull UnifiedFullscreenAdCallback callback,
                     @NonNull UnifiedFullscreenAdRequestParams requestParams,
                     @NonNull UnifiedMediationParams mediationParams,
                     @Nullable Map<String, Object> localExtra) {
        mraidParams = new MraidParams(mediationParams);
        if (!mraidParams.isValid(callback)) {
            return;
        }
        this.callback = callback;
        skipAfterTimeSec = mediationParams.getInt("skipAfterTimeSec");
        adapterListener = new MraidFullScreenAdapterListener(this, callback);
        onUiThread(new Runnable() {
            @Override
            public void run() {
                mraidInterstitial = new MRAIDInterstitial.builder(
                        context, mraidParams.creativeAdm, mraidParams.width, mraidParams.height
                ).setPreload(mraidParams.canPreload)
                        .setListener(adapterListener)
                        .setNativeFeatureListener(adapterListener)
                        .build();
            }
        });
    }

    @Override
    public void show(@NonNull Context context,
                     @NonNull UnifiedFullscreenAdCallback callback) {
        if (mraidInterstitial != null && mraidInterstitial.isReady) {
            MraidActivity.show(context, this, videoType);
        } else {
            callback.onAdShowFailed(BMError.NotLoaded);
        }
    }

    @Override
    public void onDestroy() {
        if (mraidInterstitial != null) {
            mraidInterstitial = null;
        }
    }

    MRAIDInterstitial getMraidInterstitial() {
        return mraidInterstitial;
    }

    MraidActivity getShowingActivity() {
        return showingActivity;
    }

    MraidFullScreenAdapterListener getAdapterListener() {
        return adapterListener;
    }

    void setShowingActivity(MraidActivity showingActivity) {
        this.showingActivity = showingActivity;
    }

    boolean canPreload() {
        return mraidParams != null && mraidParams.canPreload;
    }

    int getSkipAfterTimeSec() {
        return skipAfterTimeSec;
    }

    @Nullable
    public UnifiedFullscreenAdCallback getCallback() {
        return callback;
    }

}