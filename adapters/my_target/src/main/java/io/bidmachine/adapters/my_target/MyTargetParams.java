package io.bidmachine.adapters.my_target;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import io.bidmachine.unified.UnifiedAdCallback;
import io.bidmachine.unified.UnifiedMediationParams;
import io.bidmachine.unified.UnifiedParams;
import io.bidmachine.utils.BMError;

class MyTargetParams extends UnifiedParams {

    final int slotId;
    final String bidId;

    MyTargetParams(@NonNull UnifiedMediationParams mediationParams) {
        super(mediationParams);
        slotId = mediationParams.getInt(MyTargetConfig.KEY_SLOT_ID);
        bidId = mediationParams.getString(MyTargetConfig.KEY_BID_ID);
    }

    @Override
    public boolean isValid(@NonNull UnifiedAdCallback callback) {
        if (!getMediationParams().contains(MyTargetConfig.KEY_SLOT_ID)) {
            callback.onAdLoadFailed(BMError.requestError("slot_id not provided"));
            return false;
        }
        if (TextUtils.isEmpty(bidId)) {
            callback.onAdLoadFailed(BMError.requestError("bid_id not provided"));
            return false;
        }
        return true;
    }
}
