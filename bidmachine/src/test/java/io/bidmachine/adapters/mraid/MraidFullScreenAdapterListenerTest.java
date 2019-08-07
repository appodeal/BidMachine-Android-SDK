package io.bidmachine.adapters.mraid;

import android.content.Context;
import io.bidmachine.BidMachine;
import io.bidmachine.rewarded.RewardedAd;
import io.bidmachine.rewarded.RewardedListener;
import io.bidmachine.test_utils.RobolectricHandlerFixer;
import io.bidmachine.utils.BMError;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class MraidFullScreenAdapterListenerTest {

    private RewardedAd rewardedVideoAd;
    private RewardedListener rewardedVideoListener;
    private MraidFullScreenAd mraidFullScreenAd;
    private MraidFullScreenAdapterListener mraidFullScreenAdapterListener;

    @Before
    public void setUp() throws Exception {
        Context context = RuntimeEnvironment.application;
        BidMachine.initialize(context, "1");
        rewardedVideoListener = mock(RewardedListener.class);
        rewardedVideoAd = new RewardedAd(context);
        rewardedVideoAd.setListener(rewardedVideoListener);
        mraidFullScreenAd = spy(new MraidFullScreenAd<RewardedAd>(Video.Type.REWARDED, mock(FullScreenAdObjectParams.class)));
        mraidFullScreenAd.attachAd(rewardedVideoAd);
        mraidFullScreenAdapterListener = new MraidFullScreenAdapterListener(mraidFullScreenAd);

        RobolectricHandlerFixer robolectricHandlerFixer = new RobolectricHandlerFixer();
        robolectricHandlerFixer.start();
    }

    @Test
    public void mraidInterstitialLoaded() {
        mraidFullScreenAdapterListener.mraidInterstitialLoaded(null);

        verify(mraidFullScreenAd).processLoadSuccess();
        verify(rewardedVideoListener).onAdLoaded(rewardedVideoAd);
    }

    @Test
    public void mraidInterstitialShow() {
        mraidFullScreenAdapterListener.mraidInterstitialShow(null);

        verify(mraidFullScreenAd).processShown();
        verify(rewardedVideoListener).onAdShown(rewardedVideoAd);
    }

    @Test
    public void mraidInterstitialHide() {
        mraidFullScreenAdapterListener.mraidInterstitialHide(null);

        verify(mraidFullScreenAd).processClosed(true);
        verify(rewardedVideoListener).onAdClosed(rewardedVideoAd, true);
    }

    @Test
    public void mraidInterstitialNoFill() {
        mraidFullScreenAdapterListener.mraidInterstitialNoFill(null);

        BMError error = BMError.noFillError(null);
        verify(mraidFullScreenAd).processLoadFail(error);
        verify(rewardedVideoListener).onAdLoadFailed(rewardedVideoAd, error);
    }

    @Test
    public void mraidNativeFeatureOpenBrowser() {
        mraidFullScreenAdapterListener.mraidNativeFeatureOpenBrowser(null, null);

        verify(mraidFullScreenAd).processClicked();
        verify(rewardedVideoListener).onAdClicked(rewardedVideoAd);
    }


}