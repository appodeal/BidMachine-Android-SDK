package io.bidmachine.adapters.my_target;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import com.my.target.common.CustomParams;
import com.my.target.common.MyTargetPrivacy;
import com.my.target.common.MyTargetVersion;
import io.bidmachine.*;
import io.bidmachine.models.DataRestrictions;
import io.bidmachine.models.TargetingInfo;
import io.bidmachine.unified.UnifiedAdRequestParams;
import io.bidmachine.unified.UnifiedBannerAd;
import io.bidmachine.unified.UnifiedFullscreenAd;
import io.bidmachine.utils.BMError;
import io.bidmachine.utils.Gender;

import java.util.Collections;
import java.util.Map;

class MyTargetAdapter extends NetworkAdapter implements HeaderBiddingAdapter {

    MyTargetAdapter() {
        super("my_target",
                MyTargetVersion.VERSION,
                new AdsType[]{AdsType.Banner, AdsType.Interstitial, AdsType.Rewarded});
    }

    @Override
    public UnifiedBannerAd createBanner() {
        return new MyTargetBanner();
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
    protected void onInitialize(@NonNull ContextProvider contextProvider,
                                @NonNull UnifiedAdRequestParams adRequestParams,
                                @NonNull NetworkConfig networkConfig) {
        updateRestrictions(adRequestParams);
    }

    @Override
    public void collectHeaderBiddingParams(@NonNull ContextProvider contextProvider,
                                           @NonNull UnifiedAdRequestParams requestParams,
                                           @NonNull HeaderBiddingCollectParamsCallback callback,
                                           @NonNull Map<String, String> mediationConfig) {
        String slotId = mediationConfig.get(MyTargetConfig.KEY_SLOT_ID);
        if (TextUtils.isEmpty(slotId)) {
            callback.onCollectFail(BMError.requestError("slot_id not provided"));
            return;
        }
        updateRestrictions(requestParams);
        Map<String, String> params = Collections.singletonMap(MyTargetConfig.KEY_SLOT_ID, slotId);
        callback.onCollectFinished(params);
    }

    private void updateRestrictions(@NonNull UnifiedAdRequestParams adRequestParams) {
        DataRestrictions dataRestrictions = adRequestParams.getDataRestrictions();
        if (dataRestrictions.isUserInGdprScope()) {
            MyTargetPrivacy.setUserConsent(dataRestrictions.isUserHasConsent());
        }
        MyTargetPrivacy.setUserAgeRestricted(dataRestrictions.isUserAgeRestricted());
    }

    static void updateTargeting(@NonNull UnifiedAdRequestParams adRequestParams, @NonNull CustomParams customParams) {
        TargetingInfo targetingInfo = adRequestParams.getTargetingParams();
        Integer age = targetingInfo.getUserAge();
        if (age != null) {
            customParams.setAge(age);
        }
        Gender gender = targetingInfo.getGender();
        if (gender != null) {
            customParams.setGender(transformGender(gender));
        }
    }

    private static int transformGender(@NonNull Gender gender) {
        switch (gender) {
            case Female:
                return CustomParams.Gender.FEMALE;
            case Male:
                return CustomParams.Gender.MALE;
            default:
                return CustomParams.Gender.UNKNOWN;
        }
    }

}
