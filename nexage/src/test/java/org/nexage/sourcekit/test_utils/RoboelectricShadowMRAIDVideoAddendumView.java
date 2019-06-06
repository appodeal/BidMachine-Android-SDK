package org.nexage.sourcekit.test_utils;

import android.app.Activity;
import android.content.Context;

import org.nexage.sourcekit.mraid.MRAIDNativeFeatureListener;
import org.nexage.sourcekit.mraid.MRAIDVideoAddendumView;
import org.nexage.sourcekit.mraid.MRAIDVideoAddendumViewListener;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadows.ShadowViewGroup;

@Implements(MRAIDVideoAddendumView.class)
public class RoboelectricShadowMRAIDVideoAddendumView extends ShadowViewGroup {
    @RealObject
    private MRAIDVideoAddendumView realView;
    private MRAIDVideoAddendumViewListener listener;

    public void __constructor__(Context context, String baseUrl, String data, String[] supportedNativeFeatures, MRAIDVideoAddendumViewListener listener,
                                MRAIDNativeFeatureListener nativeFeatureListener, boolean isInterstitial, int width, int height, boolean preload, boolean skippable) {
        listener.mraidVideoAddendumViewLoaded(realView);
        this.listener = listener;
    }

    @Implementation
    public void showAsInterstitial(Activity activity) {
        listener.mraidVideoAddendumViewExpand(realView);
        listener.mraidVideoAddendumViewStarted(realView);
        listener.mraidVideoAddendumViewStopped(realView);
        listener.mraidVideoAddendumViewSkipped(realView);
        listener.mraidVideoAddendumViewVideoStart(realView);
        listener.mraidVideoAddendumViewFirstQuartile(realView);
        listener.mraidVideoAddendumViewMidpoint(realView);
        listener.mraidVideoAddendumViewThirdQuartile(realView);
        listener.mraidVideoAddendumViewPaused(realView);
        listener.mraidVideoAddendumViewPlaying(realView);
        listener.mraidVideoAddendumViewError(realView, "error");
        listener.mraidVideoAddendumViewClickThru(realView, "url");
        listener.mraidVideoAddendumViewComplete(realView);
        listener.mraidVideoAddendumViewSkippableStateChange(realView, true);
        listener.mraidVideoAddendumViewLog(realView, "log");
        listener.mraidVideoAddendumViewClose(realView);
    }
}
