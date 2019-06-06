package io.bidmachine;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowLog;

@RunWith(RobolectricTestRunner.class)
public class UrlBuilderTest {

    @Before
    public void setup() {
        ShadowLog.stream = System.out;
    }

    @Test
    public void builderTest_noParams() {
    }

}
