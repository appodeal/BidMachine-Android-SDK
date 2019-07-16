package io.bidmachine.displays;

import android.content.Context;
import android.graphics.Point;
import android.support.annotation.NonNull;
import com.explorestack.protobuf.adcom.*;
import com.explorestack.protobuf.openrtb.Response;
import com.google.protobuf.Message;
import io.bidmachine.AdContentType;
import io.bidmachine.AdRequest;
import io.bidmachine.Constants;
import io.bidmachine.adapters.OrtbAdapter;
import io.bidmachine.core.Utils;
import io.bidmachine.models.AdObjectParams;

import java.util.Arrays;

public class VideoPlacementBuilder<AdRequestType extends AdRequest> extends PlacementBuilder<AdRequestType>
        implements ISizableDisplayPlacement<AdRequestType> {

    private boolean canSkip;

    public VideoPlacementBuilder(boolean canSkip) {
        super(AdContentType.Video);
        this.canSkip = canSkip;
    }

    @Override
    public Message.Builder buildPlacement(android.content.Context context, AdRequestType adRequest, OrtbAdapter adapter) {
        Placement.VideoPlacement.Builder builder = Placement.VideoPlacement.newBuilder();
        builder.setSkip(canSkip);
        builder.setUnit(SizeUnit.SIZE_UNIT_DIPS);
        builder.setPos(PlacementPosition.PLACEMENT_POSITION_FULLSCREEN);

        Point screenSize = getSize(context, adRequest);
        builder.setW(screenSize.x);
        builder.setH(screenSize.y);

        builder.addCtype(VideoCreativeType.VIDEO_CREATIVE_TYPE_VAST_2_0);
        builder.addCtype(VideoCreativeType.VIDEO_CREATIVE_TYPE_VAST_3_0);
        builder.addCtype(VideoCreativeType.VIDEO_CREATIVE_TYPE_VAST_WRAPPER_2_0);
        builder.addCtype(VideoCreativeType.VIDEO_CREATIVE_TYPE_VAST_WRAPPER_3_0);

        builder.addAllMime(Arrays.asList(Constants.VIDEO_MIME_TYPES));
        builder.setMinbitr(Constants.VIDEO_MINBITR);
        builder.setMaxbitr(Constants.VIDEO_MAXBITR);
        builder.setMindur(Constants.VIDEO_MINDUR);
        builder.setMaxdur(Constants.VIDEO_MAXDUR);
        builder.setLinearValue(Constants.VIDEO_LINEARITY);

        return builder;
    }

    @Override
    public Point getSize(Context context, AdRequestType request) {
        return Utils.getScreenSize(context);
    }

    @Override
    public boolean isMatch(Ad ad) {
        return ad.hasVideo();
    }

    @Override
    public AdObjectParams createAdObjectParams(@NonNull Context context,
                                               @NonNull AdRequestType adRequest,
                                               @NonNull Response.Seatbid seatbid,
                                               @NonNull Response.Seatbid.Bid bid,
                                               @NonNull Ad ad) {
        Ad.Video video = ad.getVideo();
        VideoAdObjectParams params = new VideoAdObjectParams(seatbid, bid, ad);
        params.setCreativeAdm(video.getAdm());
        return params;
    }

}
