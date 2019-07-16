package io.bidmachine.adapters.mraid;

import android.content.Context;
import com.explorestack.iab.vast.VideoType;
import io.bidmachine.BidMachine;
import io.bidmachine.displays.FullScreenAdObjectParams;
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
    private MraidFullScreenAdObject<RewardedAd> mraidFullScreenAdObject;
    private MraidFullScreenAdapterListener mraidFullScreenAdapterListener;

    @Before
    public void setUp() throws Exception {
        Context context = RuntimeEnvironment.application;
        BidMachine.initialize(context, "1");
        rewardedVideoListener = mock(RewardedListener.class);
        rewardedVideoAd = new RewardedAd(context);
        rewardedVideoAd.setListener(rewardedVideoListener);
        mraidFullScreenAdObject = spy(new MraidFullScreenAdObject<RewardedAd>(VideoType.Rewarded, mock(FullScreenAdObjectParams.class)));
        mraidFullScreenAdObject.attachAd(rewardedVideoAd);
        mraidFullScreenAdapterListener = new MraidFullScreenAdapterListener(mraidFullScreenAdObject);

        RobolectricHandlerFixer robolectricHandlerFixer = new RobolectricHandlerFixer();
        robolectricHandlerFixer.start();
    }

    @Test
    public void mraidInterstitialLoaded() {
        mraidFullScreenAdapterListener.mraidInterstitialLoaded(null);

        verify(mraidFullScreenAdObject).processLoadSuccess();
        verify(rewardedVideoListener).onAdLoaded(rewardedVideoAd);
    }

    @Test
    public void mraidInterstitialShow() {
        mraidFullScreenAdapterListener.mraidInterstitialShow(null);

        verify(mraidFullScreenAdObject).processShown();
        verify(rewardedVideoListener).onAdShown(rewardedVideoAd);
    }

    @Test
    public void mraidInterstitialHide() {
        mraidFullScreenAdapterListener.mraidInterstitialHide(null);

        verify(mraidFullScreenAdObject).processClosed(true);
        verify(rewardedVideoListener).onAdClosed(rewardedVideoAd, true);
    }

    @Test
    public void mraidInterstitialNoFill() {
        mraidFullScreenAdapterListener.mraidInterstitialNoFill(null);

        BMError error = BMError.noFillError(null);
        verify(mraidFullScreenAdObject).processLoadFail(error);
        verify(rewardedVideoListener).onAdLoadFailed(rewardedVideoAd, error);
    }

    @Test
    public void mraidNativeFeatureOpenBrowser() {
        mraidFullScreenAdapterListener.mraidNativeFeatureOpenBrowser(null, null);

        verify(mraidFullScreenAdObject).processClicked();
        verify(rewardedVideoListener).onAdClicked(rewardedVideoAd);
    }


}