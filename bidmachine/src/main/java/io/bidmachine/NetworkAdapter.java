package io.bidmachine;

import android.support.annotation.NonNull;
import io.bidmachine.unified.UnifiedAdRequestParams;
import io.bidmachine.unified.UnifiedBannerAd;
import io.bidmachine.unified.UnifiedFullscreenAd;
import io.bidmachine.unified.UnifiedNativeAd;

/**
 * Class for implement Network initialization and specific Ads types creation
 */
public abstract class NetworkAdapter {

    private final String key;
    private final String version;
    private final String adapterVersion;
    private final AdsType[] supportedTypes;

    private boolean isInitialized;

    protected NetworkAdapter(@NonNull String key,
                             @NonNull String version,
                             @NonNull String adapterVersion,
                             @NonNull AdsType[] supportedTypes) {
        this.key = key;
        this.version = version;
        this.adapterVersion = adapterVersion;
        this.supportedTypes = supportedTypes;
    }

    /**
     * @return unique Network key
     */
    public String getKey() {
        return key;
    }

    /**
     * @return Network version
     */
    public String getVersion() {
        return version;
    }

    /**
     * @return Network adapter version
     */
    public String getAdapterVersion() {
        return adapterVersion;
    }

    /**
     * @return Network supported ads types
     */
    AdsType[] getSupportedTypes() {
        return supportedTypes;
    }

    /**
     * Call for initialize Network
     */
    public final void initialize(@NonNull ContextProvider contextProvider,
                                 @NonNull UnifiedAdRequestParams adRequestParams,
                                 @NonNull NetworkConfigParams networkConfigParams) throws Throwable {
        if (!isInitialized) {
            onInitialize(contextProvider, adRequestParams, networkConfigParams);
            isInitialized = true;
        }
    }

    protected void onInitialize(@NonNull ContextProvider contextProvider,
                                @NonNull UnifiedAdRequestParams adRequestParams,
                                @NonNull NetworkConfigParams networkConfigParams) {
    }

    /**
     * Method for create specific per Network Banner Ads
     */
    public UnifiedBannerAd createBanner() {
        throw new IllegalArgumentException(getKey() + " adapter not supported banner");
    }

    /**
     * Method for create specific per Network Interstitial Ads
     */
    public UnifiedFullscreenAd createInterstitial() {
        throw new IllegalArgumentException(getKey() + " adapter not supported static interstitial");
    }

    /**
     * Method for create specific per Network Rewarded Ads
     */
    public UnifiedFullscreenAd createRewarded() {
        throw new IllegalArgumentException(getKey() + " adapter not supported rewarded interstitial");
    }

    /**
     * Method for create specific per Network Native Ads
     */
    public UnifiedNativeAd createNativeAd() {
        throw new IllegalArgumentException(getKey() + " adapter not supported native");
    }

    /**
     * Enable logging in adapter. Will be called after this parameter was changed via {@link BidMachine#setLoggingEnabled(boolean)}
     *
     * @param enabled {@code true} to enable logging
     */
    public void setLogging(boolean enabled) {
    }

}