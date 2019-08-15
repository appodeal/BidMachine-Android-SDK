package io.bidmachine;

import android.support.annotation.NonNull;
import io.bidmachine.unified.UnifiedAdRequestParams;

import java.util.Map;

public interface HeaderBiddingAdapter {

    String getKey();

    String getVersion();

    void collectHeaderBiddingParams(@NonNull ContextProvider contextProvider,
                                    @NonNull UnifiedAdRequestParams requestParams,
                                    @NonNull HeaderBiddingCollectParamsCallback callback,
                                    @NonNull Map<String, String> mediationConfig);

}
