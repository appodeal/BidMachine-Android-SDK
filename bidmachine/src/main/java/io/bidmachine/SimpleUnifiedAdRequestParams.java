package io.bidmachine;

import android.support.annotation.NonNull;
import io.bidmachine.models.DataRestrictions;
import io.bidmachine.models.TargetingInfo;
import io.bidmachine.unified.UnifiedAdRequestParams;

class SimpleUnifiedAdRequestParams implements UnifiedAdRequestParams {

    @NonNull
    private final DataRestrictions dataRestrictions;
    @NonNull
    private final TargetingInfo targetingInfo;

    SimpleUnifiedAdRequestParams(@NonNull DataRestrictions dataRestrictions,
                                 @NonNull TargetingInfo targetingInfo) {
        this.dataRestrictions = dataRestrictions;
        this.targetingInfo = targetingInfo;
    }

    @NonNull
    @Override
    public DataRestrictions getDataRestrictions() {
        return dataRestrictions;
    }

    @Override
    public TargetingInfo getTargetingParams() {
        return targetingInfo;
    }

    @Override
    public boolean isTestMode() {
        return BidMachineImpl.get().isTestMode();
    }

}