package io.bidmachine.adapters.tapjoy;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import com.tapjoy.TJConnectListener;
import com.tapjoy.Tapjoy;
import io.bidmachine.AdsType;
import io.bidmachine.BidMachineAdapter;
import io.bidmachine.HeaderBiddingAdapter;
import io.bidmachine.HeaderBiddingCollectParamsCallback;
import io.bidmachine.unified.UnifiedAdRequestParams;
import io.bidmachine.unified.UnifiedFullscreenAd;
import io.bidmachine.utils.BMError;

import java.util.HashMap;
import java.util.Map;

public class TapjoyAdapter extends BidMachineAdapter implements HeaderBiddingAdapter {

    public TapjoyAdapter() {
        super("tapjoy", Tapjoy.getVersion(), new AdsType[]{AdsType.Interstitial, AdsType.Rewarded});
    }

    @Override
    public UnifiedFullscreenAd createInterstitial() {
        return new TapjoyFullscreenAd();
    }

    @Override
    public UnifiedFullscreenAd createRewarded() {
        return new TapjoyFullscreenAd();
    }

    @Override
    public void collectHeaderBiddingParams(@NonNull Context context,
                                           @NonNull UnifiedAdRequestParams requestParams,
                                           @NonNull final HeaderBiddingCollectParamsCallback callback,
                                           @NonNull Map<String, Object> config) {
        final String sdkKey = (String) config.get("sdk_key");
        if (TextUtils.isEmpty(sdkKey)) {
            callback.onCollectFail(BMError.requestError("sdk_key not provided"));
            return;
        }
        final String placementName = (String) config.get("placement");
        if (TextUtils.isEmpty(placementName)) {
            callback.onCollectFail(BMError.requestError("placement_name not provided"));
            return;
        }
        if (Tapjoy.isLimitedConnected()) {
            finalizeHeaderBiddingCollecting(sdkKey, placementName, Tapjoy.getUserToken(), callback);
        } else {
            Tapjoy.limitedConnect(context, sdkKey, new TJConnectListener() {
                @Override
                public void onConnectSuccess() {
                    finalizeHeaderBiddingCollecting(sdkKey, placementName, Tapjoy.getUserToken(), callback);
                }

                @Override
                public void onConnectFailure() {
                    callback.onCollectFail(BMError.IncorrectAdUnit);
                }
            });
        }
    }

    private void finalizeHeaderBiddingCollecting(@NonNull String sdkKey,
                                                 @NonNull String placementName,
                                                 @NonNull String token,
                                                 @NonNull HeaderBiddingCollectParamsCallback callback) {
        Map<String, String> params = new HashMap<>();
        params.put("sdk_key", sdkKey);
        params.put("placement_name", placementName);
        params.put("token", token);
        callback.onCollectFinished(params);
    }

}
