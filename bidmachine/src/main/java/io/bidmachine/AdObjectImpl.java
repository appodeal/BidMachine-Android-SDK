package io.bidmachine;

import android.content.Context;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import io.bidmachine.models.AdObject;
import io.bidmachine.models.AdObjectParams;
import io.bidmachine.unified.UnifiedAd;
import io.bidmachine.unified.UnifiedAdCallback;
import io.bidmachine.unified.UnifiedAdRequestParams;
import io.bidmachine.utils.BMError;
import io.bidmachine.utils.ContextProvider;

import java.util.Map;

public abstract class AdObjectImpl<
        AdRequestType extends AdRequest<AdRequestType, UnifiedAdRequestParamsType>,
        AdObjectParamsType extends AdObjectParams,
        UnifiedAdType extends UnifiedAd<UnifiedAdCallbackType, UnifiedAdRequestParamsType>,
        UnifiedAdCallbackType extends UnifiedAdCallback,
        UnifiedAdRequestParamsType extends UnifiedAdRequestParams>
        implements AdObject<AdObjectParamsType>, ContextProvider {

    @NonNull
    private final ContextProvider contextProvider;
    @NonNull
    private final AdProcessCallback processCallback;
    @NonNull
    private final AdRequestType adRequest;
    @NonNull
    private final AdObjectParamsType adObjectParams;
    @NonNull
    private final UnifiedAdType unifiedAd;
    @NonNull
    private final UnifiedAdCallbackType unifiedAdCallback;

    public AdObjectImpl(@NonNull ContextProvider contextProvider,
                        @NonNull AdProcessCallback processCallback,
                        @NonNull AdRequestType adRequest,
                        @NonNull AdObjectParamsType adObjectParams,
                        @NonNull UnifiedAdType unifiedAd) {
        this.contextProvider = contextProvider;
        this.processCallback = processCallback;
        this.adRequest = adRequest;
        this.adObjectParams = adObjectParams;
        this.unifiedAd = unifiedAd;
        this.unifiedAdCallback = createUnifiedCallback(processCallback);
    }

    @Nullable
    @Override
    public Context getContext() {
        return contextProvider.getContext();
    }

    @NonNull
    public abstract UnifiedAdCallbackType createUnifiedCallback(@NonNull AdProcessCallback processCallback);

    @NonNull
    public AdRequestType getAdRequest() {
        return adRequest;
    }

    @NonNull
    @Override
    public AdObjectParamsType getParams() {
        return adObjectParams;
    }

    @NonNull
    public UnifiedAdType getUnifiedAd() {
        return unifiedAd;
    }

    @NonNull
    public UnifiedAdCallbackType getUnifiedAdCallback() {
        return unifiedAdCallback;
    }

    @NonNull
    public AdProcessCallback getProcessCallback() {
        return processCallback;
    }

    @Override
    public void load(@NonNull Context context, @Nullable Map<String, Object> extra) {
        unifiedAd.load(context,
                unifiedAdCallback,
                adRequest.getUnifiedRequestParams(),
                adObjectParams.toMediationParams(),
                extra);
    }

    @CallSuper
    @Override
    public void onShown() {
    }

    @CallSuper
    @Override
    public void onImpression() {
    }

    @CallSuper
    @Override
    public void onClicked() {
    }

    @CallSuper
    @Override
    public void onFinished() {
    }

    @CallSuper
    @Override
    public void onClosed() {
    }

    @CallSuper
    @Override
    public void onDestroy() {
    }

    protected static class BaseUnifiedAdCallback implements UnifiedAdCallback {

        @NonNull
        AdProcessCallback processCallback;

        public BaseUnifiedAdCallback(@NonNull AdProcessCallback processCallback) {
            this.processCallback = processCallback;
        }

        @Override
        public void onAdLoadFailed(BMError error) {
            processCallback.processLoadFail(error);
        }

        @Override
        public void onAdClicked() {
            processCallback.processClicked();
        }

        @Override
        public void onAdShowFailed(BMError error) {
            processCallback.processShowFail(error);
        }

        @Override
        public void onAdExpired() {
            processCallback.processExpired();
        }
    }
}
