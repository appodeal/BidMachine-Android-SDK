package io.bidmachine;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import io.bidmachine.core.Logger;

public class BidMachine {

    public static final String NAME = "BidMachine SDK";
    public static final String VERSION = BuildConfig.VERSION_NAME;
    public static final int VERSION_CODE = BuildConfig.VERSION_CODE;

    static {
        Logger.setTag(NAME);
    }

    /**
     * Initialize BidMachine SDK
     *
     * @param context  - your application context
     * @param sellerId - your Seller Id
     */
    public static void initialize(@NonNull Context context, @NonNull String sellerId) {
        initialize(context, sellerId, null);
    }

    /**
     * Initialize BidMachine SDK
     *
     * @param context  - your application context
     * @param sellerId - your Seller Id
     * @param callback - you {@link InitializationCallback}
     */
    public static void initialize(@NonNull Context context,
                                  @NonNull String sellerId,
                                  @Nullable InitializationCallback callback) {
        BidMachineImpl.get().initialize(context, sellerId, callback);
    }

    /**
     * Check if BidMachine SDK was initialized
     *
     * @return - {@code true} if BidMachine SDK was already initialized
     */
    public static boolean isInitialized() {
        return BidMachineImpl.get().isInitialized();
    }

    /**
     * Set BidMachine SDK endpoint
     *
     * @param url - BidMachine endpoint URL
     */
    public static void setEndpoint(@NonNull String url) {
        BidMachineImpl.get().setEndpoint(url);
    }

    /**
     * Set BidMachine SDK logs enabled
     *
     * @param enabled - if {@code true} SDK will print all information about ad requests
     */
    public static void setLoggingEnabled(boolean enabled) {
        Logger.setLoggingEnabled(enabled);
        NetworkRegistry.setLoggingEnabled(enabled);
    }

    /**
     * Set BidMachine SDK test mode
     *
     * @param testMode = if {@code true} SDK will run in test mode
     */
    public static void setTestMode(boolean testMode) {
        BidMachineImpl.get().setTestMode(testMode);
    }

    /**
     * Set default {@link TargetingParams} for all advertising requests
     */
    public static void setTargetingParams(@Nullable TargetingParams targetingParams) {
        BidMachineImpl.get().setTargetingParams(targetingParams);
    }

    /**
     * Set consent config
     *
     * @param hasConsent    - user has given consent to the processing of personal data relating to him or her. https://www.eugdpr.org/
     * @param consentString - GDPR consent string if applicable, complying with the comply with the IAB standard
     *                      <a href="https://github.com/InteractiveAdvertisingBureau/GDPR-Transparency-and-Consent-Framework/blob/master/Consent%20string%20and%20vendor%20list%20formats%20v1.1%20Final.md">Consent String Format</a>
     *                      in the <a href="https://github.com/InteractiveAdvertisingBureau/GDPR-Transparency-and-Consent-Framework">Transparency and Consent Framework</a> technical specifications
     */
    public static void setConsentConfig(boolean hasConsent, @Nullable String consentString) {
        BidMachineImpl.get().getUserRestrictionParams().setConsentConfig(hasConsent, consentString);
    }

    /**
     * Set subject to GDPR
     *
     * @param subject - Flag indicating if GDPR regulations apply. <a href="https://wikipedia.org/wiki/General_Data_Protection_Regulation">The  General Data Protection Regulation (GDPR)</a> is a regulation of the European Union
     */
    public static void setSubjectToGDPR(@Nullable Boolean subject) {
        BidMachineImpl.get().getUserRestrictionParams().setSubjectToGDPR(subject);
    }

    /**
     * Set coppa
     *
     * @param coppa - Flag indicating if COPPA regulations apply. <a href="https://wikipedia.org/wiki/Children%27s_Online_Privacy_Protection_Act">The Children's Online Privacy Protection Act (COPPA)</a> was established by the U.S. Federal Trade Commission
     */
    public static void setCoppa(@Nullable Boolean coppa) {
        BidMachineImpl.get().getUserRestrictionParams().setCoppa(coppa);
    }

    /**
     * Add 3rd party network configuration, which will be used for mediation, loading and displaying ads
     *
     * @param networkConfigs - Custom configuration object per network
     */
    public static void registerNetworks(@NonNull NetworkConfig... networkConfigs) {
        NetworkRegistry.registerNetworks(networkConfigs);
    }

    /**
     * Add 3rd party network configuration, which will be used for mediation, loading and displaying ads
     *
     * @param jsonData - Json array which contains info about required networks
     */
    public static void registerNetworks(@NonNull String jsonData) {
        NetworkRegistry.registerNetworks(jsonData);
    }

}
