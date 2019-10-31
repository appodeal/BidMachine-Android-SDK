package io.bidmachine.ads.networks.smaato;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import io.bidmachine.unified.UnifiedAdCallback;
import io.bidmachine.unified.UnifiedMediationParams;
import io.bidmachine.unified.UnifiedParams;
import io.bidmachine.utils.BMError;

public class SmaatoParams extends UnifiedParams {

    final String adSpaceId;

    SmaatoParams(@NonNull UnifiedMediationParams mediationParams) {
        super(mediationParams);
        adSpaceId = mediationParams.getString(SmaatoConfig.KEY_AD_SPACE_ID);
    }

    @Override
    public boolean isValid(@NonNull UnifiedAdCallback callback) {
        if (TextUtils.isEmpty(adSpaceId)) {
            callback.onAdLoadFailed(BMError.requestError("ad_space_id not provided"));
            return false;
        }
        return true;
    }

}