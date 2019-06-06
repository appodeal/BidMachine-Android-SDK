package io.bidmachine.displays;

import io.bidmachine.protobuf.adcom.Ad;
import io.bidmachine.protobuf.openrtb.Response;

public class DisplayAdObjectParams extends FullScreenAdObjectParams {

    DisplayAdObjectParams(Response.Seatbid seatbid, Response.Seatbid.Bid bid, Ad ad) {
        super(seatbid, bid, ad);
        prepareEvents(ad.getDisplay().getEventList());
    }

}
