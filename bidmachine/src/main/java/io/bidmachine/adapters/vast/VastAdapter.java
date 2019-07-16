package io.bidmachine.adapters.vast;

import com.explorestack.iab.utils.Logger;
import com.explorestack.iab.vast.VastLog;
import com.explorestack.iab.vast.VideoType;
import io.bidmachine.FullScreenAdObject;
import io.bidmachine.adapters.OrtbAdapter;
import io.bidmachine.displays.FullScreenAdObjectParams;

public class VastAdapter extends OrtbAdapter {

    public VastAdapter() {
        super("vast", "2.0");
    }

    @Override
    public void setLogging(boolean enabled) {
        VastLog.setLoggingLevel(enabled ? Logger.LogLevel.debug : Logger.LogLevel.none);
    }

    @Override
    public FullScreenAdObject createInterstitialAdObject(FullScreenAdObjectParams adObjectParams) {
        return new VastFullScreenAdObject<>(VideoType.NonRewarded, adObjectParams);
    }

    @Override
    public FullScreenAdObject createRewardedAdObject(FullScreenAdObjectParams adObjectParams) {
        return new VastFullScreenAdObject<>(VideoType.Rewarded, adObjectParams);
    }

}
