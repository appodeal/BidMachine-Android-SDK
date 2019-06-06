package io.bidmachine.adapters.mraid;

import org.nexage.sourcekit.mraid.internal.MRAIDLog;
import org.nexage.sourcekit.util.Video;

import io.bidmachine.FullScreenAdObject;
import io.bidmachine.ViewAdObject;
import io.bidmachine.adapters.OrtbAdapter;
import io.bidmachine.displays.DisplayAdObjectParams;
import io.bidmachine.displays.FullScreenAdObjectParams;
import io.bidmachine.displays.VideoAdObjectParams;
import io.bidmachine.models.AdObjectParams;

public class MraidAdapter extends OrtbAdapter {

    public MraidAdapter() {
        super("mraid", "2.0");
    }

    @Override
    public void setLogging(boolean enabled) {
        MRAIDLog.setLoggingLevel(enabled ? MRAIDLog.LOG_LEVEL.verbose : MRAIDLog.LOG_LEVEL.none);
    }

    @Override
    public ViewAdObject createBannerAdObject(DisplayAdObjectParams adObjectParams) {
        return new MraidViewAdObject(adObjectParams);
    }

    @Override
    public FullScreenAdObject createInterstitialAdObject(FullScreenAdObjectParams adObjectParams) {
        return new MraidFullScreenAdObject<>(Video.Type.NON_REWARDED, adObjectParams);
    }

    @Override
    public FullScreenAdObject createRewardedAdObject(FullScreenAdObjectParams adObjectParams) {
        return new MraidFullScreenAdObject<>(Video.Type.REWARDED, adObjectParams);
    }

}
