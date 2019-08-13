package io.bidmachine;

import android.support.annotation.Nullable;

import java.util.EnumMap;
import java.util.Map;

public interface NetworkConfigParams {

    @Nullable
    Map<String, String> obtainNetworkParams();

    @Nullable
    EnumMap<AdsFormat, Map<String, String>> obtainNetworkMediationConfigs(AdsFormat... adsFormats);
}
