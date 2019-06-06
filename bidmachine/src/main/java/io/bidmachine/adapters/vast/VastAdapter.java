package io.bidmachine.adapters.vast;

import org.nexage.sourcekit.util.VASTLog;
import org.nexage.sourcekit.util.Video;

import io.bidmachine.FullScreenAdObject;
import io.bidmachine.adapters.OrtbAdapter;
import io.bidmachine.displays.FullScreenAdObjectParams;
import io.bidmachine.displays.VideoAdObjectParams;
import io.bidmachine.models.AdObjectParams;

public class VastAdapter extends OrtbAdapter {

    public VastAdapter() {
        super("vast", "2.0");
    }

    @Override
    public void setLogging(boolean enabled) {
        VASTLog.setLoggingLevel(enabled ? VASTLog.LOG_LEVEL.verbose : VASTLog.LOG_LEVEL.none);
    }

    @Override
    public FullScreenAdObject createInterstitialAdObject(FullScreenAdObjectParams adObjectParams) {
        return new VastFullScreenAdObject<>(Video.Type.NON_REWARDED, adObjectParams);
    }

    @Override
    public FullScreenAdObject createRewardedAdObject(FullScreenAdObjectParams adObjectParams) {
        return new VastFullScreenAdObject<>(Video.Type.REWARDED, adObjectParams);
    }

}
