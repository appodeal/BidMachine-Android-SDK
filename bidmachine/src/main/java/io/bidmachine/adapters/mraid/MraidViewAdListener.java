package io.bidmachine.adapters.mraid;

import android.webkit.WebView;
import com.explorestack.iab.mraid.MRAIDNativeFeatureListener;
import com.explorestack.iab.mraid.MRAIDView;
import com.explorestack.iab.mraid.MRAIDViewListener;
import com.explorestack.iab.utils.Utils;
import io.bidmachine.utils.BMError;

class MraidViewAdListener implements MRAIDViewListener, MRAIDNativeFeatureListener {

    private MraidViewAdObject adObject;

    MraidViewAdListener(MraidViewAdObject adObject) {
        this.adObject = adObject;
    }

    @Override
    public void mraidViewLoaded(MRAIDView mraidView) {
        adObject.processMraidViewLoaded();
    }

    @Override
    public void mraidViewExpand(MRAIDView mraidView) {
    }

    @Override
    public void mraidViewClose(MRAIDView mraidView) {
    }

    @Override
    public boolean mraidViewResize(MRAIDView mraidView, int width, int height, int offsetX, int offsetY) {
        return false;
    }

    @Override
    public void mraidViewNoFill(MRAIDView mraidView) {
        adObject.processLoadFail(BMError.noFillError(null));
    }

    @Override
    public void mraidNativeFeatureCallTel(String url) {
    }

    @Override
    public void mraidNativeFeatureCreateCalendarEvent(String eventJSON) {
    }

    @Override
    public void mraidNativeFeaturePlayVideo(String url) {
    }

    @Override
    public void mraidNativeFeatureOpenBrowser(String url, WebView view) {
        adObject.processClicked();
        if (url != null && adObject.obtainBannerView() != null) {
            Utils.addBannerSpinnerView(adObject.obtainBannerView());
            Utils.openBrowser(adObject.obtainBannerView().getContext(), url, new Runnable() {
                @Override
                public void run() {
                    Utils.hideBannerSpinnerView(adObject.obtainBannerView());
                }
            });
        }
    }

    @Override
    public void mraidNativeFeatureStorePicture(String url) {
    }

    @Override
    public void mraidNativeFeatureSendSms(String url) {
    }

}