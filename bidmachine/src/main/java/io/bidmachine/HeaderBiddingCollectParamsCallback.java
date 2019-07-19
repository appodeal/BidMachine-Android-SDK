package io.bidmachine;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import io.bidmachine.utils.BMError;

import java.util.HashMap;

public interface HeaderBiddingCollectParamsCallback {

    void onCollectFinished(@NonNull HashMap<String, String> params);

    void onCollectFail(@Nullable BMError error);

}
