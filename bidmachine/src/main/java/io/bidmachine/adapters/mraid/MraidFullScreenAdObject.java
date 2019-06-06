package io.bidmachine.adapters.mraid;

import org.nexage.sourcekit.mraid.MRAIDInterstitial;
import org.nexage.sourcekit.util.Video;

import io.bidmachine.FullScreenAdObject;
import io.bidmachine.OrtbAd;
import io.bidmachine.displays.FullScreenAdObjectParams;

import static io.bidmachine.core.Utils.onUiThread;

class MraidFullScreenAdObject<RequestType extends OrtbAd>
        extends FullScreenAdObject<RequestType> {

    private Video.Type videoType;
    private MRAIDInterstitial mraidInterstitial;
    private MraidActivity showingActivity;
    private MraidFullScreenAdapterListener adapterListener;

    MraidFullScreenAdObject(Video.Type videoType, FullScreenAdObjectParams adObjectParams) {
        super(adObjectParams);
        this.videoType = videoType;
    }

    @Override
    public void load() {
        adapterListener = new MraidFullScreenAdapterListener(this);
        onUiThread(new Runnable() {
            @Override
            public void run() {
                mraidInterstitial = new MRAIDInterstitial.builder(
                        getContext(),
                        getParams().getCreativeAdm(),
                        getParams().getWidth(),
                        getParams().getHeight())
                        .setPreload(getParams().canPreload())
                        .setListener(adapterListener)
                        .setNativeFeatureListener(adapterListener)
                        .build();
            }
        });
    }

    @Override
    public void show() {
        MraidActivity.show(getContext(), this, videoType);
    }

    @Override
    protected void onDestroy() {
        if (mraidInterstitial != null) {
            mraidInterstitial = null;
        }
    }

    MRAIDInterstitial getMraidInterstitial() {
        return mraidInterstitial;
    }

    MraidActivity getShowingActivity() {
        return showingActivity;
    }

    MraidFullScreenAdapterListener getAdapterListener() {
        return adapterListener;
    }

    void setShowingActivity(MraidActivity showingActivity) {
        this.showingActivity = showingActivity;
    }
}