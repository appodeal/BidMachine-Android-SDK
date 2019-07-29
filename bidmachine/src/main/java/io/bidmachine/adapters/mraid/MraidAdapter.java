package io.bidmachine.adapters.mraid;

import io.bidmachine.AdsType;
import io.bidmachine.NetworkAdapter;
import io.bidmachine.unified.UnifiedBannerAd;
import io.bidmachine.unified.UnifiedFullscreenAd;
import org.nexage.sourcekit.mraid.internal.MRAIDLog;
import org.nexage.sourcekit.util.Video;

public class MraidAdapter extends NetworkAdapter {

    public MraidAdapter() {
        super("mraid", "2.0", new AdsType[]{AdsType.Banner, AdsType.Interstitial, AdsType.Rewarded});
    }

    @Override
    public void setLogging(boolean enabled) {
        MRAIDLog.setLoggingLevel(enabled ? MRAIDLog.LOG_LEVEL.verbose : MRAIDLog.LOG_LEVEL.none);
    }

    @Override
    public UnifiedBannerAd createBanner() {
        return new MraidBannerAd();
    }

    @Override
    public UnifiedFullscreenAd createInterstitial() {
        return new MraidFullScreenAd(Video.Type.NON_REWARDED);
    }

    @Override
    public UnifiedFullscreenAd createRewarded() {
        return new MraidFullScreenAd(Video.Type.REWARDED);
    }

}
