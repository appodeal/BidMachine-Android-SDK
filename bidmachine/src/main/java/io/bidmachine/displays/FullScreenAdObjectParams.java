package io.bidmachine.displays;

import android.support.annotation.NonNull;

import io.bidmachine.protobuf.AdExtension;
import io.bidmachine.protobuf.adcom.Ad;
import io.bidmachine.protobuf.openrtb.Response;

import static io.bidmachine.Utils.getOrDefault;

public class FullScreenAdObjectParams extends AdObjectParamsImpl {

    private static final int DEF_SKIP_AFTER_TIME_SEC = 2;

    private int skipAfterTimeSec = DEF_SKIP_AFTER_TIME_SEC;

    FullScreenAdObjectParams(Response.Seatbid seatbid, Response.Seatbid.Bid bid, Ad ad) {
        super(seatbid, bid, ad);
    }

    @Override
    protected void prepareExtensions(@NonNull Response.Seatbid seatbid, @NonNull Response.Seatbid.Bid bid, @NonNull AdExtension extension) {
        super.prepareExtensions(seatbid, bid, extension);
        skipAfterTimeSec = getOrDefault(extension.getSkipAfter(),
                AdExtension.getDefaultInstance().getSkipAfter(),
                DEF_SKIP_AFTER_TIME_SEC);
    }

    public int getSkipAfterTimeSec() {
        return skipAfterTimeSec;
    }

}
