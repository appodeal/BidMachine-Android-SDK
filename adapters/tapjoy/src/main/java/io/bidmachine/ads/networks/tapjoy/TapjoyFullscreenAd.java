package io.bidmachine.ads.networks.tapjoy;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.tapjoy.TJPlacement;
import com.tapjoy.Tapjoy;
import com.tapjoy.TapjoyAuctionFlags;
import io.bidmachine.BidMachine;
import io.bidmachine.ContextProvider;
import io.bidmachine.unified.UnifiedFullscreenAd;
import io.bidmachine.unified.UnifiedFullscreenAdCallback;
import io.bidmachine.unified.UnifiedFullscreenAdRequestParams;
import io.bidmachine.unified.UnifiedMediationParams;
import io.bidmachine.utils.BMError;

import java.util.HashMap;

public class TapjoyFullscreenAd extends UnifiedFullscreenAd {

    @Nullable
    private TJPlacement tjPlacement;

    @Override
    public void load(@NonNull ContextProvider context,
                     @NonNull UnifiedFullscreenAdCallback callback,
                     @NonNull UnifiedFullscreenAdRequestParams requestParams,
                     @NonNull UnifiedMediationParams mediationParams) throws Throwable {
        //TODO: fix this behavior
        Tapjoy.setActivity((Activity) context);
        TapjoyFullscreenAdListener listener = new TapjoyFullscreenAdListener(callback);
        tjPlacement = Tapjoy.getLimitedPlacement(mediationParams.getString(TapjoyConfig.KEY_PLACEMENT_NAME), listener);
        tjPlacement.setVideoListener(listener);
        tjPlacement.setMediationName(BidMachine.NAME);
        tjPlacement.setAdapterVersion(BuildConfig.VERSION_NAME);

        HashMap<String, String> auctionParams = new HashMap<>();
        auctionParams.put(TapjoyAuctionFlags.AUCTION_ID, mediationParams.getString(TapjoyAuctionFlags.AUCTION_ID));
        auctionParams.put(TapjoyAuctionFlags.AUCTION_DATA, mediationParams.getString(TapjoyAuctionFlags.AUCTION_DATA));
        tjPlacement.setAuctionData(auctionParams);

        tjPlacement.requestContent();
    }

    @Override
    public void show(@NonNull Context context, @NonNull UnifiedFullscreenAdCallback callback) {
        if (tjPlacement != null && tjPlacement.isContentReady()) {
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
