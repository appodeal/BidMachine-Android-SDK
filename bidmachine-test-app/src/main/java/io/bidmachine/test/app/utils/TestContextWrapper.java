package io.bidmachine.test.app.utils;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.PackageManager;

import io.bidmachine.test.app.ParamsHelper;

public class TestContextWrapper extends ContextWrapper {

    public TestContextWrapper(Context base) {
        super(base);
    }

    public String getPackageName() {
        String appBundle = ParamsHelper.getInstance(this, ParamsHelper.AdsType.Global).getAppBundle();
        return appBundle != null ? appBundle : super.getPackageName();
    }

    @Override
    public PackageManager getPackageManager() {
        return new TestPackageManagerWrapper(getBaseContext(), super.getPackageManager());
    }
}
