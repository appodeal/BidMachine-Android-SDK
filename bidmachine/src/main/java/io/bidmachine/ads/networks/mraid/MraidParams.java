package io.bidmachine.ads.networks.mraid;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import io.bidmachine.unified.UnifiedAdCallback;
import io.bidmachine.unified.UnifiedMediationParams;
import io.bidmachine.unified.UnifiedParams;
import io.bidmachine.utils.BMError;
import io.bidmachine.utils.IabUtils;

class MraidParams extends UnifiedParams {

    final String creativeAdm;
    final int width;
    final int height;
    final boolean canPreload;

    MraidParams(@NonNull UnifiedMediationParams mediationParams) {
        super(mediationParams);
        this.creativeAdm = mediationParams.getString(IabUtils.KEY_CREATIVE_ADM);
        this.width = mediationParams.getInt(IabUtils.KEY_WIDTH);
        this.height = mediationParams.getInt(IabUtils.KEY_HEIGHT);
        this.canPreload = mediationParams.getBool(IabUtils.KEY_PRELOAD);
    }

    @Override
    public boolean isValid(@NonNull UnifiedAdCallback callback) {
        if (TextUtils.isEmpty(creativeAdm) || width == 0 || height == 0) {
            callback.onAdLoadFailed(BMError.IncorrectAdUnit);
            return false;
        }
        return true;
    }

}
