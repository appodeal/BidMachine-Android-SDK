package io.bidmachine.ads.networks.facebook;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdListener;
import io.bidmachine.unified.UnifiedAdCallback;
import io.bidmachine.utils.BMError;

abstract class BaseFacebookListener<UnifiedAdCallbackType extends UnifiedAdCallback> implements AdListener {

    @NonNull
    private UnifiedAdCallbackType callback;

    BaseFacebookListener(@NonNull UnifiedAdCallbackType callback) {
        this.callback = callback;
    }

    @NonNull
    UnifiedAdCallbackType getCallback() {
        return callback;
    }

    @Override
    public void onError(Ad ad, AdError adError) {
        callback.onAdLoadFailed(mapError(adError));
        ad.destroy();
    }

    @Override
    public void onAdClicked(Ad ad) {
        callback.onAdClicked();
    }

    /**
     * @param error Facebook loading error code {@link com.facebook.ads.AdError}
     * @return Appodeal loading error {@link BMError} or null
     */
    @Nullable
    private static BMError mapError(@Nullable AdError error) {
        if (error == null) return null;
        switch (error.getErrorCode()) {
            case AdError.NETWORK_ERROR_CODE:
                return BMError.Connection;
            case AdError.NO_FILL_ERROR_CODE:
            case AdError.SERVER_ERROR_CODE:
            case AdError.INTERNAL_ERROR_CODE:
            case AdError.CACHE_ERROR_CODE:
            case AdError.MEDIATION_ERROR_CODE:
            case AdError.LOAD_TOO_FREQUENTLY_ERROR_CODE:
                return BMError.NoContent;
            case AdError.INTERSTITIAL_AD_TIMEOUT:
                return BMError.TimeoutError;
            case AdError.NATIVE_AD_IS_NOT_LOADED:
            case AdError.BROKEN_MEDIA_ERROR_CODE:
            case AdError.ICONVIEW_MISSING_ERROR_CODE:
            case AdError.AD_ASSETS_UNSUPPORTED_TYPE_ERROR_CODE:
                return BMError.IncorrectAdUnit;
            default:
                return BMError.Internal;
        }
    }

}
