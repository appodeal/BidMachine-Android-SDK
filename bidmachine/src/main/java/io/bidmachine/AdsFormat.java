package io.bidmachine;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

public enum AdsFormat {
    Banner("banner", new AdsFormatMatcher() {
        @Override
        public boolean isMatch(AdsType adsType, AdContentType adContentType) {
            return adsType == AdsType.Banner;
        }
    }),
    Interstitial("interstitial", new AdsFormatMatcher() {
        @Override
        public boolean isMatch(AdsType adsType, AdContentType adContentType) {
            return adsType == AdsType.Interstitial;
        }
    }),
    InterstitialVideo("interstitial_video", new AdsFormatMatcher() {
        @Override
        public boolean isMatch(AdsType adsType, AdContentType adContentType) {
            return adsType == AdsType.Interstitial && adContentType == AdContentType.Video;
        }
    }),
    InterstitialStatic("interstitial_static", new AdsFormatMatcher() {
        @Override
        public boolean isMatch(AdsType adsType, AdContentType adContentType) {
            return adsType == AdsType.Interstitial && adContentType == AdContentType.Static;
        }
    }),
    Rewarded("rewarded", new AdsFormatMatcher() {
        @Override
        public boolean isMatch(AdsType adsType, AdContentType adContentType) {
            return adsType == AdsType.Rewarded;
        }
    }),
    RewardedVideo("rewarded_video", new AdsFormatMatcher() {
        @Override
        public boolean isMatch(AdsType adsType, AdContentType adContentType) {
            return adsType == AdsType.Rewarded && adContentType == AdContentType.Video;
        }
    }),
    RewardedStatic("rewarded_static", new AdsFormatMatcher() {
        @Override
        public boolean isMatch(AdsType adsType, AdContentType adContentType) {
            return adsType == AdsType.Rewarded && adContentType == AdContentType.Static;
        }
    }),
    Native("native", new AdsFormatMatcher() {
        @Override
        public boolean isMatch(AdsType adsType, AdContentType adContentType) {
            return adsType == AdsType.Native;
        }
    });

    @NonNull
    private String remoteName;
    @NonNull
    private AdsFormatMatcher matcher;

    AdsFormat(@NonNull String remoteName, @NonNull AdsFormatMatcher matcher) {
        this.remoteName = remoteName;
        this.matcher = matcher;
    }

    boolean isMatch(AdsType adsType, AdContentType adContentType) {
        return matcher.isMatch(adsType, adContentType);
    }

    static AdsFormat byRemoteName(@Nullable String name) {
        if (!TextUtils.isEmpty(name)) {
            for (AdsFormat type : values()) {
                if (type.remoteName.equals(name)) {
                    return type;
                }
            }
        }
        return null;
    }

    interface AdsFormatMatcher {
        boolean isMatch(AdsType adsType, AdContentType adContentType);
    }
}
