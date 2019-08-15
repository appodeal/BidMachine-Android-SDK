package io.bidmachine;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import io.bidmachine.utils.BMError;

import java.util.Map;

public interface HeaderBiddingCollectParamsCallback {

    void onCollectFinished(@NonNull Map<String, String> params);

    void onCollectFail(@Nullable BMError error);

}
