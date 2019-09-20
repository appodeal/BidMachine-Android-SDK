package io.bidmachine;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class AdRequestExecutorTest {

    @Test
    public void testEnabling() {
        AdRequestExecutor executor = new AdRequestExecutor(1);
        executor.execute(new Runnable() {
            @Override
            public void run() {
                //ignore
            }
        });
        assertEquals(0, executor.getActiveCount());
        executor.enable();
        assertEquals(1, executor.getActiveCount());
    }

}
