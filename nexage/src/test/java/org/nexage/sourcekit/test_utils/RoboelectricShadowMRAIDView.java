package org.nexage.sourcekit.test_utils;

import android.content.Context;

import org.nexage.sourcekit.mraid.MRAIDNativeFeatureListener;
import org.nexage.sourcekit.mraid.MRAIDView;
import org.nexage.sourcekit.mraid.MRAIDViewListener;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadows.ShadowViewGroup;

@Implements(MRAIDView.class)
public class RoboelectricShadowMRAIDView extends ShadowViewGroup {
    @RealObject
    private MRAIDView realView;
    private MRAIDViewListener listener;
    private MRAIDNativeFeatureListener nativeFeatureListener;

    public void __constructor__(MRAIDView.builder mMRAIDViewBuilder) {
        this.listener = mMRAIDViewBuilder.listener;
        this.nativeFeatureListener = mMRAIDViewBuilder.nativeFeatureListener;
    }

    @Implementation
    public void load() {
        listener.mraidViewLoaded(realView);
        nativeFeatureListener.mraidNativeFeatureOpenBrowser("url", null);
    }
}
