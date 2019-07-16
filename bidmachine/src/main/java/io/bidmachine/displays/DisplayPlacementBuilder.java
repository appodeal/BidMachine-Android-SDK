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

public class DisplayPlacementBuilder<AdRequestType extends AdRequest>
        extends PlacementBuilder<AdRequestType>
        implements ISizableDisplayPlacement<AdRequestType> {

    private boolean isFullscreen;

    public DisplayPlacementBuilder(boolean fullscreen) {
        super(AdContentType.Static);
        this.isFullscreen = fullscreen;
    }

    @Override
    public Message.Builder buildPlacement(android.content.Context context, AdRequestType adRequest, OrtbAdapter adapter) {
        Placement.DisplayPlacement.Builder builder = Placement.DisplayPlacement.newBuilder();
        builder.addApi(ApiFramework.API_FRAMEWORK_MRAID_2_0);
        builder.setUnit(SizeUnit.SIZE_UNIT_DIPS);
        builder.addAllMime(Arrays.asList(Constants.IMAGE_MIME_TYPES));
        if (isFullscreen) {
            builder.setInstl(true);
            builder.setPos(PlacementPosition.PLACEMENT_POSITION_FULLSCREEN);
        }

        Point displaySize = getSize(context, adRequest);
        builder.setW(displaySize.x);
        builder.setH(displaySize.y);
        return builder;
    }

    @Override
    public Point getSize(Context context, AdRequestType adRequestType) {
        return Utils.getScreenSize(context);
    }

    @Override
    public boolean isMatch(Ad ad) {
        return ad.hasDisplay();
    }

    @Override
    public AdObjectParams createAdObjectParams(@NonNull Context context,
                                               @NonNull AdRequestType adRequest,
                                               @NonNull Response.Seatbid seatbid,
                                               @NonNull Response.Seatbid.Bid bid,
                                               @NonNull Ad ad) {
        Ad.Display display = ad.getDisplay();
        DisplayAdObjectParams params = new DisplayAdObjectParams(seatbid, bid, ad);
        params.setCreativeAdm(display.getAdm());
        params.setWidth(display.getW());
        params.setHeight(display.getH());
        return params;
    }

}
