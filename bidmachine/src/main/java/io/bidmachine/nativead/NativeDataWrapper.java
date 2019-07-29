package io.bidmachine.nativead;

import android.support.annotation.Nullable;
import io.bidmachine.nativead.utils.NativeData;

public class NativeDataWrapper implements NativeData {

    private String iconUrl;
    private String imageUrl;
    private String clickUrl;
    private String videoUrl;
    private String videoAdm;
    private String title;
    private String description;
    private String callToAction;
    private String sponsored;
    private String ageRestriction;
    private float rating;

    private NativeDataWrapper() {
    }

    @Nullable
    @Override
    public String getIconUrl() {
        return iconUrl;
    }

    @Nullable
    @Override
    public String getImageUrl() {
        return imageUrl;
    }

    @Nullable
    @Override
    public String getClickUrl() {
        return clickUrl;
    }

    @Nullable
    @Override
    public String getVideoUrl() {
        return videoUrl;
    }

    @Nullable
    @Override
    public String getVideoAdm() {
        return videoAdm;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getCallToAction() {
        return callToAction;
    }

    @Override
    public String getSponsored() {
        return sponsored;
    }

    @Override
    public String getAgeRestrictions() {
        return ageRestriction;
    }

    @Override
    public float getRating() {
        return rating;
    }

    public static Builder newBuilder() {
        return new NativeDataWrapper().new Builder();
    }

    public class Builder {

        public Builder setIconUrl(String iconUrl) {
            NativeDataWrapper.this.iconUrl = iconUrl;
            return this;
        }

        public Builder setImageUrl(String imageUrl) {
            NativeDataWrapper.this.imageUrl = imageUrl;
            return this;
        }

        public Builder setClickUrl(String clickUrl) {
            NativeDataWrapper.this.clickUrl = clickUrl;
            return this;
        }

        public Builder setVideoUrl(String videoUrl) {
            NativeDataWrapper.this.videoUrl = videoUrl;
            return this;
        }

        public Builder setVideoAdm(String videoAdm) {
            NativeDataWrapper.this.videoAdm = videoAdm;
            return this;
        }

        public Builder setTitle(String title) {
            NativeDataWrapper.this.title = title;
            return this;
        }

        public Builder setDescription(String description) {
            NativeDataWrapper.this.description = description;
            return this;
        }

        public Builder setCallToAction(String callToAction) {
            NativeDataWrapper.this.callToAction = callToAction;
            return this;
        }

        public Builder setSponsored(String sponsored) {
            NativeDataWrapper.this.sponsored = sponsored;
            return this;
        }

        public Builder setAgeRestriction(String ageRestriction) {
            NativeDataWrapper.this.ageRestriction = ageRestriction;
            return this;
        }

        public Builder setRating(float rating) {
            NativeDataWrapper.this.rating = rating;
            return this;
        }

        public NativeDataWrapper build() {
            return NativeDataWrapper.this;
        }
    }
}
