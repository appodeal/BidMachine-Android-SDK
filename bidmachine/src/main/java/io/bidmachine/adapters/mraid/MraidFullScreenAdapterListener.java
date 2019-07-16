package io.bidmachine.adapters.mraid;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.webkit.WebView;
import com.explorestack.iab.mraid.MRAIDInterstitial;
import com.explorestack.iab.mraid.MRAIDInterstitialListener;
import com.explorestack.iab.mraid.MRAIDNativeFeatureListener;
import com.explorestack.iab.utils.Utils;
import io.bidmachine.utils.BMError;

class MraidFullScreenAdapterListener implements MRAIDInterstitialListener,
        MRAIDNativeFeatureListener {

    @NonNull
    private final MraidFullScreenAdObject adObject;
    @Nullable
    private Runnable afterStartShowRunnable;

    MraidFullScreenAdapterListener(@NonNull MraidFullScreenAdObject adObject) {
        this.adObject = adObject;
    }

    @Override
    public void mraidInterstitialLoaded(MRAIDInterstitial mraidInterstitial) {
        adObject.processLoadSuccess();
    }

    @Override
    public void mraidInterstitialShow(MRAIDInterstitial mraidInterstitial) {
        if (afterStartShowRunnable != null) {
            afterStartShowRunnable.run();
        }
        adObject.processShown();
    }

    @Override
    public void mraidInterstitialHide(MRAIDInterstitial mraidInterstitial) {
        adObject.processFinished();
        adObject.processClosed(true);
        final MraidActivity showingActivity = adObject.getShowingActivity();
        if (showingActivity != null) {
            showingActivity.finish();
            showingActivity.overridePendingTransition(0, 0);
            adObject.setShowingActivity(null);
        }
    }

    @Override
    public void mraidInterstitialNoFill(MRAIDInterstitial mraidInterstitial) {
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
        if (url != null && adObject.getShowingActivity() != null) {
            if (adObject.getShowingActivity() != null) {
                adObject.getShowingActivity().showProgressBar();
            }
            Utils.openBrowser(adObject.getShowingActivity().getApplicationContext(), url,
                    new Runnable() {
                        @Override
                        public void run() {
                            if (adObject.getShowingActivity() != null) {
                                adObject.getShowingActivity().hideProgressBar();
                            }
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

    void setAfterStartShowRunnable(@Nullable Runnable afterStartShowRunnable) {
        this.afterStartShowRunnable = afterStartShowRunnable;
    }
}