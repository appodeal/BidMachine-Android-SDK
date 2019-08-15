package io.bidmachine.unified;

import io.bidmachine.models.DataRestrictions;
import io.bidmachine.models.TargetingInfo;

public interface UnifiedAdRequestParams {

    DataRestrictions getDataRestrictions();

    TargetingInfo getTargetingParams();

    boolean isTestMode();

}
