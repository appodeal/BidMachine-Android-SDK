package io.bidmachine.models;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import io.bidmachine.ContextProvider;

import java.util.Map;

public interface AdObject<AdObjectParamsType extends AdObjectParams> {

    AdObjectParamsType getParams();

    void load(@NonNull ContextProvider contextProvider, @Nullable Map<String, Object> extra);

    void onShown();

    void onImpression();

    void onClicked();

    void onFinished();

    void onClosed();

    void onDestroy();
}