package io.bidmachine;

import io.bidmachine.core.Logger;

public class BMLog {

    public static void log(String key, String message) {
        Logger.log(String.format("%s: %s", key, message));
    }

    public static void log(Throwable throwable) {
        Logger.log(throwable);
    }

}
