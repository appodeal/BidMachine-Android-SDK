package io.bidmachine.adapters.nast;

import io.bidmachine.AdsType;
import io.bidmachine.NetworkAdapter;
import io.bidmachine.unified.UnifiedNativeAd;

public class NastAdapter extends NetworkAdapter {

    public NastAdapter() {
        super("nast", "1.0", new AdsType[]{AdsType.Native});
    }

    @Override
    public UnifiedNativeAd createNativeAd() {
        return new NastNativeAdObject();
    }

}