package io.bidmachine.ads.networks.mraid;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.explorestack.iab.mraid.MRAIDInterstitial;
import com.explorestack.iab.vast.VideoType;
import io.bidmachine.ContextProvider;
import io.bidmachine.unified.UnifiedFullscreenAd;
import io.bidmachine.unified.UnifiedFullscreenAdCallback;
import io.bidmachine.unified.UnifiedFullscreenAdRequestParams;
import io.bidmachine.unified.UnifiedMediationParams;
import io.bidmachine.utils.BMError;

import static io.bidmachine.core.Utils.onUiThread;

class MraidFullScreenAd extends UnifiedFullscreenAd {

    private VideoType videoType;
    private MRAIDInterstitial mraidInterstitial;
    private MraidActivity showingActivity;
    private MraidFullScreenAdapterListener adapterListener;
    @Nullable
    private UnifiedFullscreenAdCallback callback;
    @Nullable
    private MraidParams mraidParams;
    private int skipAfterTimeSec;

    MraidFullScreenAd(VideoType videoType) {
        this.videoType = videoType;
    }

    @Override
    public void load(@NonNull final ContextProvider contextProvider,
                     @NonNull UnifiedFullscreenAdCallback callback,
                     @NonNull UnifiedFullscreenAdRequestParams requestParams,
                     @NonNull UnifiedMediationParams mediationParams) {
        final Activity activity = contextProvider.getActivity();
        if (activity == null) {
            BMError.requestError("Activity not provided");
            return;
        }
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
                mraidInterstitial = MRAIDInterstitial
                        .newBuilder(activity, mraidParams.creativeAdm, mraidParams.width, mraidParams.height)
                        .setPreload(mraidParams.canPreload)
                        .setListener(adapterListener)
                        .setNativeFeatureListener(adapterListener)
                        .build();
            }
        });
    }

    @Override
    public void show(@NonNull Context context,
                     @NonNull UnifiedFullscreenAdCallback callback) {
        if (mraidInterstitial != null && mraidInterstitial.isReady()) {
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