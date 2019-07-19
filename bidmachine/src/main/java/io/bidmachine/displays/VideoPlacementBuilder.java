package io.bidmachine.displays;

import android.content.Context;
import android.graphics.Point;
import android.support.annotation.NonNull;
import com.explorestack.protobuf.adcom.*;
import com.explorestack.protobuf.openrtb.Response;
import com.google.protobuf.Any;
import com.google.protobuf.Message;
import io.bidmachine.AdContentType;
import io.bidmachine.AdsType;
import io.bidmachine.Constants;
import io.bidmachine.NetworkConfig;
import io.bidmachine.core.Utils;
import io.bidmachine.models.AdObjectParams;
import io.bidmachine.unified.UnifiedAdRequestParams;

import java.util.Arrays;
import java.util.Collection;

public class VideoPlacementBuilder<UnifiedAdRequestParamsType extends UnifiedAdRequestParams>
        extends PlacementBuilder<UnifiedAdRequestParamsType>
        implements ISizableDisplayPlacement<UnifiedAdRequestParamsType> {

    private boolean canSkip;

    public VideoPlacementBuilder(boolean canSkip, boolean supportHeaderBidding) {
        super(AdContentType.Video, supportHeaderBidding);
        this.canSkip = canSkip;
    }

    @Override
    public Message.Builder createPlacement(@NonNull Context context,
                                           @NonNull UnifiedAdRequestParamsType adRequestParams,
                                           @NonNull AdsType adsType,
                                           @NonNull Collection<NetworkConfig> networkConfigs) {
        Placement.VideoPlacement.Builder builder = Placement.VideoPlacement.newBuilder();
        builder.setSkip(canSkip);
        builder.setUnit(SizeUnit.SIZE_UNIT_DIPS);
        builder.setPos(PlacementPosition.PLACEMENT_POSITION_FULLSCREEN);

        Point screenSize = getSize(context, adRequestParams);
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
        Message.Builder headerBiddingPlacement =
                createHeaderBiddingPlacement(context, adRequestParams, adsType, getAdContentType(), networkConfigs);
        if (headerBiddingPlacement != null) {
            builder.addExt(Any.pack(headerBiddingPlacement.build()));
        }
        return builder;
    }

    @Override
    public Point getSize(Context context, UnifiedAdRequestParamsType adRequestParams) {
        return Utils.getScreenSize(context);
    }

    @Override
    public AdObjectParams createAdObjectParams(@NonNull Context context,
                                               @NonNull UnifiedAdRequestParamsType adRequestParams,
                                               @NonNull Response.Seatbid seatbid,
                                               @NonNull Response.Seatbid.Bid bid,
                                               @NonNull Ad ad) {
        Ad.Video video = ad.getVideo();
        if (video == null) {
            return null;
        }
        AdObjectParams params = createHeaderBiddingAdObjectParams(context, adRequestParams, seatbid, bid, ad);
        if (params == null) {
            VideoAdObjectParams videoParams = new VideoAdObjectParams(seatbid, bid, ad);
            videoParams.setCreativeAdm(video.getAdm());
            params = videoParams;
        }
        return params;
    }

}
