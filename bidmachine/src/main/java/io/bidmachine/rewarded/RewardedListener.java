package io.bidmachine.rewarded;

import android.support.annotation.NonNull;
import io.bidmachine.AdFullScreenListener;
import io.bidmachine.AdListener;

public interface RewardedListener
        extends AdListener<RewardedAd>, AdFullScreenListener<RewardedAd> {

    /**
     * Called when Rewarded Ad Complete (e.g - the video has been played to the end).
     * You can use this event to initialize your reward
     *
     * @param ad - Ad type {@link RewardedAd}
     */
    void onAdRewarded(@NonNull RewardedAd ad);
}
