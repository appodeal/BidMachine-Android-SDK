package io.bidmachine.adapters.my_target;

import android.content.Context;
import android.support.annotation.NonNull;
import com.my.target.common.MyTargetPrivacy;
import io.bidmachine.AdsType;
import io.bidmachine.BidMachineAdapter;
import io.bidmachine.HeaderBiddingAdapter;
import io.bidmachine.HeaderBiddingCollectParamsCallback;
import io.bidmachine.models.DataRestrictions;
import io.bidmachine.unified.UnifiedAdRequestParams;
import io.bidmachine.unified.UnifiedBannerAd;
import io.bidmachine.unified.UnifiedFullscreenAd;
import io.bidmachine.utils.BMError;

import java.util.HashMap;
import java.util.Map;

public class MyTargetAdapter extends BidMachineAdapter implements HeaderBiddingAdapter {

    public MyTargetAdapter() {
        super("my_target", BuildConfig.VERSION_NAME, new AdsType[]{AdsType.Banner, AdsType.Interstitial, AdsType.Rewarded});
    }

    @Override
    public UnifiedBannerAd createBanner() {
        return new MyTargetViewAd();
    }

    @Override
    public UnifiedFullscreenAd createInterstitial() {
        return new MyTargetFullscreenAd();
    }

    @Override
    public UnifiedFullscreenAd createRewarded() {
        return new MyTargetFullscreenAd();
    }

    @Override
    public void collectHeaderBiddingParams(@NonNull Context context,
                                           @NonNull UnifiedAdRequestParams requestParams,
                                           @NonNull HeaderBiddingCollectParamsCallback callback,
                                           @NonNull Map<String, Object> config) {
        Object slotId = config.get("slot_id");
        if (!(slotId instanceof String)) {
            callback.onCollectFail(BMError.requestError("slot_id not provided"));
            return;
        }
        updateRestrictions(requestParams);
        HashMap<String, String> params = new HashMap<>();
        params.put("mailru_slot_id", (String) slotId);
        callback.onCollectFinished(params);
    }

    private void updateRestrictions(@NonNull UnifiedAdRequestParams requestParams) {
        DataRestrictions dataRestrictions = requestParams.getDataRestrictions();
        if (dataRestrictions.isUserInGdprScope()) {
            MyTargetPrivacy.setUserConsent(dataRestrictions.isUserHasConsent());
        }
        MyTargetPrivacy.setUserAgeRestricted(dataRestrictions.isUserAgeRestricted());
    }

}
