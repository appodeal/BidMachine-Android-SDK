package io.bidmachine.ads.networks;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.amazon.device.ads.AdError;
import com.amazon.device.ads.AdRegistration;
import com.amazon.device.ads.DTBAdCallback;
import com.amazon.device.ads.DTBAdRequest;
import com.amazon.device.ads.DTBAdResponse;
import com.amazon.device.ads.DTBAdSize;
import com.amazon.device.ads.MRAIDPolicy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.bidmachine.AdsType;
import io.bidmachine.ContextProvider;
import io.bidmachine.HeaderBiddingAdapter;
import io.bidmachine.HeaderBiddingCollectParamsCallback;
import io.bidmachine.NetworkAdapter;
import io.bidmachine.NetworkConfigParams;
import io.bidmachine.ads.networks.amazon.BuildConfig;
import io.bidmachine.banner.BannerSize;
import io.bidmachine.unified.UnifiedAdRequestParams;
import io.bidmachine.unified.UnifiedBannerAdRequestParams;
import io.bidmachine.unified.UnifiedFullscreenAdRequestParams;
import io.bidmachine.utils.BMError;

class AmazonAdapter extends NetworkAdapter implements HeaderBiddingAdapter {

    AmazonAdapter() {
        super("amazon",
              AdRegistration.getVersion(),
              BuildConfig.VERSION_NAME,
              new AdsType[]{AdsType.Banner, AdsType.Interstitial});
    }

    @Override
    public void setLogging(boolean enabled) {
        super.setLogging(enabled);
        AdRegistration.enableLogging(enabled);
    }

    @Override
    protected void onInitialize(@NonNull ContextProvider contextProvider,
                                @NonNull UnifiedAdRequestParams adRequestParams,
                                @NonNull NetworkConfigParams networkConfigParams) {
        super.onInitialize(contextProvider, adRequestParams, networkConfigParams);
        initialize(contextProvider, adRequestParams, networkConfigParams.obtainNetworkParams());
        AdRegistration.setMRAIDSupportedVersions(new String[]{"1.0", "2.0"});
        AdRegistration.setMRAIDPolicy(MRAIDPolicy.CUSTOM);
    }

    @Override
    public void collectHeaderBiddingParams(@NonNull ContextProvider contextProvider,
                                           @NonNull UnifiedAdRequestParams requestParams,
                                           @NonNull final HeaderBiddingCollectParamsCallback callback,
                                           @NonNull Map<String, String> mediationConfig) {
        if (!initialize(contextProvider, requestParams, mediationConfig)) {
            callback.onCollectFail(BMError.paramError(AmazonConfig.APP_KEY + " not provided"));
            return;
        }
        final String slotUuid = mediationConfig.get(AmazonConfig.SLOT_UUID);
        if (TextUtils.isEmpty(slotUuid)) {
            callback.onCollectFail(BMError.paramError(AmazonConfig.SLOT_UUID + " not provided"));
            return;
        }
        final DTBAdRequest loader = new DTBAdRequest();
        if (requestParams instanceof UnifiedBannerAdRequestParams) {
            BannerSize bannerSize = ((UnifiedBannerAdRequestParams) requestParams).getBannerSize();
            loader.setSizes(new DTBAdSize(bannerSize.width, bannerSize.height, slotUuid));
        } else if (requestParams instanceof UnifiedFullscreenAdRequestParams) {
            loader.setSizes(new DTBAdSize.DTBInterstitialAdSize(slotUuid));
        } else {
            callback.onCollectFail(BMError.IncorrectAdUnit);
            return;
        }
        loader.loadAd(new DTBAdCallback() {
            @Override
            public void onFailure(@NonNull AdError adError) {
                callback.onCollectFail(mapError(adError));
            }

            @Override
            public void onSuccess(@NonNull DTBAdResponse dtbAdResponse) {
                Map<String, String> resultMap = new HashMap<>();
                Map<String, List<String>> params = dtbAdResponse.getDefaultDisplayAdsRequestCustomParams();
                for (Map.Entry<String, List<String>> entry : params.entrySet()) {
                    List<String> values = entry.getValue();
                    if (values != null) {
                        String value = values.get(0);
                        if (value != null) {
                            resultMap.put(entry.getKey(), value);
                        }
                    }
                }
                if (resultMap.isEmpty()) {
                    callback.onCollectFail(BMError.paramError(
                            "Amazon: Response was successful but params not provided"));
                } else {
                    callback.onCollectFinished(resultMap);
                }
            }
        });
    }

    private boolean initialize(@NonNull ContextProvider contextProvider,
                               @NonNull UnifiedAdRequestParams requestParams,
                               @Nullable Map<String, String> params) {
        String appKey = params != null ? params.get(AmazonConfig.APP_KEY) : null;
        if (TextUtils.isEmpty(appKey)) {
            return false;
        }
        assert appKey != null;
        AdRegistration.getInstance(appKey, contextProvider.getContext().getApplicationContext());
        AdRegistration.enableTesting(requestParams.isTestMode());
        return true;
    }

    private static BMError mapError(@NonNull AdError error) {
        switch (error.getCode()) {
            case NO_FILL:
                return BMError.NoContent;
            case NETWORK_ERROR:
                return BMError.Connection;
            case REQUEST_ERROR:
                return BMError.requestError(error.getMessage());
            case NETWORK_TIMEOUT:
                return BMError.TimeoutError;
            default:
                return BMError.Internal;
        }
    }
}
