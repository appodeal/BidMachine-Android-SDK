package io.bidmachine.adapters.vast;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.explorestack.iab.utils.Utils;
import com.explorestack.iab.vast.*;
import com.explorestack.iab.vast.activity.VastActivity;
import io.bidmachine.utils.BMError;

class VastFullScreenAdapterListener implements VastRequestListener, VastActivityListener {

    private VastFullScreenAdObject adObject;

    VastFullScreenAdapterListener(@NonNull VastFullScreenAdObject adObject) {
        this.adObject = adObject;
    }

    @Override
    public void onVastLoaded(@NonNull VastRequest vastRequest) {
        adObject.processLoadSuccess();
    }

    @Override
    public void onVastError(@NonNull Context context, @NonNull VastRequest vastRequest, int error) {
        //TODO: implement vast error mapping
        switch (error) {
            case VastError.ERROR_CODE_NO_NETWORK: {
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
    public void onVastShown(@NonNull VastActivity vastActivity, @NonNull VastRequest vastRequest) {
        adObject.processShown();
    }

    @Override
    public void onVastClick(@NonNull VastActivity vastActivity,
                            @NonNull VastRequest vastRequest,
                            @NonNull final VastClickCallback vastClickCallback,
                            @Nullable String url) {
        adObject.processClicked();
        if (url != null) {
            Utils.openBrowser(vastActivity, url, new Runnable() {
                @Override
                public void run() {
                    vastClickCallback.clickHandled();
                }
            });
        } else {
            vastClickCallback.clickHandleCanceled();
        }
    }

    @Override
    public void onVastComplete(@NonNull VastActivity vastActivity, @NonNull VastRequest vastRequest) {
        adObject.processFinished();
    }

    @Override
    public void onVastDismiss(@NonNull VastActivity vastActivity, @Nullable VastRequest vastRequest, boolean finished) {
        adObject.processClosed(finished);
    }

}
