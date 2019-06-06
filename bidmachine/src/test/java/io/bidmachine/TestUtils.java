package io.bidmachine;

import java.lang.reflect.Field;

import static org.junit.Assert.assertNull;

public class TestUtils {

    public static void changeInitUrl(String url) {
        BidMachineImpl.DEF_INIT_URL = url;
    }

    public static void restoreInitUrl() {
        BidMachineImpl.DEF_INIT_URL = BuildConfig.BM_API_URL + "init";
    }

    public static void resetBidMachineInstance() throws NoSuchFieldException, IllegalAccessException {
        Field field = BidMachineImpl.class.getDeclaredField("instance");
        field.setAccessible(true);
        field.set(null, null);
        assertNull(field.get(null));
    }

}
