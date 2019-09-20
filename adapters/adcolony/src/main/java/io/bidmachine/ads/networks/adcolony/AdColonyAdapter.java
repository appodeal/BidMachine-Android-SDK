package io.bidmachine.ads.networks.adcolony;

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

import java.lang.reflect.Field;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

class AdColonyAdapter extends NetworkAdapter implements HeaderBiddingAdapter {

    private static HashSet<String> zonesCache = new HashSet<>();
    private boolean isAdapterInitialized = false;

    AdColonyAdapter() {
        super("adcolony",
              obtainAdColonyVersion(),
              BuildConfig.VERSION_NAME,
              new AdsType[]{AdsType.Interstitial, AdsType.Rewarded});
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
    protected void onInitialize(@NonNull ContextProvider contextProvider,
                                @NonNull UnifiedAdRequestParams adRequestParams,
                                @NonNull NetworkConfigParams networkConfigParams) {
        super.onInitialize(contextProvider, adRequestParams, networkConfigParams);
        EnumMap<AdsFormat, Map<String, String>> mediationConfigs =
                networkConfigParams.obtainNetworkMediationConfigs(AdsFormat.values());
        if (mediationConfigs != null) {
            for (Map<String, String> config : mediationConfigs.values()) {
                extractZoneId(config);
            }
        }
    }

    @Override
    public void collectHeaderBiddingParams(@NonNull ContextProvider contextProvider,
                                           @NonNull UnifiedAdRequestParams adRequestParams,
                                           @NonNull HeaderBiddingAdRequestParams hbAdRequestParams,
                                           @NonNull final HeaderBiddingCollectParamsCallback collectCallback,
                                           @NonNull Map<String, String> mediationConfig) {
        String appId = mediationConfig.get(AdColonyConfig.KEY_APP_ID);
        if (TextUtils.isEmpty(appId)) {
            collectCallback.onCollectFail(BMError.requestError("App id not provided"));
            return;
        }
        assert appId != null;
        String zoneId = extractZoneId(mediationConfig);
        if (TextUtils.isEmpty(zoneId)) {
            collectCallback.onCollectFail(BMError.requestError("Zone id not provided"));
            return;
        }
        assert zoneId != null;
        String storeId = mediationConfig.get(AdColonyConfig.KEY_STORE_ID);
        if (TextUtils.isEmpty(storeId)) {
            collectCallback.onCollectFail(BMError.requestError("Store id not provided"));
            return;
        }
        assert storeId != null;
        synchronized (AdColonyAdapter.class) {
            if (!isAdapterInitialized) {
                AdColony.configure(
                        (Application) contextProvider.getContext().getApplicationContext(),
                        createAppOptions(contextProvider.getContext(), adRequestParams, storeId),
                        appId,
                        zonesCache.toArray(new String[0]));
                if (!isAdColonyConfigured()) {
                    collectCallback.onCollectFail(BMError.TimeoutError);
                    return;
                }
                isAdapterInitialized = true;
            }
        }

        final Map<String, String> params = new HashMap<>();
        params.put(AdColonyConfig.KEY_APP_ID, appId);
        params.put(AdColonyConfig.KEY_ZONE_ID, zoneId);

        AdColonyZone zone = AdColony.getZone(zoneId);
        if (zone != null && zone.isValid()) {
            collectCallback.onCollectFinished(params);
        } else {
            AdColony.requestInterstitial(zoneId, new AdColonyInterstitialListener() {
                @Override
                public void onRequestFilled(AdColonyInterstitial adColonyInterstitial) {
                    collectCallback.onCollectFinished(params);
                }

                @Override
                public void onRequestNotFilled(AdColonyZone zone) {
                    collectCallback.onCollectFail(BMError.NoContent);
                }
            }, createAdOptions(adRequestParams));
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

    private static String obtainAdColonyVersion() {
        String version = AdColony.getSDKVersion();
        if (TextUtils.isEmpty(version)) {
            try {
                Class<?> versionClass = Class.forName("com.adcolony.sdk.j");
                Field buildVersionField = versionClass.getDeclaredField("a");
                buildVersionField.setAccessible(true);
                version = (String) buildVersionField.get(versionClass);
            } catch (Exception ignore) {
            }
        }
        return version;
    }

    private String extractZoneId(Map<String, String> mediationConfig) {
        String zoneId = mediationConfig.get(AdColonyConfig.KEY_ZONE_ID);
        if (TextUtils.isEmpty(zoneId)) {
            return null;
        }
        assert zoneId != null;
        if (zonesCache == null) {
            zonesCache = new HashSet<>();
        }
        if (zonesCache.add(zoneId)) {
            isAdapterInitialized = false;
        }
        return zoneId;
    }

    private boolean isAdColonyConfigured() {
        return !TextUtils.isEmpty(AdColony.getSDKVersion());
    }

}
