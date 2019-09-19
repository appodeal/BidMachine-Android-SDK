package io.bidmachine;

import android.support.annotation.NonNull;

import java.util.Map;

import io.bidmachine.unified.UnifiedAdRequestParams;

public interface HeaderBiddingAdapter {

    String getKey();

    String getVersion();

    void collectHeaderBiddingParams(@NonNull ContextProvider contextProvider,
                                    @NonNull UnifiedAdRequestParams adRequestParams,
                                    @NonNull HeaderBiddingAdRequestParams hbAdRequestParams,
                                    @NonNull HeaderBiddingCollectParamsCallback collectCallback,
                                    @NonNull Map<String, String> mediationConfig);

}
