package io.bidmachine.adapters.mraid;

import android.support.annotation.Nullable;
import android.view.View;
import com.explorestack.iab.mraid.MRAIDView;
import io.bidmachine.ViewAd;
import io.bidmachine.ViewAdObject;
import io.bidmachine.displays.DisplayAdObjectParams;
import io.bidmachine.utils.BMError;

import static io.bidmachine.core.Utils.onUiThread;

class MraidViewAdObject<AdRequestType extends ViewAd> extends ViewAdObject<AdRequestType> {

    @Nullable
    private MRAIDView mraidView;

    MraidViewAdObject(DisplayAdObjectParams adDisplay) {
        super(adDisplay);
    }

    @Override
    public void load() {
        final MraidViewAdListener mraidBannerAdListener = new MraidViewAdListener(this);
        onUiThread(new Runnable() {
            @Override
            public void run() {
                mraidView = new MRAIDView.builder(getContext(), getParams().getCreativeAdm(), getWidth(), getHeight())
                        .setListener(mraidBannerAdListener)
                        .setNativeFeatureListener(mraidBannerAdListener)
                        .setPreload(getParams().canPreload())
                        .build();
                mraidView.load();
            }
        });
    }

    void processMraidViewLoaded() {
        if (mraidView != null && mraidView.getParent() == null) {
            mraidView.show();
            processLoadSuccess();
        } else {
            processLoadFail(BMError.Internal);
        }
    }

    @Override
    protected View obtainBannerView() {
        return mraidView;
    }

    @Override
    protected void onDestroy() {
        if (mraidView != null) {
            mraidView.destroy();
        }
    }

}