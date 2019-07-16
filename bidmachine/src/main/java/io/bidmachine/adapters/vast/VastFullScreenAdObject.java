package io.bidmachine.adapters.vast;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.explorestack.iab.vast.VastRequest;
import com.explorestack.iab.vast.VideoType;
import io.bidmachine.FullScreenAdObject;
import io.bidmachine.OrtbAd;
import io.bidmachine.displays.FullScreenAdObjectParams;
import io.bidmachine.utils.BMError;

class VastFullScreenAdObject<RequestType extends OrtbAd>
        extends FullScreenAdObject<RequestType> {

    @NonNull
    private VideoType videoType;
    @Nullable
    private VastRequest vastRequest;
    private VastFullScreenAdapterListener vastListener;

    VastFullScreenAdObject(@NonNull VideoType videoType, FullScreenAdObjectParams adObjectParams) {
        super(adObjectParams);
        this.videoType = videoType;
    }

    @Override
    public void load() {
        vastListener = new VastFullScreenAdapterListener(this);
        vastRequest = VastRequest.newBuilder()
                .setPreCache(true)
                .setCloseTime(getParams().getSkipAfterTimeSec())
                .build();
        assert vastRequest != null;
        vastRequest.loadVideoWithData(getContext(), getParams().getCreativeAdm(), vastListener);
    }

    @Override
    public void show() {
        if (vastRequest != null && vastRequest.checkFile()) {
            vastRequest.display(getContext(), videoType, vastListener);
        } else {
            processShowFail(BMError.Internal);
        }
    }

    @Override
    protected void onDestroy() {
        if (vastRequest != null) {
            vastRequest = null;
        }
    }

}
