package io.bidmachine.unified;

import io.bidmachine.models.DataRestrictions;
import io.bidmachine.models.DeviceInfo;
import io.bidmachine.models.TargetingInfo;

public interface UnifiedAdRequestParams {

    DataRestrictions getDataRestrictions();

    TargetingInfo getTargetingParams();

    DeviceInfo getDeviceInfo();

    boolean isTestMode();

}
