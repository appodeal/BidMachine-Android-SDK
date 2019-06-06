package io.bidmachine.adapters.nast;

import android.content.Context;

import io.bidmachine.core.Logger;
import io.bidmachine.core.Utils;
import io.bidmachine.displays.NativeAdObjectParams;
import io.bidmachine.nativead.NativeAdObject;
import io.bidmachine.nativead.utils.NativeNetworkExecutor;
import io.bidmachine.utils.BMError;

class NastNativeAdObject extends NativeAdObject {

    NastNativeAdObject(NativeAdObjectParams adObjectParams) {
        super(adObjectParams);
    }

    public void load() {
        try {
            loadAsset();
        } catch (Exception e) {
            Logger.log(e);
            processLoadFail(BMError.Internal);
        }
    }

    @Override
    protected void onClicked(Context context) {
        showProgressDialog(context);
        Utils.openBrowser(context, getParams().getClickUrl(), NativeNetworkExecutor.getInstance(),
                new Runnable() {
                    @Override
                    public void run() {
                        hideProgressDialog();
                    }
                });
    }

}