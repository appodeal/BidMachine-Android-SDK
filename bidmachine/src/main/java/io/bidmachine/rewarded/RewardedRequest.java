package io.bidmachine.rewarded;

import android.support.annotation.NonNull;

import io.bidmachine.AdRequest;
import io.bidmachine.AdsType;
import io.bidmachine.FullScreenAdRequest;
import io.bidmachine.protobuf.adcom.Placement;

public final class RewardedRequest extends FullScreenAdRequest<RewardedRequest> {

    private RewardedRequest() {
    }

    @Override
    protected void onBuildPlacement(Placement.Builder builder) {
        super.onBuildPlacement(builder);
        builder.setReward(true);
    }

    @NonNull
    @Override
    protected AdsType getType() {
        return AdsType.Rewarded;
    }

    public static final class Builder extends FullScreenRequestBuilder<Builder, RewardedRequest> {
        @Override
        protected RewardedRequest createRequest() {
            return new RewardedRequest();
        }
    }

    public interface AdRequestListener extends AdRequest.AdRequestListener<RewardedRequest> {
    }

}
