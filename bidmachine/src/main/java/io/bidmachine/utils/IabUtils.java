package io.bidmachine.utils;

import android.support.annotation.NonNull;
import io.bidmachine.nativead.NativeDataWrapper;
import io.bidmachine.nativead.utils.NativeData;
import io.bidmachine.unified.UnifiedMediationParams;

public class IabUtils {

    public static final String KEY_CLICK_URL = "clickUrl";
    public static final String KEY_TITLE = "title";
    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_ICON_URL = "iconUrl";
    public static final String KEY_IMAGE_URL = "imageUrl";
    public static final String KEY_CTA = "cta";
    public static final String KEY_RATING = "rating";
    public static final String KEY_SPONSORED = "sponsored";
    public static final String KEY_VIDEO_URL = "videoUrl";
    public static final String KEY_VIDEO_ADM = "videoAdm";

    public static final String KEY_CREATIVE_ID = "creativeId";
    public static final String KEY_CREATIVE_ADM = "creativeAdm";
    public static final String KEY_WIDTH = "width";
    public static final String KEY_HEIGHT = "height";
    public static final String KEY_PRELOAD = "preload";
    public static final String KEY_SKIP_AFTER_TIME_SEC = "skipAfterTimeSec";

    public static NativeData nativeDataFromMediationParams(@NonNull UnifiedMediationParams mediationParams) {
        return NativeDataWrapper.newBuilder()
                .setClickUrl(mediationParams.getString(KEY_CLICK_URL))
                .setTitle(mediationParams.getString(KEY_TITLE))
                .setDescription(mediationParams.getString(KEY_DESCRIPTION))
                .setIconUrl(mediationParams.getString(KEY_ICON_URL))
                .setImageUrl(mediationParams.getString(KEY_IMAGE_URL))
                .setCallToAction(mediationParams.getString(KEY_CTA))
                .setRating(mediationParams.getFloat(KEY_RATING))
                .setSponsored(mediationParams.getString(KEY_SPONSORED))
                .setVideoUrl(mediationParams.getString(KEY_VIDEO_URL))
                .setVideoAdm(mediationParams.getString(KEY_VIDEO_ADM))
                .build();
    }

}
