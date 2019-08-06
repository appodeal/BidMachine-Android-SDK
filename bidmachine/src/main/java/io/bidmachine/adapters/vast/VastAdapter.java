package io.bidmachine.adapters.vast;

import io.bidmachine.AdsType;
import io.bidmachine.BuildConfig;
import io.bidmachine.NetworkAdapter;
import io.bidmachine.unified.UnifiedFullscreenAd;
import org.nexage.sourcekit.util.VASTLog;
import org.nexage.sourcekit.util.Video;

public class VastAdapter extends NetworkAdapter {

    public VastAdapter() {
        super("vast",
                "2.0",
                BuildConfig.VERSION_NAME + ".1",
                new AdsType[]{AdsType.Interstitial, AdsType.Rewarded});
    }

    @Override
    public void setLogging(boolean enabled) {
        VASTLog.setLoggingLevel(enabled ? VASTLog.LOG_LEVEL.verbose : VASTLog.LOG_LEVEL.none);
    }

    @Override
    public UnifiedFullscreenAd createInterstitial() {
        return new VastFullScreenAd(Video.Type.NON_REWARDED);
    }

    @Override
    public UnifiedFullscreenAd createRewarded() {
        return new VastFullScreenAd(Video.Type.REWARDED);
    }

}
