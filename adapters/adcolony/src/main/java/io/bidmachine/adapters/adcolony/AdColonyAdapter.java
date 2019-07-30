package io.bidmachine.adapters.adcolony;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import com.adcolony.sdk.*;
import io.bidmachine.*;
import io.bidmachine.models.DataRestrictions;
import io.bidmachine.models.TargetingInfo;
import io.bidmachine.unified.UnifiedAdRequestParams;
import io.bidmachine.unified.UnifiedFullscreenAd;
import io.bidmachine.utils.BMError;
import io.bidmachine.utils.Gender;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

class AdColonyAdapter extends NetworkAdapter implements HeaderBiddingAdapter {

    private static HashSet<String> zonesCache = new HashSet<>();

    AdColonyAdapter() {
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
    public void collectHeaderBiddingParams(@NonNull ContextProvider contextProvider,
                                           @NonNull UnifiedAdRequestParams requestParams,
                                           @NonNull final HeaderBiddingCollectParamsCallback callback,
                                           @NonNull Map<String, String> mediationConfig) {
        String appId = mediationConfig.get(AdColonyConfig.KEY_APP_ID);
        if (TextUtils.isEmpty(appId)) {
            callback.onCollectFail(BMError.requestError("App id not provided"));
            return;
        }
        assert appId != null;
        String zoneId = mediationConfig.get(AdColonyConfig.KEY_ZONE_ID);
        if (TextUtils.isEmpty(zoneId)) {
            callback.onCollectFail(BMError.requestError("Zone id not provided"));
            return;
        }
        assert zoneId != null;
        String storeId = mediationConfig.get(AdColonyConfig.KEY_STORE_ID);
        if (TextUtils.isEmpty(storeId)) {
            callback.onCollectFail(BMError.requestError("Store id not provided"));
            return;
        }
        assert storeId != null;
        if (zonesCache == null) {
            zonesCache = new HashSet<>();
        }
        zonesCache.add(zoneId);
        AdColony.configure(
                (Application) contextProvider.getContext().getApplicationContext(),
                createAppOptions(contextProvider.getContext(), requestParams, storeId),
                appId,
                zonesCache.toArray(new String[0]));

        final Map<String, String> params = new HashMap<>();
        params.put(AdColonyConfig.KEY_APP_ID, appId);
        params.put(AdColonyConfig.KEY_ZONE_ID, zoneId);

        AdColonyZone zone = AdColony.getZone(zoneId);
        if (zone != null && zone.isValid()) {
            callback.onCollectFinished(params);
        } else {
            AdColony.requestInterstitial(zoneId, new AdColonyInterstitialListener() {
                @Override
                public void onRequestFilled(AdColonyInterstitial adColonyInterstitial) {
                    callback.onCollectFinished(params);
                }

                @Override
                public void onRequestNotFilled(AdColonyZone zone) {
                    callback.onCollectFail(BMError.NoContent);
                }
            }, createAdOptions(requestParams));
        }
    }

    private static AdColonyAppOptions createAppOptions(@NonNull Context context,
                                                       @NonNull UnifiedAdRequestParams adRequestParams,
                                                       @NonNull String storeId) {
        DataRestrictions dataRestrictions = adRequestParams.getDataRestrictions();
        TargetingInfo targetingInfo = adRequestParams.getTargetingParams();
        AdColonyAppOptions options = AdColony.getAppOptions();
        if (options == null) {
            options = new AdColonyAppOptions();
            AdColony.setAppOptions(options);
        }
        String userId = targetingInfo.getUserId();
        if (userId != null) {
            options.setUserID(userId);
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
        options.setTestModeEnabled(adRequestParams.isTestMode());
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
