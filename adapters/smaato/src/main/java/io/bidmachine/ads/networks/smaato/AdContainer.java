package io.bidmachine.ads.networks.smaato;

import android.support.annotation.Nullable;

class AdContainer<T> {

    private T ad;
    private boolean isShown;

    void setAd(T ad) {
        this.ad = ad;
    }

    @Nullable
    T getAd() {
        return ad;
    }

    void setShown(boolean shown) {
        isShown = shown;
    }

    boolean isShown() {
        return isShown;
    }

    void destroy() {
        ad = null;
    }

}
