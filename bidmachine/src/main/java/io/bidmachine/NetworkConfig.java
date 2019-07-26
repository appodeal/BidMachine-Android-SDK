package io.bidmachine;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import io.bidmachine.unified.UnifiedAdRequestParams;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Class for store and provide Network specific configuration
 */
public abstract class NetworkConfig {

    @NonNull
    private final NetworkAdapter adapter;
    @Nullable
    private Map<String, Object> networkConfig;
    @Nullable
    private Map<String, Object> mediationConfig;
    @Nullable
    private EnumMap<AdsFormat, Map<String, Object>> typedMediationConfigs;
    @Nullable
    private AdsType[] supportedAdsTypes;
    @Nullable
    private AdsType[] mergedAdsTypes;

    protected NetworkConfig(@NonNull NetworkAdapter adapter) {
        this.adapter = adapter;
    }

    /**
     * @return unique Network key
     */
    public String getKey() {
        return getAdapter().getKey();
    }

    /**
     * @return Network version
     */
    public String getVersion() {
        return getAdapter().getVersion();
    }

    /**
     * @return Network {@link NetworkAdapter} implementation
     */
    @NonNull
    public NetworkAdapter getAdapter() {
        return adapter;
    }

    /**
     * @return Network global configuration (will be used for {@link NetworkAdapter#initialize(ContextProvider, UnifiedAdRequestParams, Map)})
     */
    @Nullable
    Map<String, Object> getNetworkConfig() {
        return networkConfig;
    }

    /**
     * Set Network global configuration (will be used for {@link NetworkAdapter#initialize(ContextProvider, UnifiedAdRequestParams, Map)})
     *
     * @param config map of parameters which will be used for Network initialization
     */
    @SuppressWarnings("unchecked")
    public <T extends NetworkConfig> T withNetworkConfig(@Nullable Map<String, Object> config) {
        this.networkConfig = config;
        return (T) this;
    }

    /**
     * Set Network mediation configuration (will be used for {@link HeaderBiddingAdapter#collectHeaderBiddingParams(ContextProvider, UnifiedAdRequestParams, HeaderBiddingCollectParamsCallback, Map)}).
     * Will be used as default for all {@link AdsFormat}
     *
     * @param config map of parameters which will be used for Network mediation process
     */
    @SuppressWarnings({"unchecked", "WeakerAccess"})
    public <T extends NetworkConfig> T withMediationConfig(@Nullable Map<String, Object> config) {
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
    public <T extends NetworkConfig> T withMediationConfig(@NonNull AdsFormat adsFormat, @Nullable Map<String, Object> config) {
        if (config == null) {
            if (typedMediationConfigs != null) {
                typedMediationConfigs.remove(adsFormat);
            }
        } else {
            if (typedMediationConfigs == null) {
                typedMediationConfigs = new EnumMap<>(AdsFormat.class);
            }
            typedMediationConfigs.put(adsFormat, config);
        }
        return (T) this;
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
     * @param adsType     required {@link AdsType}
     * @param contentType required {@link AdContentType}
     * @return map of parameters for provided {@link AdsType} and {@link AdContentType} which will be used for mediation process
     */
    @Nullable
    public Map<String, Object> peekMediationConfig(@NonNull AdsType adsType,
                                                   @NonNull AdContentType contentType) {
        Map<String, Object> resultConfig = null;
        if (typedMediationConfigs != null) {
            for (Map.Entry<AdsFormat, Map<String, Object>> entry : typedMediationConfigs.entrySet()) {
                if (entry.getKey().isMatch(adsType, contentType)) {
                    resultConfig = new HashMap<>(entry.getValue());
                    break;
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
            AdsType[] adapterSupportedTypes = getAdapter().getSupportedTypes();
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

}