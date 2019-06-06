package io.bidmachine.test_utils;

import org.robolectric.shadows.ShadowLooper;

//TODO: move to Shadow
public class RobolectricHandlerFixer extends Thread {

    private boolean isStopped = false;

    @Override
    public void run() {
        super.run();
        while (!isStopped && !Thread.currentThread().isInterrupted()) {
            try {
                //issues with java.lang.RuntimeException: Can't create handler inside thread that has not called Looper.prepare()
                ShadowLooper.runUiThreadTasks();
//                ShadowLooper.getMainLooper().;
//                Shadows.shadowOf(ShadowLooper.getMainLooper()).runToEndOfTasks();
            } catch (Exception e) {
                //issue with MainThread tasks
                e.printStackTrace();
            }
        }
    }

    public void shutdown() {
        isStopped = false;
    }

}