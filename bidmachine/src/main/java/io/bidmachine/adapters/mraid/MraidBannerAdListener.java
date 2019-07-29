package io.bidmachine.adapters.mraid;

import android.webkit.WebView;
import io.bidmachine.unified.UnifiedBannerAdCallback;
import io.bidmachine.utils.BMError;
import org.nexage.sourcekit.mraid.MRAIDNativeFeatureListener;
import org.nexage.sourcekit.mraid.MRAIDView;
import org.nexage.sourcekit.mraid.MRAIDViewListener;
import org.nexage.sourcekit.util.Utils;

class MraidBannerAdListener implements MRAIDViewListener, MRAIDNativeFeatureListener {

    private MraidBannerAd adObject;
    private UnifiedBannerAdCallback callback;

    MraidBannerAdListener(MraidBannerAd adObject, UnifiedBannerAdCallback callback) {
        this.adObject = adObject;
        this.callback = callback;
    }

    @Override
    public void mraidViewLoaded(MRAIDView mraidView) {
        adObject.processMraidViewLoaded(callback);
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
        callback.onAdLoadFailed(BMError.noFillError(null));
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
        callback.onAdClicked();
        if (url != null && adObject.mraidView != null) {
            Utils.addBannerSpinnerView(adObject.mraidView);
            Utils.openBrowser(adObject.mraidView.getContext(), url, new Runnable() {
                @Override
                public void run() {
                    Utils.hideBannerSpinnerView(adObject.mraidView);
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