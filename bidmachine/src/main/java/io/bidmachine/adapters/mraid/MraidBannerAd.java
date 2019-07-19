package io.bidmachine.adapters.mraid;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import io.bidmachine.unified.UnifiedBannerAd;
import io.bidmachine.unified.UnifiedBannerAdCallback;
import io.bidmachine.unified.UnifiedBannerAdRequestParams;
import io.bidmachine.unified.UnifiedMediationParams;
import io.bidmachine.utils.BMError;
import org.nexage.sourcekit.mraid.MRAIDView;

import java.util.Map;

import static io.bidmachine.core.Utils.onUiThread;

class MraidBannerAd implements UnifiedBannerAd {

    @Nullable
    MRAIDView mraidView;

    @Override
    public void load(@NonNull final Context context,
                     @NonNull UnifiedBannerAdCallback callback,
                     @NonNull UnifiedBannerAdRequestParams requestParams,
                     @NonNull UnifiedMediationParams mediationParams,
                     @Nullable Map<String, Object> localExtra) {
        final MraidParams mraidParams = new MraidParams(mediationParams);
        if (!mraidParams.isValid(callback)) {
            return;
        }
        final MraidBannerAdListener mraidBannerAdListener = new MraidBannerAdListener(this, callback);
        onUiThread(new Runnable() {
            @Override
            public void run() {
                mraidView = new MRAIDView.builder(context, mraidParams.creativeAdm, mraidParams.width, mraidParams.height)
                        .setListener(mraidBannerAdListener)
                        .setNativeFeatureListener(mraidBannerAdListener)
                        .setPreload(mraidParams.canPreload)
                        .build();
                mraidView.load();
            }
        });
    }

    void processMraidViewLoaded(@NonNull UnifiedBannerAdCallback callback) {
        if (mraidView != null && mraidView.getParent() == null) {
            mraidView.show();
            callback.onAdLoaded(mraidView);
        } else {
            callback.onAdLoadFailed(BMError.Internal);
        }
    }

    @Override
    public void onDestroy() {
        if (mraidView != null) {
            mraidView.destroy();
        }
    }

}