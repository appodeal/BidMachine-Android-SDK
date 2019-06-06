package io.bidmachine.interstitial;

import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.runner.RunWith;

import io.bidmachine.BaseFullScreenRequestTestImpl;

@RunWith(AndroidJUnit4.class)
@LargeTest
public abstract class InterstitialRequestTest
        extends BaseFullScreenRequestTestImpl<InterstitialAd, InterstitialRequest> {

    @Override
    protected InterstitialAd createAd() {
        return new InterstitialAd(activityTestRule.getActivity());
    }

    @Override
    protected InterstitialRequest createAdRequest() {
        return new InterstitialRequest.Builder().build();
    }

}
