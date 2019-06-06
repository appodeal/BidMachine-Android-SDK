package org.nexage.sourcekit.mraid;

import android.app.Activity;

import org.nexage.sourcekit.test_utils.RoboelectricShadowMRAIDView;
import org.nexage.sourcekit.test_utils.RoboelectricShadowMRAIDView_NoFill;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.mockito.Mockito.*;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class MRAIDInterstitialTest {
    private Activity activity;

    @Before
    public void setUp() throws Exception {
        activity = Robolectric.buildActivity(Activity.class).create().start().get();
    }

    @Test
    @Config(shadows={RoboelectricShadowMRAIDView.class})
    public void createMraidInterstitial_Loaded() throws Exception {
        MRAIDInterstitialListener listener = mock(MRAIDInterstitialListener.class);
        MRAIDNativeFeatureListener nativeFeatureListener = mock(MRAIDNativeFeatureListener.class);
        MRAIDInterstitial mraidInterstitial =  new MRAIDInterstitial.builder(activity, "", 0, 0)
                .setBaseUrl("")
                .setListener(listener)
                .setNativeFeatureListener(nativeFeatureListener)
                .setPreload(true)
                .setIsTag(false)
                .setUseLayout(true)
                .build();

        verify(listener).mraidInterstitialLoaded(mraidInterstitial);
        verify(nativeFeatureListener).mraidNativeFeatureOpenBrowser("url", null);

        assertTrue(mraidInterstitial.isReady);

        MRAIDView mraidView = mock(MRAIDView.class);
        mraidInterstitial.mraidView = mraidView;

        mraidInterstitial.show(activity);
        verify(mraidView).showAsInterstitial(activity);

        mraidInterstitial.mraidViewExpand(mraidInterstitial.mraidView);
        verify(listener).mraidInterstitialShow(mraidInterstitial);

        mraidInterstitial.mraidViewClose(mraidInterstitial.mraidView);
        verify(listener).mraidInterstitialHide(mraidInterstitial);
    }

    @Test
    @Config(shadows={RoboelectricShadowMRAIDView_NoFill.class})
    public void createMraidInterstitial_NoFill() throws Exception {
        MRAIDInterstitialListener listener = mock(MRAIDInterstitialListener.class);
        MRAIDNativeFeatureListener nativeFeatureListener = mock(MRAIDNativeFeatureListener.class);
        MRAIDInterstitial mraidInterstitial =  new MRAIDInterstitial.builder(activity, "", 0, 0)
                .setBaseUrl("")
                .setListener(listener)
                .setNativeFeatureListener(nativeFeatureListener)
                .setPreload(true)
                .setIsTag(false)
                .setUseLayout(true)
                .build();
        verify(listener).mraidInterstitialNoFill(mraidInterstitial);
        assertFalse(mraidInterstitial.isReady);
    }
}