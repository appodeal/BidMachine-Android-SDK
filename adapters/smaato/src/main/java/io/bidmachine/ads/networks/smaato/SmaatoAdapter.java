package io.bidmachine.ads.networks.smaato;

import android.app.Application;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.smaato.sdk.core.LatLng;
import com.smaato.sdk.core.SmaatoSdk;
import com.smaato.sdk.ub.UBBannerSize;
import com.smaato.sdk.ub.UBBid;
import com.smaato.sdk.ub.UBBidRequestError;
import com.smaato.sdk.ub.UBError;
import com.smaato.sdk.ub.UnifiedBidding;

import java.util.HashMap;
import java.util.Map;

import io.bidmachine.AdsType;
import io.bidmachine.BuildConfig;
import io.bidmachine.ContextProvider;
import io.bidmachine.HeaderBiddingAdRequestParams;
import io.bidmachine.HeaderBiddingAdapter;
import io.bidmachine.HeaderBiddingCollectParamsCallback;
import io.bidmachine.NetworkAdapter;
import io.bidmachine.NetworkConfigParams;
import io.bidmachine.banner.BannerSize;
import io.bidmachine.models.TargetingInfo;
import io.bidmachine.unified.UnifiedAdRequestParams;
import io.bidmachine.unified.UnifiedBannerAd;
import io.bidmachine.unified.UnifiedBannerAdRequestParams;
import io.bidmachine.unified.UnifiedFullscreenAd;
import io.bidmachine.utils.BMError;
import io.bidmachine.utils.Gender;

class SmaatoAdapter extends NetworkAdapter implements HeaderBiddingAdapter {

    SmaatoAdapter() {
        super("smaato",
              SmaatoSdk.getVersion(),
              BuildConfig.VERSION_NAME,
              new AdsType[]{AdsType.Banner, AdsType.Interstitial, AdsType.Rewarded});
    }

    @Override
    public UnifiedBannerAd createBanner() {
        return new SmaatoBanner();
    }

    @Override
    public UnifiedFullscreenAd createInterstitial() {
        return new SmaatoInterstitial();
    }

    @Override
    public UnifiedFullscreenAd createRewarded() {
        return new SmaatoRewarded();
    }

    @Override
    protected void onInitialize(@NonNull ContextProvider contextProvider,
                                @NonNull UnifiedAdRequestParams adRequestParams,
                                @NonNull NetworkConfigParams networkConfig) {
        Map<String, String> mediationConfig = networkConfig.obtainNetworkParams();
        if (mediationConfig == null) {
            return;
        }
        String publisherId = mediationConfig.get(SmaatoConfig.KEY_PUBLISHER_ID);
        if (TextUtils.isEmpty(publisherId)) {
            return;
        }
        assert publisherId != null;
        Application application = (Application) contextProvider.getContext()
                .getApplicationContext();
        SmaatoSdk.init(application, publisherId);
    }

    @Override
    public void collectHeaderBiddingParams(@NonNull ContextProvider contextProvider,
                                           @NonNull UnifiedAdRequestParams adRequestParams,
                                           @NonNull HeaderBiddingAdRequestParams hbAdRequestParams,
                                           @NonNull final HeaderBiddingCollectParamsCallback collectCallback,
                                           @NonNull Map<String, String> mediationConfig) {
        final String publisherId = mediationConfig.get(SmaatoConfig.KEY_PUBLISHER_ID);
        if (TextUtils.isEmpty(publisherId)) {
            collectCallback.onCollectFail(BMError.requestError("publisher_id not provided"));
            return;
        }
        assert publisherId != null;
        final String adSpaceId = mediationConfig.get(SmaatoConfig.KEY_AD_SPACE_ID);
        if (TextUtils.isEmpty(adSpaceId)) {
            collectCallback.onCollectFail(BMError.requestError("ad_space_id not provided"));
            return;
        }
        assert adSpaceId != null;
        updateTargeting(adRequestParams);
        final AdsType adsType = hbAdRequestParams.getAdsType();
        switch (adsType) {
            case Banner:
                BannerSize bannerSize = ((UnifiedBannerAdRequestParams) adRequestParams).getBannerSize();
                UnifiedBidding.prebidBanner(adSpaceId,
                                            mapBannerSize(bannerSize),
                                            new LoadListener(publisherId,
                                                             adSpaceId,
                                                             collectCallback));
                break;
            case Interstitial:
                UnifiedBidding.prebidInterstitial(adSpaceId,
                                                  new LoadListener(publisherId,
                                                                   adSpaceId,
                                                                   collectCallback));
                break;
            case Rewarded:
                UnifiedBidding.prebidRewardedInterstitial(adSpaceId,
                                                          new LoadListener(publisherId,
                                                                           adSpaceId,
                                                                           collectCallback));
                break;
            default:
                collectCallback.onCollectFail(BMError.IncorrectAdUnit);
        }
    }

    private void updateTargeting(@NonNull UnifiedAdRequestParams adRequestParams) {
        SmaatoSdk.setCoppa(adRequestParams.getDataRestrictions().isUserAgeRestricted());
        TargetingInfo targetingInfo = adRequestParams.getTargetingParams();
        Gender gender = targetingInfo.getGender();
        if (gender != null) {
            switch (gender) {
                case Male:
                    SmaatoSdk.setGender(com.smaato.sdk.core.Gender.MALE);
                    break;
                case Female:
                    SmaatoSdk.setGender(com.smaato.sdk.core.Gender.FEMALE);
                    break;
                default:
                    SmaatoSdk.setGender(com.smaato.sdk.core.Gender.OTHER);
            }
        }
        Integer age = targetingInfo.getUserAge();
        if (age != null) {
            SmaatoSdk.setAge(age);
        }
        Location location = targetingInfo.getDeviceLocation();
        if (location != null) {
            SmaatoSdk.setLatLng(new LatLng(location.getLatitude(), location.getLongitude()));
        }
        String zip = targetingInfo.getZip();
        if (zip != null) {
            SmaatoSdk.setZip(zip);
        }
    }

    private static final class LoadListener implements UnifiedBidding.PrebidListener {

        private final HeaderBiddingCollectParamsCallback collectCallback;
        private final String publisherId;
        private final String adSpaceId;

        LoadListener(String publisherId,
                     String adSpaceId,
                     HeaderBiddingCollectParamsCallback collectCallback) {
            this.publisherId = publisherId;
            this.adSpaceId = adSpaceId;
            this.collectCallback = collectCallback;
        }

        @Override
        public void onPrebidResult(UBBid ubBid, UBBidRequestError ubBidRequestError) {
            if (ubBid == null) {
                BMError bmError = ubBidRequestError != null
                        ? mapError(ubBidRequestError.error)
                        : BMError.IncorrectAdUnit;
                collectCallback.onCollectFail(bmError);
                return;
            }

            Map<String, String> params = new HashMap<>();
            params.put(SmaatoConfig.KEY_PUBLISHER_ID, publisherId);
            params.put(SmaatoConfig.KEY_AD_SPACE_ID, adSpaceId);
            params.put(SmaatoConfig.KEY_BID_PRICE, String.valueOf(ubBid.bidPrice));
            collectCallback.onCollectFinished(params);
        }

    }

    private static UBBannerSize mapBannerSize(@Nullable BannerSize bannerSize) {
        if (bannerSize != null) {
            switch (bannerSize) {
                case Size_300x250:
                    return UBBannerSize.MEDIUM_RECTANGLE_300x250;
                case Size_728x90:
                    return UBBannerSize.LEADERBOARD_728x90;
            }
        }
        return UBBannerSize.XX_LARGE_320x50;
    }

    private static BMError mapError(@Nullable UBError ubError) {
        if (ubError != null) {
            switch (ubError) {
                case INVALID_REQUEST:
                    return BMError.IncorrectAdUnit;
                case NETWORK_ERROR:
                    return BMError.Connection;
                case NOT_INITIALISED:
                    return BMError.NotInitialized;
                case TOO_MANY_REQUESTS:
                    return BMError.Server;
                case MISSING_DEPENDENCY:
                case INTERNAL_ERROR:
                    return BMError.Internal;
            }
        }
        return BMError.IncorrectAdUnit;
    }

}
