package io.bidmachine;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import io.bidmachine.unified.UnifiedAdRequestParams;
import io.bidmachine.unified.UnifiedBannerAd;
import io.bidmachine.unified.UnifiedFullscreenAd;
import io.bidmachine.unified.UnifiedNativeAd;

import java.util.Map;

/**
 * All adapters must extends this class
 */
public abstract class NetworkAdapter {

    private final String key;
    private final String version;
    private final AdsType[] supportedTypes;

    private boolean isInitialized;

    public NetworkAdapter(@NonNull String key, @NonNull String version, @NonNull AdsType[] supportedTypes) {
        this.key = key;
        this.version = version;
        this.supportedTypes = supportedTypes;
    }

    public String getKey() {
        return key;
    }

    public String getVersion() {
        return version;
    }

    AdsType[] getSupportedTypes() {
        return supportedTypes;
    }

    /**
     * Call for initialize BidMachine
     */
    public final void initialize(@NonNull ContextProvider context,
                                 @NonNull UnifiedAdRequestParams adRequestParams,
                                 @Nullable Map<String, Object> config) throws Throwable {
        if (!isInitialized) {
            onInitialize(context, adRequestParams, config);
            isInitialized = true;
        }
    }

    protected void onInitialize(@NonNull ContextProvider context,
                                @NonNull UnifiedAdRequestParams adRequestParams,
                                @Nullable Map<String, Object> config) {
    }

    public UnifiedBannerAd createBanner() {
        throw new IllegalArgumentException(getKey() + " adapter not supported banner");
    }

    public UnifiedFullscreenAd createInterstitial() {
        throw new IllegalArgumentException(getKey() + " adapter not supported static interstitial");
    }

    public UnifiedFullscreenAd createRewarded() {
        throw new IllegalArgumentException(getKey() + " adapter not supported rewarded interstitial");
    }

    public UnifiedNativeAd createNativeAd() {
        throw new IllegalArgumentException(getKey() + " adapter not supported native");
    }

    /**
     * Enable logging in adapter. Will be called before load of any ad type
     *
     * @param enabled {@code true} to enable logging
     */
    public void setLogging(boolean enabled) {
    }

}