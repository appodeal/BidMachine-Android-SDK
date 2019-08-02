package io.bidmachine;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import io.bidmachine.unified.UnifiedAdRequestParams;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Class for store and provide Network specific configuration.
 * Inherits should implement at least constructor with {@link Map<String, String>} argument, which is required for load
 * config from json.
 */
public abstract class NetworkConfig {

    @Nullable
    private NetworkAdapter networkAdapter;
    @Nullable
    private Map<String, String> networkParams;
    @Nullable
    private Map<String, String> mediationConfig;
    @Nullable
    private EnumMap<AdsFormat, Map<String, String>> typedMediationConfigs;
    @Nullable
    private AdsType[] supportedAdsTypes;
    @Nullable
    private AdsType[] mergedAdsTypes;

    protected NetworkConfig(@Nullable Map<String, String> networkParams) {
        withNetworkParams(networkParams);
    }

    @NonNull
    protected abstract NetworkAdapter createNetworkAdapter();

    /**
     * @return unique Network key
     */
    @NonNull
    public String getKey() {
        return obtainNetworkAdapter().getKey();
    }

    /**
     * @return Network version
     */
    @Nullable
    public String getVersion() {
        return obtainNetworkAdapter().getVersion();
    }

    /**
     * @return Network {@link NetworkAdapter} implementation
     */
    @NonNull
    public NetworkAdapter obtainNetworkAdapter() {
        if (networkAdapter == null) {
            networkAdapter = createNetworkAdapter();
        }
        return networkAdapter;
    }

    /**
     * @return Network global configuration (will be used for {@link NetworkAdapter#initialize(ContextProvider, UnifiedAdRequestParams, NetworkConfig)})
     */
    @Nullable
    public Map<String, String> getNetworkParams() {
        return networkParams;
    }

    /**
     * Set Network global configuration (will be used for {@link NetworkAdapter#initialize(ContextProvider, UnifiedAdRequestParams, NetworkConfig)})
     *
     * @param config map of parameters which will be used for Network initialization
     */
    @SuppressWarnings("unchecked")
    public <T extends NetworkConfig> T withNetworkParams(@Nullable Map<String, String> config) {
        this.networkParams = config;
        return (T) this;
    }

    /**
     * Set Network mediation configuration (will be used for {@link HeaderBiddingAdapter#collectHeaderBiddingParams(ContextProvider, UnifiedAdRequestParams, HeaderBiddingCollectParamsCallback, Map)}).
     * Will be used as default for all {@link AdsFormat}
     *
     * @param config map of parameters which will be used for Network mediation process
     */
    @SuppressWarnings({"unchecked", "WeakerAccess"})
    public <T extends NetworkConfig> T withMediationConfig(@Nullable Map<String, String> config) {
        this.mediationConfig = config;
        return (T) this;
    }

    /**
     * Set Network mediation configuration (will be used for {@link HeaderBiddingAdapter#collectHeaderBiddingParams(ContextProvider, UnifiedAdRequestParams, HeaderBiddingCollectParamsCallback, Map)}).
     * Will be used only for provided {@link AdsFormat}
     *
     * @param adsFormat specific {@link AdsFormat} for which should be used provide {@param config}
     * @param config    map of parameters which will be used for Network mediation process
     */
    @SuppressWarnings({"unchecked", "WeakerAccess"})
    public <T extends NetworkConfig> T withMediationConfig(@NonNull AdsFormat adsFormat, @Nullable Map<String, String> config) {
        if (config == null) {
            if (typedMediationConfigs != null) {
                typedMediationConfigs.remove(adsFormat);
            }
        } else {
            if (typedMediationConfigs == null) {
                typedMediationConfigs = new EnumMap<>(AdsFormat.class);
            }
            onMediationConfigAdded(adsFormat, config);
            typedMediationConfigs.put(adsFormat, config);
        }
        return (T) this;
    }

    protected void onMediationConfigAdded(@NonNull AdsFormat adsFormat, @NonNull Map<String, String> config) {
    }

    /**
     * Set {@link AdsType}s for which Network can be used
     *
     * @param adsType required {@link AdsType}s
     */
    public NetworkConfig forAdTypes(@NonNull AdsType... adsType) {
        this.supportedAdsTypes = adsType;
        return this;
    }

    /**
     * Method which return parameters which should be used for mediation process.
     * If no specific parameters was provided will return default set by {@link NetworkConfig#withMediationConfig(Map)}
     *
     * @param adsType         required {@link AdsType}
     * @param adRequestParams provided typed {@link UnifiedAdRequestParams}
     * @return map of parameters for provided {@link AdsType} and {@link AdContentType} which will be used for mediation process
     */
    @Nullable
    public <T extends UnifiedAdRequestParams> Map<String, String> peekMediationConfig(@NonNull AdsType adsType,
                                                                                      @NonNull T adRequestParams) {
        Map<String, String> resultConfig = null;
        if (typedMediationConfigs != null) {
            for (Map.Entry<AdsFormat, Map<String, String>> entry : typedMediationConfigs.entrySet()) {
                if (entry.getKey().isMatch(adsType, adRequestParams)) {
                    resultConfig = new HashMap<>(entry.getValue());
                }
            }
        }
        if (resultConfig == null && mediationConfig != null) {
            resultConfig = new HashMap<>(mediationConfig);
        }
        return resultConfig;
    }

    /**
     * Method which return array of merged {@link NetworkAdapter#getSupportedTypes()} and {@link NetworkConfig#getSupportedAdsTypes()}.
     * Will be called only once per app session.
     *
     * @return array of supported {@link AdsType}s
     */
    AdsType[] getSupportedAdsTypes() {
        if (mergedAdsTypes == null) {
            AdsType[] adapterSupportedTypes = obtainNetworkAdapter().getSupportedTypes();
            ArrayList<AdsType> resultList = new ArrayList<>();
            for (AdsType adsType : adapterSupportedTypes) {
                if (supportedAdsTypes == null || contains(supportedAdsTypes, adsType)) {
                    resultList.add(adsType);
                }
            }
            mergedAdsTypes = resultList.toArray(new AdsType[0]);
        }
        return mergedAdsTypes;
    }

    private boolean contains(Object[] array, Object v) {
        for (Object o : array) {
            if (o == v) return true;
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NetworkConfig that = (NetworkConfig) o;
        return getKey().equals(that.getKey());
    }

    @Override
    public int hashCode() {
        return getKey().hashCode();
    }
}