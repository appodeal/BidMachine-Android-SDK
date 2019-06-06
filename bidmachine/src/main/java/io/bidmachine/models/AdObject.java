package io.bidmachine.models;

import io.bidmachine.AdProcessCallback;
import io.bidmachine.IAd;

public interface AdObject<
        AdType extends IAd,
        AdObjectParamsType extends AdObjectParams>
        extends AdProcessCallback {

    void attachAd(AdType ad);

    AdType getAd();

    AdObjectParamsType getParams();

    void load();

    void destroy();

}