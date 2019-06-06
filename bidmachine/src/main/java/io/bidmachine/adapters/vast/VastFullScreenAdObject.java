package io.bidmachine.adapters.vast;

import org.nexage.sourcekit.util.Video;
import org.nexage.sourcekit.vast.view.AppodealVASTPlayer;

import io.bidmachine.FullScreenAdObject;
import io.bidmachine.OrtbAd;
import io.bidmachine.displays.FullScreenAdObjectParams;
import io.bidmachine.utils.BMError;

class VastFullScreenAdObject<RequestType extends OrtbAd>
        extends FullScreenAdObject<RequestType> {

    private Video.Type videoType;
    private AppodealVASTPlayer vastPlayer;
    private VastFullScreenAdapterListener vastListener;

    VastFullScreenAdObject(Video.Type videoType, FullScreenAdObjectParams adObjectParams) {
        super(adObjectParams);
        this.videoType = videoType;
    }

    @Override
    public void load() {
        vastPlayer = new AppodealVASTPlayer(getContext());
        vastPlayer.setPrecache(true);
        vastPlayer.setCloseTime(getParams().getSkipAfterTimeSec());
        vastListener = new VastFullScreenAdapterListener(this);
        vastPlayer.loadVideoWithData(getParams().getCreativeAdm(), vastListener);
    }

    @Override
    public void show() {
        if (vastPlayer.checkFile()) {
            vastPlayer.play(getContext(), videoType, vastListener);
        } else {
            processShowFail(BMError.Internal);
        }
    }

    @Override
    protected void onDestroy() {
        if (vastPlayer != null) {
            vastPlayer = null;
        }
    }

}
