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
public class RoboelectricShadowMRAIDView_NoFill extends ShadowViewGroup {
    @RealObject
    private MRAIDView realView;
    private MRAIDViewListener listener;

    public void __constructor__(MRAIDView.builder mMRAIDViewBuilder) {
        this.listener = mMRAIDViewBuilder.listener;
    }

    @Implementation
    public void load() {
        listener.mraidViewNoFill(realView);
    }
}
