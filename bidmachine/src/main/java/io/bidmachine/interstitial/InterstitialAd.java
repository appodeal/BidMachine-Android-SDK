package io.bidmachine.interstitial;

import android.content.Context;
import android.support.annotation.NonNull;
import io.bidmachine.*;
import io.bidmachine.models.AdObjectParams;
import io.bidmachine.unified.UnifiedFullscreenAd;
import io.bidmachine.ContextProvider;

public final class InterstitialAd
        extends FullScreenAd<InterstitialAd, InterstitialRequest, FullScreenAdObject<InterstitialRequest>, InterstitialListener> {

    public InterstitialAd(@NonNull Context context) {
        super(context, AdsType.Interstitial);
    }

    @Override
    protected FullScreenAdObject<InterstitialRequest> createAdObject(
            @NonNull ContextProvider contextProvider,
            @NonNull InterstitialRequest adRequest,
            @NonNull NetworkAdapter adapter,
            @NonNull AdObjectParams adObjectParams,
            @NonNull AdProcessCallback processCallback
    ) {
        UnifiedFullscreenAd unifiedAd = adapter.createInterstitial();
        if (unifiedAd == null) {
            return null;
        }
        return new FullScreenAdObject<>(contextProvider, processCallback, adRequest, adObjectParams, unifiedAd);
    }
}
