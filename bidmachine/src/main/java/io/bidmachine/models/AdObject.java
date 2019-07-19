package io.bidmachine.models;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Map;

public interface AdObject<AdObjectParamsType extends AdObjectParams> {

    AdObjectParamsType getParams();

    void load(@NonNull Context context, @Nullable Map<String, Object> extra);

    void onShown();

    void onImpression();

    void onClicked();

    void onFinished();

    void onClosed();

    void onDestroy();
}