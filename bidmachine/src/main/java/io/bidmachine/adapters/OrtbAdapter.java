package io.bidmachine.adapters;

import android.content.Context;
import android.support.annotation.NonNull;

import io.bidmachine.FullScreenAdObject;
import io.bidmachine.ViewAdObject;
import io.bidmachine.core.Logger;
import io.bidmachine.displays.DisplayAdObjectParams;
import io.bidmachine.displays.FullScreenAdObjectParams;
import io.bidmachine.displays.NativeAdObjectParams;
import io.bidmachine.models.AdObject;
import io.bidmachine.nativead.NativeAdObject;

/**
 * All adapters must extends this class
 */
public abstract class OrtbAdapter {

    private final String key;
    private final String version;

    private boolean isInitialized;

    public OrtbAdapter(String key, String version) {
        this.key = key;
        this.version = version;
    }

    public String getKey() {
        return key;
    }

    public String getVersion() {
        return version;
    }

    /**
     * Call for initialize BidMachine
     */
    public void initialize(@NonNull Context context) throws Throwable {
        if (!isInitialized) {
            onInitialize(context);
            isInitialized = true;
        } else {
            Logger.log(getClass().getSimpleName() + " already initialized");
        }
    }

    protected void onInitialize(@NonNull Context context) {
    }

    public ViewAdObject createBannerAdObject(DisplayAdObjectParams adObjectParams) {
        throw new IllegalArgumentException(getKey() + " adapter not supported banner");
    }

    public FullScreenAdObject createInterstitialAdObject(FullScreenAdObjectParams adObjectParams) {
        throw new IllegalArgumentException(getKey() + " adapter not supported static interstitial");
    }

    public FullScreenAdObject createRewardedAdObject(FullScreenAdObjectParams adObjectParams) {
        throw new IllegalArgumentException(getKey() + " adapter not supported rewarded interstitial");
    }

    public NativeAdObject createNativeAdObject(NativeAdObjectParams adObjectParams) {
        throw new IllegalArgumentException(getKey() + " adapter not supported native");
    }

    /**
     * Load ad with server response
     */
    public void load(AdObject response) {
        response.load();
    }

    /**
     * Enable logging in adapter. Will be called before load of any ad type
     *
     * @param enabled {@code true} to enable logging
     */
    public void setLogging(boolean enabled) {
    }

}