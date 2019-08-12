package io.bidmachine.interstitial;

import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class InterstitialMraidRequestTest extends InterstitialRequestTest {

    @Override
    protected PlacementDisplayBuilder createPlacementDisplayBuilder() {
        return new MraidDisplayBuilder().setSize(320, 400);
    }
}
