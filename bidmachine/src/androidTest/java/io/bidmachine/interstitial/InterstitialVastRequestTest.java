package io.bidmachine.interstitial;

import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class InterstitialVastRequestTest extends InterstitialRequestTest {

    @Override
    protected PlacementDisplayBuilder createPlacementDisplayBuilder() {
        return new VastVideoDisplayBuilder();
    }

    @Override
    protected void testClick(InterstitialAd ad) {
        new AwaitHelper() {
            @Override
            public boolean isReady() {
                return rewardedState.getState() != null;
            }
        }.start(10000);
        super.testClick(ad);
    }
}
