package io.bidmachine.displays;

import com.explorestack.protobuf.adcom.Ad;
import com.explorestack.protobuf.openrtb.Response;

public class DisplayAdObjectParams extends FullScreenAdObjectParams {

    DisplayAdObjectParams(Response.Seatbid seatbid, Response.Seatbid.Bid bid, Ad ad) {
        super(seatbid, bid, ad);
        prepareEvents(ad.getDisplay().getEventList());
    }

}
