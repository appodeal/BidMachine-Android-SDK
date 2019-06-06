package io.bidmachine.test.app;

import android.app.Application;

import io.bidmachine.BidMachine;

public class BMTestApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        BidMachine.setLoggingEnabled(true);
    }

}
