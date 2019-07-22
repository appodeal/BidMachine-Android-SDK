package io.bidmachine;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public final class NetworkConfig {

    @NonNull
    private final BidMachineAdapter adapter;
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

    public NetworkConfig(@NonNull BidMachineAdapter adapter) {
        this.adapter = adapter;
    }

    @NonNull
    public BidMachineAdapter getAdapter() {
        return adapter;
    }

    @Nullable
    Map<String, Object> getNetworkConfig() {
        return networkConfig;
    }

    public NetworkConfig withNetworkConfig(@Nullable Map<String, Object> config) {
        this.networkConfig = config;
        return this;
    }

    public NetworkConfig withMediationConfig(@Nullable Map<String, Object> config) {
        this.mediationConfig = config;
        return this;
    }

    public NetworkConfig withMediationConfig(@NonNull AdsFormat adsFormat, @Nullable Map<String, Object> config) {
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
        return this;
    }

    public NetworkConfig forAdTypes(@NonNull AdsType... adsType) {
        this.supportedAdsTypes = adsType;
        return this;
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

    private boolean contains(Object[] array, Object v) {
        for (Object o : array) {
            if (o == v) return true;
        }
        return false;
    }

}