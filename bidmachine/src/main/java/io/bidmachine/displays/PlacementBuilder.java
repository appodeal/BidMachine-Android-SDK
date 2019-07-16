package io.bidmachine.displays;

import android.content.Context;
import android.support.annotation.NonNull;
import com.explorestack.protobuf.adcom.Ad;
import com.explorestack.protobuf.openrtb.Response;
import com.google.protobuf.Message;
import io.bidmachine.AdContentType;
import io.bidmachine.adapters.OrtbAdapter;
import io.bidmachine.models.AdObjectParams;

public abstract class PlacementBuilder<AdRequestType> {

    private AdContentType contentType;

    PlacementBuilder(AdContentType contentType) {
        this.contentType = contentType;
    }

    public AdContentType getAdContentType() {
        return contentType;
    }

    public abstract Message.Builder buildPlacement(android.content.Context context,
                                                   AdRequestType adRequest,
                                                   OrtbAdapter adapter);

    public abstract boolean isMatch(Ad ad);

    public abstract AdObjectParams createAdObjectParams(@NonNull Context context,
                                                        @NonNull AdRequestType adRequest,
                                                        @NonNull Response.Seatbid seatbid,
                                                        @NonNull Response.Seatbid.Bid bid,
                                                        @NonNull Ad ad);

}
