package io.bidmachine;

import android.support.annotation.NonNull;

public enum AdsFormat {
    Banner(new AdsFormatMatcher() {
        @Override
        public boolean isMatch(AdsType adsType, AdContentType adContentType) {
            return adsType == AdsType.Banner;
        }
    }),
    Interstitial(new AdsFormatMatcher() {
        @Override
        public boolean isMatch(AdsType adsType, AdContentType adContentType) {
            return adsType == AdsType.Interstitial;
        }
    }),
    InterstitialVideo(new AdsFormatMatcher() {
        @Override
        public boolean isMatch(AdsType adsType, AdContentType adContentType) {
            return adsType == AdsType.Interstitial && adContentType == AdContentType.Video;
        }
    }),
    InterstitialStatic(new AdsFormatMatcher() {
        @Override
        public boolean isMatch(AdsType adsType, AdContentType adContentType) {
            return adsType == AdsType.Interstitial && adContentType == AdContentType.Static;
        }
    }),
    Rewarded(new AdsFormatMatcher() {
        @Override
        public boolean isMatch(AdsType adsType, AdContentType adContentType) {
            return adsType == AdsType.Rewarded;
        }
    }),
    RewardedVideo(new AdsFormatMatcher() {
        @Override
        public boolean isMatch(AdsType adsType, AdContentType adContentType) {
            return adsType == AdsType.Rewarded && adContentType == AdContentType.Video;
        }
    }),
    RewardedStatic(new AdsFormatMatcher() {
        @Override
        public boolean isMatch(AdsType adsType, AdContentType adContentType) {
            return adsType == AdsType.Rewarded && adContentType == AdContentType.Static;
        }
    }),
    Native(new AdsFormatMatcher() {
        @Override
        public boolean isMatch(AdsType adsType, AdContentType adContentType) {
            return adsType == AdsType.Native;
        }
    });

    @NonNull
    private AdsFormatMatcher matcher;

    AdsFormat(@NonNull AdsFormatMatcher matcher) {
        this.matcher = matcher;
    }

    boolean isMatch(AdsType adsType, AdContentType adContentType) {
        return matcher.isMatch(adsType, adContentType);
    }

    interface AdsFormatMatcher {
        boolean isMatch(AdsType adsType, AdContentType adContentType);
    }
}
