package io.bidmachine.rewarded;

import android.content.Context;
import android.support.annotation.NonNull;

import io.bidmachine.AdsType;
import io.bidmachine.FullScreenAd;
import io.bidmachine.FullScreenAdObject;

public final class RewardedAd extends FullScreenAd<
        RewardedAd,
        RewardedRequest,
        FullScreenAdObject<RewardedAd>,
        RewardedListener> {

    public RewardedAd(Context context) {
        super(context);
    }

    @NonNull
    @Override
    protected AdsType getType() {
        return AdsType.Rewarded;
    }

}
