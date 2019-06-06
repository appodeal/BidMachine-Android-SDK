package io.bidmachine.interstitial;

import android.content.Context;
import android.support.annotation.NonNull;

import io.bidmachine.AdsType;
import io.bidmachine.FullScreenAd;
import io.bidmachine.FullScreenAdObject;

public final class InterstitialAd extends FullScreenAd<
        InterstitialAd,
        InterstitialRequest,
        FullScreenAdObject<InterstitialAd>,
        InterstitialListener> {

    public InterstitialAd(Context context) {
        super(context);
    }

    @NonNull
    @Override
    protected AdsType getType() {
        return AdsType.Interstitial;
    }

}
