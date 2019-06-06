package io.bidmachine.adapters.vast;

import android.app.Activity;
import android.support.annotation.NonNull;

import org.nexage.sourcekit.util.Utils;
import org.nexage.sourcekit.vast.VASTPlayer;
import org.nexage.sourcekit.vast.activity.VASTActivity;

import io.bidmachine.utils.BMError;

class VastFullScreenAdapterListener implements VASTPlayer.VASTPlayerListener {

    private VastFullScreenAdObject adObject;

    VastFullScreenAdapterListener(@NonNull VastFullScreenAdObject adObject) {
        this.adObject = adObject;
    }

    @Override
    public void vastReady() {
        adObject.processLoadSuccess();
    }

    @Override
    public void vastError(int error) {
        //TODO: implement vast error mapping
        switch (error) {
            case VASTPlayer.ERROR_NO_NETWORK: {
                adObject.processLoadFail(BMError.noFillError(BMError.Connection));
                break;
            }
            default: {
                adObject.processLoadFail(BMError.noFillError(null));
                break;
            }
        }
    }

    @Override
    public void vastShown() {
        adObject.processShown();
    }

    @Override
    public void vastClick(String url, final Activity activity) {
        adObject.processClicked();
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
        adObject.processFinished();
    }

    @Override
    public void vastDismiss(boolean finished) {
        adObject.processClosed(finished);
    }

}
