package io.bidmachine.unified;

import android.support.annotation.NonNull;
import io.bidmachine.models.DataRestrictions;
import io.bidmachine.models.TargetingInfo;

public interface UnifiedAdRequestParams {

    DataRestrictions getDataRestrictions();

    TargetingInfo getTargetingParams();

    class SimpleUnifiedAdRequestParams implements UnifiedAdRequestParams {

        @NonNull
        private final DataRestrictions dataRestrictions;
        @NonNull
        private final TargetingInfo targetingInfo;

        public SimpleUnifiedAdRequestParams(@NonNull DataRestrictions dataRestrictions,
                                            @NonNull TargetingInfo targetingInfo) {
            this.dataRestrictions = dataRestrictions;
            this.targetingInfo = targetingInfo;
        }

        @Override
        public DataRestrictions getDataRestrictions() {
            return dataRestrictions;
        }

        @Override
        public TargetingInfo getTargetingParams() {
            return targetingInfo;
        }
    }

}
