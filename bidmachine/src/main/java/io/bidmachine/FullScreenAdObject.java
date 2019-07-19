package io.bidmachine;

import android.content.Context;
import android.support.annotation.NonNull;
import io.bidmachine.core.Utils;
import io.bidmachine.models.AdObjectParams;
import io.bidmachine.unified.UnifiedFullscreenAd;
import io.bidmachine.unified.UnifiedFullscreenAdCallback;
import io.bidmachine.unified.UnifiedFullscreenAdRequestParams;
import io.bidmachine.utils.ContextProvider;

public final class FullScreenAdObject<AdRequestType extends FullScreenAdRequest<AdRequestType>>
        extends AdObjectImpl<AdRequestType, AdObjectParams, UnifiedFullscreenAd, UnifiedFullscreenAdCallback, UnifiedFullscreenAdRequestParams>
        implements IFullScreenAd {

    private ImpressionThresholdTask thresholdTask = new ImpressionThresholdTask() {
        @Override
        void onTracked() {
            getProcessCallback().processImpression();
        }
    };

    public FullScreenAdObject(@NonNull ContextProvider contextProvider,
                              @NonNull AdProcessCallback processCallback,
                              @NonNull AdRequestType adRequest,
                              @NonNull AdObjectParams adObjectParams,
                              @NonNull UnifiedFullscreenAd unifiedAd) {
        super(contextProvider, processCallback, adRequest, adObjectParams, unifiedAd);
    }

    @NonNull
    @Override
    public UnifiedFullscreenAdCallback createUnifiedCallback(@NonNull AdProcessCallback processCallback) {
        return new UnifiedFullscreenAdCallbackImpl(processCallback);
    }

    @Override
    public void show(@NonNull Context context) {
        getUnifiedAd().show(context, getUnifiedAdCallback());
    }

    @Override
    public void onShown() {
        super.onShown();
        startImpressionThresholdTask();
    }

    @Override
    public void onImpression() {
        super.onImpression();
        cancelImpressionThresholdTask();
    }

    @Override
    public void onClosed() {
        super.onClosed();
        cancelImpressionThresholdTask();
    }

    @Override
    public void onFinished() {
        super.onFinished();
        cancelImpressionThresholdTask();
    }

    private void startImpressionThresholdTask() {
        thresholdTask.start(getParams().getViewabilityTimeThresholdMs());
    }

    private void cancelImpressionThresholdTask() {
        thresholdTask.cancel();
    }

    private abstract class ImpressionThresholdTask implements Runnable {

        void start(long threshold) {
            Utils.onBackgroundThread(this, threshold);
        }

        void cancel() {
            Utils.cancelBackgroundThreadTask(this);
        }

        @Override
        public void run() {
            onTracked();
        }

        abstract void onTracked();

    }

    private class UnifiedFullscreenAdCallbackImpl extends BaseUnifiedAdCallback implements UnifiedFullscreenAdCallback {

        UnifiedFullscreenAdCallbackImpl(@NonNull AdProcessCallback processCallback) {
            super(processCallback);
        }

        @Override
        public void onAdLoaded() {
            processCallback.processLoadSuccess();
        }

        @Override
        public void onAdShown() {
            processCallback.processShown();
        }

        @Override
        public void onAdFinished() {
            processCallback.processFinished();
        }

        @Override
        public void onAdClosed() {
            processCallback.processClosed();
        }
    }

}