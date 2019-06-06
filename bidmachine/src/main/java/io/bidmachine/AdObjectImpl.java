package io.bidmachine;

import android.content.Context;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;

import io.bidmachine.models.AdObject;
import io.bidmachine.models.AdObjectParams;
import io.bidmachine.utils.BMError;

public abstract class AdObjectImpl<
        AdType extends OrtbAd,
        AdObjectParamsType extends AdObjectParams>
        implements AdObject<AdType, AdObjectParamsType> {

    @NonNull
    private final AdObjectParamsType adObjectParams;

    private AdType ad;

    private boolean isShownTracked;
    private boolean isImpressionTracked;

    public AdObjectImpl(@NonNull AdObjectParamsType adObjectParams) {
        this.adObjectParams = adObjectParams;
    }

    public Context getContext() {
        return ad.getContext();
    }

    @Override
    public AdType getAd() {
        return ad;
    }

    @NonNull
    @Override
    public AdObjectParamsType getParams() {
        return adObjectParams;
    }

    @Override
    public void attachAd(AdType ad) {
        this.ad = ad;
    }

    @CallSuper
    @Override
    public void processLoadSuccess() {
        ad.processCallback.processLoadSuccess();
    }

    @CallSuper
    @Override
    public void processLoadFail(BMError error) {
        ad.processCallback.processLoadFail(error);
    }

    @CallSuper
    @Override
    public void processShown() {
        if (!isShownTracked) {
            isShownTracked = true;

            onShown();
            ad.processCallback.processShown();
        }
    }

    @CallSuper
    protected void onShown() {
    }

    @CallSuper
    @Override
    public void processShowFail(BMError error) {
        ad.processCallback.processShowFail(error);
    }

    @CallSuper
    @Override
    public void processClicked() {
        ad.processCallback.processClicked();
    }

    @Override
    public void processImpression() {
        if (!isImpressionTracked) {
            isImpressionTracked = true;

            onImpression();
            ad.processCallback.processImpression();
        }
    }

    @CallSuper
    protected void onImpression() {
    }

    @CallSuper
    @Override
    public void processFinished() {
        ad.processCallback.processFinished();
    }

    @CallSuper
    @Override
    public void processClosed(boolean finished) {
        ad.processCallback.processClosed(finished);
    }

    @Override
    public void processDestroy() {
        ad.processCallback.processDestroy();
    }

    @Override
    public void processExpired() {
    }

    @Override
    public void destroy() {
        onDestroy();
    }

    protected abstract void onDestroy();

}
