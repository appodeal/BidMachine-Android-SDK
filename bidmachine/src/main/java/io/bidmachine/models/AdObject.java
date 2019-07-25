package io.bidmachine.models;

import android.support.annotation.NonNull;
import io.bidmachine.ContextProvider;
import io.bidmachine.unified.UnifiedAdRequestParams;

public interface AdObject<
        AdObjectParamsType extends AdObjectParams,
        UnifiedAdRequestParamsType extends UnifiedAdRequestParams> {

    AdObjectParamsType getParams();

    void load(@NonNull ContextProvider contextProvider,
              @NonNull UnifiedAdRequestParamsType adRequestParams);

    void onShown();

    void onShowFailed();

    void onImpression();

    void onClicked();

    void onFinished();

    void onClosed();

    void onExpired();

    void onDestroy();
}