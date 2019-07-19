package io.bidmachine.adapters.vast;

import io.bidmachine.AdsType;
import io.bidmachine.BidMachineAdapter;
import io.bidmachine.unified.UnifiedFullscreenAd;
import org.nexage.sourcekit.util.VASTLog;
import org.nexage.sourcekit.util.Video;

public class VastAdapter extends BidMachineAdapter {

    public VastAdapter() {
        super("vast", "2.0", new AdsType[]{AdsType.Interstitial, AdsType.Rewarded});
    }

    @Override
    public void setLogging(boolean enabled) {
        VASTLog.setLoggingLevel(enabled ? VASTLog.LOG_LEVEL.verbose : VASTLog.LOG_LEVEL.none);
    }

    @Override
    public UnifiedFullscreenAd createInterstitial() {
        return new VastFullScreenAdObject(Video.Type.NON_REWARDED);
    }

    @Override
    public UnifiedFullscreenAd createRewarded() {
        return new VastFullScreenAdObject(Video.Type.REWARDED);
    }

}
