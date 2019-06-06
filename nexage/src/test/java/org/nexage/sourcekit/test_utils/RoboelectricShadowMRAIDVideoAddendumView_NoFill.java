package org.nexage.sourcekit.test_utils;

import android.content.Context;

import org.nexage.sourcekit.mraid.MRAIDNativeFeatureListener;
import org.nexage.sourcekit.mraid.MRAIDVideoAddendumView;
import org.nexage.sourcekit.mraid.MRAIDVideoAddendumViewListener;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadows.ShadowViewGroup;

@Implements(MRAIDVideoAddendumView.class)
public class RoboelectricShadowMRAIDVideoAddendumView_NoFill extends ShadowViewGroup {
    @RealObject
    private MRAIDVideoAddendumView realView;

    public void __constructor__(Context context, String baseUrl, String data, String[] supportedNativeFeatures, MRAIDVideoAddendumViewListener listener,
                                MRAIDNativeFeatureListener nativeFeatureListener, boolean isInterstitial, int width, int height, boolean preload, boolean skippable) {
        listener.mraidVideoAddendumViewNoFill(realView);
    }
}
