package io.bidmachine.banner;

import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.runner.RunWith;

import io.bidmachine.BaseViewAdRequestTest;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class BannerRequestTest extends BaseViewAdRequestTest<BannerAd, BannerRequest> {

    protected BannerSize size = BannerSize.Size_320_50;

    @Override
    protected BannerAd createAd() {
        return new BannerAd(activityTestRule.getActivity());
    }

    @Override
    protected BannerRequest createAdRequest() {
        return new BannerRequest.Builder().setSize(size).build();
    }

    @Override
    protected PlacementDisplayBuilder createPlacementDisplayBuilder() {
        return new MraidDisplayBuilder().setSize(size.width, size.height);
    }

}
