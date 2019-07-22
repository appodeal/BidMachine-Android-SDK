package io.bidmachine.adapters.adcolony;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import com.adcolony.sdk.AdColony;
import com.adcolony.sdk.AdColonyAdOptions;
import com.adcolony.sdk.AdColonyAppOptions;
import com.adcolony.sdk.AdColonyUserMetadata;
import io.bidmachine.AdsType;
import io.bidmachine.BidMachineAdapter;
import io.bidmachine.HeaderBiddingAdapter;
import io.bidmachine.HeaderBiddingCollectParamsCallback;
import io.bidmachine.models.DataRestrictions;
import io.bidmachine.models.TargetingInfo;
import io.bidmachine.unified.UnifiedAdRequestParams;
import io.bidmachine.unified.UnifiedFullscreenAd;
import io.bidmachine.utils.BMError;
import io.bidmachine.utils.Gender;

import java.util.HashMap;
import java.util.Map;

public class AdColonyAdapter extends BidMachineAdapter implements HeaderBiddingAdapter {

    public AdColonyAdapter() {
        super("adcolony", AdColony.getSDKVersion(), new AdsType[]{AdsType.Interstitial, AdsType.Rewarded});
    }

    @Override
    public UnifiedFullscreenAd createInterstitial() {
        return new AdColonyFullscreenAd(false);
    }

    @Override
    public UnifiedFullscreenAd createRewarded() {
        return new AdColonyFullscreenAd(true);
    }

    @Override
    public void collectHeaderBiddingParams(@NonNull Context context,
                                           @NonNull UnifiedAdRequestParams requestParams,
                                           @NonNull HeaderBiddingCollectParamsCallback callback,
                                           @NonNull Map<String, Object> config) {
        String appId = (String) config.get("app_id");
        if (TextUtils.isEmpty(appId)) {
            callback.onCollectFail(BMError.requestError("App id not provided"));
            return;
        }
        assert appId != null;
        String zoneId = (String) config.get("zone_id");
        if (TextUtils.isEmpty(zoneId)) {
            callback.onCollectFail(BMError.requestError("Zone id not provided"));
            return;
        }
        assert zoneId != null;
        String storeId = (String) config.get("store_id");
        if (TextUtils.isEmpty(storeId)) {
            callback.onCollectFail(BMError.requestError("Store id not provided"));
            return;
        }
        assert storeId != null;
        long start = System.currentTimeMillis();
        AdColony.configure(
                (Application) context.getApplicationContext(),
                createAppOptions(context, requestParams.getDataRestrictions(), storeId),
                zoneId);
        Log.e("AdColony", "configureTime: " + (System.currentTimeMillis() - start));
        Map<String, String> params = new HashMap<>();
        params.put("app_id", appId);
        params.put("zone_id", zoneId);
        params.put("store_id", storeId);
        callback.onCollectFinished(params);
    }

    private static AdColonyAppOptions createAppOptions(@NonNull Context context,
                                                       @NonNull DataRestrictions dataRestrictions,
                                                       @NonNull String storeId) {
        AdColonyAppOptions options = AdColony.getAppOptions();
        if (options == null) {
            options = new AdColonyAppOptions();
            AdColony.setAppOptions(options);
        }
        options.setOriginStore(storeId);
        try {
            options.setAppVersion(
                    context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (dataRestrictions.isUserInGdprScope()) {
            options.setOption("explicit_consent_given", true);
            options.setOption("consent_response", dataRestrictions.isUserHasConsent());
        }
        return options;
    }

    static AdColonyAdOptions createAdOptions(UnifiedAdRequestParams adRequestParams) {
        TargetingInfo targetingInfo = adRequestParams.getTargetingParams();
        AdColonyUserMetadata metadata = new AdColonyUserMetadata();
        Integer age = targetingInfo.getUserAge();
        if (age != null) {
            metadata.setUserAge(age);
        }
        Gender gender = targetingInfo.getGender();
        if (gender != null) {
            switch (gender) {
                case Male:
                    metadata.setUserGender(AdColonyUserMetadata.USER_MALE);
                    break;
                case Female:
                    metadata.setUserGender(AdColonyUserMetadata.USER_FEMALE);
                    break;
            }
        }
        String zip = targetingInfo.getZip();
        if (zip != null) {
            metadata.setUserZipCode(zip);
        }
        Location location = targetingInfo.getDeviceLocation();
        if (location != null) {
            metadata.setUserLocation(location);
        }
        return new AdColonyAdOptions().setUserMetadata(metadata);
    }

}
