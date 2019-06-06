package org.nexage.sourcekit.mraid;

import android.app.Activity;

import org.nexage.sourcekit.test_utils.RoboelectricShadowMRAIDVideoAddendumView;
import org.nexage.sourcekit.test_utils.RoboelectricShadowMRAIDVideoAddendumView_NoFill;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class MRAIDVideoAddendumInterstitialTest {
    private Activity activity;

    @Before
    public void setUp() throws Exception {
        activity = Robolectric.buildActivity(Activity.class).create().start().get();
    }

    @Test
    @Config(shadows={RoboelectricShadowMRAIDVideoAddendumView.class})
    public void createMraidInterstitial_Loaded() throws Exception {
        MRAIDVideoAddendumInterstitialListener listener = mock(MRAIDVideoAddendumInterstitialListener.class);
        MRAIDVideoAddendumInterstitial mraidVideoAddendumInterstitial = new MRAIDVideoAddendumInterstitial.Builder().setContext(activity).setData("").setListener(listener).build();

        verify(listener).mraidVideoAddendumInterstitialLoaded(mraidVideoAddendumInterstitial);

        mraidVideoAddendumInterstitial.show();

        verify(listener).mraidVideoAddendumViewClickThru(mraidVideoAddendumInterstitial, "url");
        verify(listener).mraidVideoAddendumInterstitialShow(mraidVideoAddendumInterstitial);
        verify(listener).mraidVideoAddendumViewStarted(mraidVideoAddendumInterstitial);
        verify(listener).mraidVideoAddendumViewStopped(mraidVideoAddendumInterstitial);
        verify(listener).mraidVideoAddendumViewSkipped(mraidVideoAddendumInterstitial);
        verify(listener).mraidVideoAddendumViewVideoStart(mraidVideoAddendumInterstitial);
        verify(listener).mraidVideoAddendumViewFirstQuartile(mraidVideoAddendumInterstitial);
        verify(listener).mraidVideoAddendumViewMidpoint(mraidVideoAddendumInterstitial);
        verify(listener).mraidVideoAddendumViewThirdQuartile(mraidVideoAddendumInterstitial);
        verify(listener).mraidVideoAddendumViewPaused(mraidVideoAddendumInterstitial);
        verify(listener).mraidVideoAddendumViewPlaying(mraidVideoAddendumInterstitial);
        verify(listener).mraidVideoAddendumViewError(mraidVideoAddendumInterstitial, "error");
        verify(listener).mraidVideoAddendumViewClickThru(mraidVideoAddendumInterstitial, "url");
        verify(listener).mraidVideoAddendumViewComplete(mraidVideoAddendumInterstitial);
        verify(listener).mraidVideoAddendumViewSkippableStateChange(mraidVideoAddendumInterstitial, true);
        verify(listener).mraidVideoAddendumViewLog(mraidVideoAddendumInterstitial, "log");
        verify(listener).mraidVideoAddendumInterstitialHide(mraidVideoAddendumInterstitial);

    }

    @Test
    @Config(shadows={RoboelectricShadowMRAIDVideoAddendumView_NoFill.class})
    public void createMraidInterstitial_NoFill() throws Exception {
        MRAIDVideoAddendumInterstitialListener listener = mock(MRAIDVideoAddendumInterstitialListener.class);
        MRAIDVideoAddendumInterstitial mraidVideoAddendumInterstitial = new MRAIDVideoAddendumInterstitial.Builder().setContext(activity).setListener(listener).build();
        verify(listener).mraidVideoAddendumInterstitialNoFill(mraidVideoAddendumInterstitial);
    }
}