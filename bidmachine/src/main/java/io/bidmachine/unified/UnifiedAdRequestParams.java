package io.bidmachine.unified;

import io.bidmachine.TargetingParams;
import io.bidmachine.models.DataRestrictions;

public interface UnifiedAdRequestParams {

    DataRestrictions getDataRestrictions();

    TargetingParams getTargetingParams();

}
