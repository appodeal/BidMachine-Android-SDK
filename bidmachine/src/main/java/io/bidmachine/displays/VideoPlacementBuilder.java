package io.bidmachine.displays;

import android.graphics.Point;
import android.support.annotation.NonNull;
import com.explorestack.protobuf.adcom.*;
import com.explorestack.protobuf.openrtb.Response;
import com.explorestack.protobuf.Any;
import com.explorestack.protobuf.Message;
import io.bidmachine.*;
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
    public void createPlacement(@NonNull ContextProvider contextProvider,
                                @NonNull UnifiedAdRequestParamsType adRequestParams,
                                @NonNull AdsType adsType,
                                @NonNull Collection<NetworkConfig> networkConfigs,
                                @NonNull PlacementCreateCallback callback) {
        Placement.VideoPlacement.Builder builder = Placement.VideoPlacement.newBuilder();
        builder.setSkip(canSkip);
        builder.setUnit(SizeUnit.SIZE_UNIT_DIPS);
        builder.setPos(PlacementPosition.PLACEMENT_POSITION_FULLSCREEN);

        Point screenSize = getSize(contextProvider, adRequestParams);
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
                createHeaderBiddingPlacement(contextProvider, adRequestParams, adsType, networkConfigs);
        if (headerBiddingPlacement != null) {
            builder.addExt(Any.pack(headerBiddingPlacement.build()));
        }
        callback.onCreated(builder);
    }

    @Override
    public Point getSize(ContextProvider contextProvider, UnifiedAdRequestParamsType adRequestParams) {
        return Utils.getScreenSize(contextProvider.getContext());
    }

    @Override
    public AdObjectParams createAdObjectParams(@NonNull ContextProvider contextProvider,
                                               @NonNull UnifiedAdRequestParamsType adRequestParams,
                                               @NonNull Response.Seatbid seatbid,
                                               @NonNull Response.Seatbid.Bid bid,
                                               @NonNull Ad ad) {
        if (!ad.hasVideo()) {
            return null;
        }
        AdObjectParams params = createHeaderBiddingAdObjectParams(contextProvider, adRequestParams, seatbid, bid, ad);
        if (params == null) {
            Ad.Video video = ad.getVideo();
            VideoAdObjectParams videoParams = new VideoAdObjectParams(seatbid, bid, ad);
            videoParams.setCreativeAdm(video.getAdm());
            params = videoParams;
        }
        return params;
    }

}
