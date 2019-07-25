package io.bidmachine;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

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

    @NonNull
    public NetworkAdapter getAdapter() {
        return adapter;
    }

    public String getKey() {
        return adapter.getKey();
    }

    public String getVersion() {
        return adapter.getVersion();
    }

    @Nullable
    Map<String, Object> getNetworkConfig() {
        return networkConfig;
    }

    @SuppressWarnings("unchecked")
    public <T extends NetworkConfig> T withNetworkConfig(@Nullable Map<String, Object> config) {
        this.networkConfig = config;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public <T extends NetworkConfig> T withMediationConfig(@Nullable Map<String, Object> config) {
        this.mediationConfig = config;
        return (T) this;
    }

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

    public NetworkConfig forAdTypes(@NonNull AdsType... adsType) {
        this.supportedAdsTypes = adsType;
        return this;
    }

    @Nullable
    public Map<String, Object> peekMediationConfig(@NonNull AdsType adsType,
                                                   @NonNull AdContentType contentType) {
        Map<String, Object> resultConfig = null;
        if (typedMediationConfigs != null) {
            for (Map.Entry<AdsFormat, Map<String, Object>> entry : typedMediationConfigs.entrySet()) {
                if (entry.getKey().isMatch(adsType, contentType)) {
                    resultConfig = entry.getValue();
                    break;
                }
            }
        }
        if (resultConfig != null && mediationConfig != null) {
            resultConfig = new HashMap<>(mediationConfig);
        }
        return resultConfig;
    }

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