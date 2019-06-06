package io.bidmachine;

import io.bidmachine.core.Utils;
import io.bidmachine.displays.FullScreenAdObjectParams;

public abstract class FullScreenAdObject<AdType extends OrtbAd>
        extends AdObjectImpl<AdType, FullScreenAdObjectParams>
        implements IFullScreenAd {

    private ImpressionThresholdTask thresholdTask = new ImpressionThresholdTask() {
        @Override
        void onTracked() {
            processImpression();
        }
    };

    public FullScreenAdObject(FullScreenAdObjectParams adObjectParams) {
        super(adObjectParams);
    }

    public int getSkipAfterTimeSec() {
        return getParams().getSkipAfterTimeSec();
    }

    @Override
    protected void onShown() {
        super.onShown();
        startImpressionThresholdTask();
    }

    @Override
    public void processClosed(boolean finished) {
        super.processClosed(finished);
        cancelImpressionThresholdTask();
    }

    @Override
    public void processFinished() {
        super.processFinished();
        cancelImpressionThresholdTask();
    }

    @Override
    protected void onImpression() {
        super.onImpression();
        cancelImpressionThresholdTask();
    }

    protected void startImpressionThresholdTask() {
        thresholdTask.start(getParams().getViewabilityTimeThresholdMs());
    }

    protected void cancelImpressionThresholdTask() {
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

}