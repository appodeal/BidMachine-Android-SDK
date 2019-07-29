package io.bidmachine.adapters.vast;

import android.app.Activity;
import android.support.annotation.NonNull;
import io.bidmachine.unified.UnifiedFullscreenAdCallback;
import io.bidmachine.utils.BMError;
import org.nexage.sourcekit.util.Utils;
import org.nexage.sourcekit.vast.VASTPlayer;
import org.nexage.sourcekit.vast.activity.VASTActivity;

class VastFullScreenAdapterListener implements VASTPlayer.VASTPlayerListener {

    @NonNull
    private UnifiedFullscreenAdCallback callback;

    VastFullScreenAdapterListener(@NonNull UnifiedFullscreenAdCallback callback) {
        this.callback = callback;
    }

    @Override
    public void vastReady() {
        callback.onAdLoaded();
    }

    @Override
    public void vastError(int error) {
        //TODO: implement vast error mapping
        switch (error) {
            case VASTPlayer.ERROR_NO_NETWORK: {
                callback.onAdLoadFailed(BMError.noFillError(BMError.Connection));
                break;
            }
            default: {
                callback.onAdLoadFailed(BMError.noFillError(null));
                break;
            }
        }
    }

    @Override
    public void vastShown() {
        callback.onAdShown();
    }

    @Override
    public void vastClick(String url, final Activity activity) {
        callback.onAdClicked();
        if (url != null) {
            if (activity instanceof VASTActivity) {
                ((VASTActivity) activity).showProgressBar();
            }
            Utils.openBrowser(activity, url, new Runnable() {
                @Override
                public void run() {
                    if (activity instanceof VASTActivity) {
                        ((VASTActivity) activity).hideProgressBar();
                    }
                }
            });
        }
    }

    @Override
    public void vastComplete() {
        callback.onAdFinished();
    }

    @Override
    public void vastDismiss(boolean finished) {
        callback.onAdClosed();
    }

}
