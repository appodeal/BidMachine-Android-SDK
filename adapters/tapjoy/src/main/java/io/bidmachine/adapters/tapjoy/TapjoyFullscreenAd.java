package io.bidmachine.adapters.tapjoy;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.tapjoy.TJPlacement;
import com.tapjoy.Tapjoy;
import io.bidmachine.unified.UnifiedFullscreenAd;
import io.bidmachine.unified.UnifiedFullscreenAdCallback;
import io.bidmachine.unified.UnifiedFullscreenAdRequestParams;
import io.bidmachine.unified.UnifiedMediationParams;
import io.bidmachine.utils.BMError;

import java.util.Map;

public class TapjoyFullscreenAd implements UnifiedFullscreenAd {

    @Nullable
    private TJPlacement tjPlacement;

    @Override
    public void load(@NonNull Context context,
                     @NonNull UnifiedFullscreenAdCallback callback,
                     @NonNull UnifiedFullscreenAdRequestParams requestParams,
                     @NonNull UnifiedMediationParams mediationParams,
                     @Nullable Map<String, Object> localExtra) {
        String placement = mediationParams.getString("placement");
        TapjoyFullscreenAdListener listener = new TapjoyFullscreenAdListener(callback);
        tjPlacement = Tapjoy.getLimitedPlacement(placement, listener);
        tjPlacement.setVideoListener(listener);
//        tjPlacement.setAuctionData();
        tjPlacement.requestContent();
    }

    @Override
    public void show(@NonNull Context context, @NonNull UnifiedFullscreenAdCallback callback) {
        if (tjPlacement != null && tjPlacement.isContentReady()) {
            Tapjoy.setActivity((Activity) context);
            tjPlacement.showContent();
        } else {
            callback.onAdShowFailed(BMError.NotLoaded);
        }
    }

    @Override
    public void onDestroy() {
        if (tjPlacement != null) {
            tjPlacement.setVideoListener(null);
            tjPlacement = null;
        }
    }
}
