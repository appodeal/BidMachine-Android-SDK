package io.bidmachine.adapters.nast;

import io.bidmachine.adapters.OrtbAdapter;
import io.bidmachine.displays.NativeAdObjectParams;
import io.bidmachine.nativead.NativeAdObject;

public class NastAdapter extends OrtbAdapter {

    public NastAdapter() {
        super("nast", "1.0");
    }

    @Override
    public NativeAdObject createNativeAdObject(NativeAdObjectParams adObjectParams) {
        return new NastNativeAdObject(adObjectParams);
    }

}