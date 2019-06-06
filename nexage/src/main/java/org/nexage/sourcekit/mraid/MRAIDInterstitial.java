package org.nexage.sourcekit.mraid;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.VisibleForTesting;
import android.util.Log;

import org.nexage.sourcekit.mraid.internal.MRAIDLog;

public class MRAIDInterstitial implements MRAIDViewListener {

    private final static String TAG = "MRAID";

    private MRAIDInterstitialListener listener;

    @VisibleForTesting
    MRAIDView mraidView;
    @VisibleForTesting
    public boolean isReady;
    public int afd;
    private long showTime;
    private boolean isClosed;

    private MRAIDInterstitial(builder mMRAIDInterstitial) {
        this.listener = mMRAIDInterstitial.listener;
        mraidView = new MRAIDView.builder(mMRAIDInterstitial.context, mMRAIDInterstitial.data, mMRAIDInterstitial.width, mMRAIDInterstitial.height)
                .setBaseUrl(mMRAIDInterstitial.baseUrl)
                .setSupportedNativeFeatures(mMRAIDInterstitial.supportedNativeFeatures)
                .setListener(this)
                .setNativeFeatureListener(mMRAIDInterstitial.nativeFeatureListener)
                .setIsInterstitial(true)
                .setPreload(mMRAIDInterstitial.preload)
                .setCloseTime(mMRAIDInterstitial.closeTime)
                .setIsTag(mMRAIDInterstitial.isTag)
                .setUseLayout(mMRAIDInterstitial.useLayout)
                .build();
        mraidView.load();
    }

    public void show() {
        show(null);
    }

    public void show(Activity activity) {
        if (!isReady) {
            MRAIDLog.w(TAG, "show failed: interstitial is not ready");
            return;
        }
        showTime = System.currentTimeMillis();
        mraidView.showAsInterstitial(activity);
    }

    // MRAIDViewListener implementation

    @Override
    public void mraidViewLoaded(MRAIDView mraidView) {
        Log.d(TAG + "-ViewListener", "mraidViewLoaded");
        isReady = true;
        if (listener != null) {
            listener.mraidInterstitialLoaded(this);
        }
    }

    @Override
    public void mraidViewExpand(MRAIDView mraidView) {
        Log.d(TAG + "-ViewListener", "mraidViewExpand");
        if (listener != null) {
            listener.mraidInterstitialShow(this);
        }
    }

    @Override
    public void mraidViewClose(MRAIDView mraidView) {
        Log.d(TAG + "-ViewListener", "mraidViewClose");
        isReady = false;
        isClosed = true;
        if (afd > 0 && System.currentTimeMillis() - showTime >= afd) {
            trackAppodealXFinish();
        }
        destroy();
        if (listener != null) {
            listener.mraidInterstitialHide(this);
        }
    }

    @Override
    public boolean mraidViewResize(MRAIDView mraidView, int width, int height, int offsetX, int offsetY) {
        return true;
    }

    @Override
    public void mraidViewNoFill(MRAIDView mraidView) {
        Log.d(TAG + "-ViewListener", "mraidViewNoFill");
        isReady = false;
        destroy();
        if (listener != null) {
            listener.mraidInterstitialNoFill(this);
        }
    }

    public void setSegmentAndPlacement(String segmentId, String placementId) {
        if (mraidView != null) {
            mraidView.setSegmentAndPlacement(segmentId, placementId);
        }
    }

    public void trackAppodealXFinish() {
        if (mraidView != null) {
            mraidView.trackAppodealXFinish();
        }
    }

    public void destroy() {
        if (this.mraidView != null) {
            this.mraidView.destroy();
            this.mraidView = null;
        }
    }

    public boolean isClosed() {
        return isClosed;
    }

    public static class builder {
        Context context;
        String baseUrl;
        String data;
        String[] supportedNativeFeatures = null;
        int width;
        int height;
        MRAIDInterstitialListener listener;
        MRAIDNativeFeatureListener nativeFeatureListener;
        boolean preload;
        int closeTime = -1;
        boolean isTag;
        boolean useLayout;

        public builder(Context context, String data, int width, int height) {
            this.context = context;
            this.data = data;
            this.width = width;
            this.height = height;
        }

        public builder setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        public builder setSupportedNativeFeatures(String[] supportedNativeFeatures) {
            this.supportedNativeFeatures = supportedNativeFeatures;
            return this;
        }

        public builder setListener(MRAIDInterstitialListener listener) {
            this.listener = listener;
            return this;
        }

        public builder setNativeFeatureListener(MRAIDNativeFeatureListener nativeFeatureListener) {
            this.nativeFeatureListener = nativeFeatureListener;
            return this;
        }

        public builder setPreload(boolean preload) {
            this.preload = preload;
            return this;
        }

        public builder setCloseTime(int closeTime) {
            this.closeTime = closeTime;
            return this;
        }

        public builder setIsTag(boolean isTag) {
            this.isTag = isTag;
            return this;
        }

        public builder setUseLayout(boolean useLayout) {
            this.useLayout = useLayout;
            return this;
        }

        public MRAIDInterstitial build() {
            return new MRAIDInterstitial(this);
        }
    }
}
