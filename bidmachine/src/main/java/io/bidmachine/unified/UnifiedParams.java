package io.bidmachine.unified;

import android.support.annotation.NonNull;

public abstract class UnifiedParams {

    @NonNull
    private UnifiedMediationParams mediationParams;

    public UnifiedParams(@NonNull UnifiedMediationParams mediationParams) {
        this.mediationParams = mediationParams;
    }

    @NonNull
    public UnifiedMediationParams getMediationParams() {
        return mediationParams;
    }

    public abstract boolean isValid(@NonNull UnifiedAdCallback callback);

}
