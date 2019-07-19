package io.bidmachine.rewarded;

import android.content.Context;
import android.support.annotation.NonNull;
import io.bidmachine.*;
import io.bidmachine.models.AdObjectParams;
import io.bidmachine.unified.UnifiedFullscreenAd;
import io.bidmachine.utils.ContextProvider;

public final class RewardedAd extends FullScreenAd<RewardedAd, RewardedRequest, FullScreenAdObject<RewardedRequest>, RewardedListener> {

    public RewardedAd(Context context) {
        super(context);
    }

    @NonNull
    @Override
    protected AdsType getType() {
        return AdsType.Rewarded;
    }

    @Override
    protected FullScreenAdObject<RewardedRequest> createAdObject(
            @NonNull ContextProvider contextProvider,
            @NonNull RewardedRequest adRequest,
            @NonNull BidMachineAdapter adapter,
            @NonNull AdObjectParams adObjectParams,
            @NonNull AdProcessCallback processCallback
    ) {
        UnifiedFullscreenAd unifiedAd = adapter.createRewarded();
        if (unifiedAd == null) {
            return null;
        }
        return new FullScreenAdObject<>(contextProvider, processCallback, adRequest, adObjectParams, unifiedAd);
    }
}
