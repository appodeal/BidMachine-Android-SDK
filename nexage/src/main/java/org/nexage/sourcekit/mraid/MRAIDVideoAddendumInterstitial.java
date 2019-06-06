package org.nexage.sourcekit.mraid;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import org.nexage.sourcekit.mraid.internal.MRAIDLog;

public class MRAIDVideoAddendumInterstitial implements MRAIDVideoAddendumViewListener {

    private final static String TAG = "MRAIDVAI";

    private MRAIDVideoAddendumInterstitialListener listener;

    private MRAIDVideoAddendumView mraidVideoAddendumView;
    private boolean isReady;
    private Activity activity;

    private MRAIDVideoAddendumInterstitial(Context context, String baseUrl, String data, String[] supportedNativeFeatures, int width, int height,
                                          MRAIDVideoAddendumInterstitialListener listener, MRAIDNativeFeatureListener nativeFeatureListener, boolean preload, boolean skippable) {
        this.listener = listener;
        mraidVideoAddendumView = new MRAIDVideoAddendumView.Builder().setContext(context).setBaseUrl(baseUrl)
                .setData(data).setSupportedNativeFeatures(supportedNativeFeatures).setListener(this)
                .setNativeFeatureListener(nativeFeatureListener).setIsInterstitial(true).setWidth(width)
                .setHeight(height).setPreload(preload).setSkippable(skippable).build();

    }

    public void show() {
        show(null);
    }

    public void finish() {
        mraidVideoAddendumView.clearView();
    }

    public void show(Activity activity) {
        if (!isReady) {
            MRAIDLog.w(TAG, "show failed: interstitial is not ready");
            return;
        }
        this.activity = activity;
        mraidVideoAddendumView.showAsInterstitial(activity);
    }

    // MRAIDViewListener implementation

    @Override
    public void mraidVideoAddendumViewLoaded(MRAIDVideoAddendumView mraidView) {
        Log.d(TAG + "-ViewListener", "mraidViewLoaded");
        isReady = true;
        if (listener != null) {
            listener.mraidVideoAddendumInterstitialLoaded(this);
        }
    }

    @Override
    public void mraidVideoAddendumViewExpand(MRAIDVideoAddendumView mraidView) {
        Log.d(TAG + "-ViewListener", "mraidViewExpand");
        if (listener != null) {
            listener.mraidVideoAddendumInterstitialShow(this);
        }
    }

    @Override
    public void mraidVideoAddendumViewClose(MRAIDVideoAddendumView mraidView) {
        Log.d(TAG + "-ViewListener", "mraidViewClose");
        isReady = false;
        destroy();
        if (listener != null) {
            listener.mraidVideoAddendumInterstitialHide(this);
        }
    }

    @Override
    public boolean mraidVideoAddendumViewResize(MRAIDVideoAddendumView mraidView, int width, int height, int offsetX, int offsetY) {
        return true;
    }

    @Override
    public void mraidVideoAddendumViewNoFill(MRAIDVideoAddendumView mraidView) {
        Log.d(TAG + "-ViewListener", "mraidViewNoFill");
        isReady = false;
        destroy();
        if (listener != null) {
            listener.mraidVideoAddendumInterstitialNoFill(this);
        }
    }

    @Override
    public void mraidVideoAddendumViewStarted(MRAIDVideoAddendumView mraidView) {
        Log.d(TAG + "-ViewListener", "AdStarted");
        if (listener != null) {
            listener.mraidVideoAddendumViewStarted(this);
        }
    }

    @Override
    public void mraidVideoAddendumViewStopped(MRAIDVideoAddendumView mraidView) {
        Log.d(TAG + "-ViewListener", "AdStopped");
        if (listener != null) {
            listener.mraidVideoAddendumViewStopped(this);
        }
    }

    @Override
    public void mraidVideoAddendumViewSkipped(MRAIDVideoAddendumView mraidView) {
        Log.d(TAG + "-ViewListener", "AdSkipped");
        if (listener != null) {
            listener.mraidVideoAddendumViewSkipped(this);
        }
    }

    @Override
    public void mraidVideoAddendumViewVideoStart(MRAIDVideoAddendumView mraidView) {
        Log.d(TAG + "-ViewListener", "AdVideoStart");
        if (listener != null) {
            listener.mraidVideoAddendumViewVideoStart(this);
        }
    }

    @Override
    public void mraidVideoAddendumViewFirstQuartile(MRAIDVideoAddendumView mraidView) {
        Log.d(TAG + "-ViewListener", "AdVideoFirstQuartile");
        if (listener != null) {
            listener.mraidVideoAddendumViewFirstQuartile(this);
        }
    }

    @Override
    public void mraidVideoAddendumViewMidpoint(MRAIDVideoAddendumView mraidView) {
        Log.d(TAG + "-ViewListener", "AdVideoMidpoint");
        if (listener != null) {
            listener.mraidVideoAddendumViewMidpoint(this);
        }
    }

    @Override
    public void mraidVideoAddendumViewThirdQuartile(MRAIDVideoAddendumView mraidView) {
        Log.d(TAG + "-ViewListener", "AdVideoThirdQuartile");
        if (listener != null) {
            listener.mraidVideoAddendumViewThirdQuartile(this);
        }
    }

    @Override
    public void mraidVideoAddendumViewComplete(MRAIDVideoAddendumView mraidView) {
        Log.d(TAG + "-ViewListener", "AdVideoComplete");
        if (listener != null) {
            listener.mraidVideoAddendumViewComplete(this);
        }
    }

    @Override
    public void mraidVideoAddendumViewPaused(MRAIDVideoAddendumView mraidView) {
        Log.d(TAG + "-ViewListener", "AdPaused");
        if (listener != null) {
            listener.mraidVideoAddendumViewPaused(this);
        }
    }

    @Override
    public void mraidVideoAddendumViewPlaying(MRAIDVideoAddendumView mraidView) {
        Log.d(TAG + "-ViewListener", "AdPlaying");
        if (listener != null) {
            listener.mraidVideoAddendumViewPlaying(this);
        }
    }

    @Override
    public void mraidVideoAddendumViewError(MRAIDVideoAddendumView mraidView, String error) {
        Log.d(TAG + "-ViewListener", "AdError");
        if (listener != null) {
            listener.mraidVideoAddendumViewError(this, error);
        }
    }

    @Override
    public void mraidVideoAddendumViewClickThru(MRAIDVideoAddendumView mraidView, String url) {
        Log.d(TAG + "-ViewListener", "AdClickThru");
        if (listener != null) {
            listener.mraidVideoAddendumViewClickThru(this, url);
        }
        mraidVideoAddendumView.clearView();
    }

    @Override
    public void mraidVideoAddendumViewUserClose(MRAIDVideoAddendumView mraidView) {
        Log.d(TAG + "-ViewListener", "AdUserClose");
        if (listener != null) {
            listener.mraidVideoAddendumViewUserClose(this);
        }
    }

    @Override
    public void mraidVideoAddendumViewSkippableStateChange(MRAIDVideoAddendumView mraidView, boolean state) {
        Log.d(TAG + "-ViewListener", "AdSkippableStateChange");
        if (listener != null) {
            listener.mraidVideoAddendumViewSkippableStateChange(this, state);
        }
        try {
            java.lang.reflect.Method method = activity.getClass().getMethod("setSkippable", boolean.class);
            method.invoke(activity, state);
        } catch (Exception e) {
            MRAIDLog.e(e.getMessage());
        }
    }

    @Override
    public void mraidVideoAddendumViewLog(MRAIDVideoAddendumView mraidView, String log) {
        Log.d(TAG + "-ViewListener", "AdLog");
        if (listener != null) {
            listener.mraidVideoAddendumViewLog(this, log);
        }
    }


    public void destroy() {
        if (this.mraidVideoAddendumView != null) {
            mraidVideoAddendumView.destroy();
            mraidVideoAddendumView = null;
            activity = null;
        }
    }

    public static class Builder {
        private Context context;
        private String baseUrl;
        private String data;
        private String[] supportedNativeFeatures;
        private int width;
        private int height;
        private MRAIDVideoAddendumInterstitialListener listener;
        private MRAIDNativeFeatureListener nativeFeatureListener;
        private boolean preload = true;
        private boolean skippable = true;

        public Builder() {

        }
        public Builder setContext(Context context) {
            this.context = context;
            return this;
        }

        public Builder setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        public Builder setData(String data) {
            this.data = data;
            return this;
        }

        public Builder setSupportedNativeFeatures(String[] supportedNativeFeatures) {
            this.supportedNativeFeatures = supportedNativeFeatures;
            return this;
        }

        public Builder setListener(MRAIDVideoAddendumInterstitialListener listener) {
            this.listener = listener;
            return this;
        }

        public Builder setNativeFeatureListener(MRAIDNativeFeatureListener nativeFeatureListener) {
            this.nativeFeatureListener = nativeFeatureListener;
            return this;
        }

        public Builder setWidth(int width) {
            this.width = width;
            return this;
        }

        public Builder setHeight(int height) {
            this.height = height;
            return this;
        }

        public Builder setPreload(boolean preload) {
            this.preload = preload;
            return this;
        }

        public Builder setSkippable(boolean skippable) {
            this.skippable = skippable;
            return this;
        }

        public MRAIDVideoAddendumInterstitial build() {
            return new MRAIDVideoAddendumInterstitial(context, baseUrl, data, supportedNativeFeatures, width, height,
            listener, nativeFeatureListener, preload, skippable);
        }

    }

}
