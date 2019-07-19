package io.bidmachine.displays;

import android.content.Context;
import android.graphics.Point;
import android.support.annotation.NonNull;
import android.text.TextUtils;
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

public class DisplayPlacementBuilder<UnifiedAdRequestParamsType extends UnifiedAdRequestParams>
        extends PlacementBuilder<UnifiedAdRequestParamsType>
        implements ISizableDisplayPlacement<UnifiedAdRequestParamsType> {

    private boolean isFullscreen;

    public DisplayPlacementBuilder(boolean fullscreen, boolean supportHeaderBidding) {
        super(AdContentType.Static, supportHeaderBidding);
        this.isFullscreen = fullscreen;
    }

    @Override
    public Message.Builder createPlacement(@NonNull Context context,
                                           @NonNull UnifiedAdRequestParamsType adRequestParams,
                                           @NonNull AdsType adsType,
                                           @NonNull Collection<NetworkConfig> networkConfigs) {
        Placement.DisplayPlacement.Builder builder = Placement.DisplayPlacement.newBuilder();
        builder.addApi(ApiFramework.API_FRAMEWORK_MRAID_2_0);
        builder.setUnit(SizeUnit.SIZE_UNIT_DIPS);
        builder.addAllMime(Arrays.asList(Constants.IMAGE_MIME_TYPES));
        if (isFullscreen) {
            builder.setInstl(true);
            builder.setPos(PlacementPosition.PLACEMENT_POSITION_FULLSCREEN);
        }

        Point displaySize = getSize(context, adRequestParams);
        builder.setW(displaySize.x);
        builder.setH(displaySize.y);
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
                                               @NonNull UnifiedAdRequestParamsType adRequest,
                                               @NonNull Response.Seatbid seatbid,
                                               @NonNull Response.Seatbid.Bid bid,
                                               @NonNull Ad ad) {
        Ad.Display display = ad.getDisplay();
        if (display == null || TextUtils.isEmpty(display.getAdm())) {
            return null;
        }
        AdObjectParams params = createHeaderBiddingAdObjectParams(context, adRequest, seatbid, bid, ad);
        if (params == null) {
            DisplayAdObjectParams displayParams = new DisplayAdObjectParams(seatbid, bid, ad);
            displayParams.setCreativeAdm(display.getAdm());
            displayParams.setWidth(display.getW());
            displayParams.setHeight(display.getH());
            params = displayParams;
        }
        return params;
    }

}
