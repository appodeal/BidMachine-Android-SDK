package io.bidmachine.models;

import io.bidmachine.AdRequest;
import io.bidmachine.PriceFloorParams;
import io.bidmachine.TargetingParams;

public interface RequestBuilder<SelfType extends RequestBuilder,
        ReturnType extends AdRequest> {

    SelfType setPriceFloorParams(PriceFloorParams priceFloorParams);

    SelfType setTargetingParams(TargetingParams targetingParams);

//    SelfType setExtraParams(ExtraParams extraParams);

    SelfType setListener(AdRequest.AdRequestListener<ReturnType> listener);

    ReturnType build();

}