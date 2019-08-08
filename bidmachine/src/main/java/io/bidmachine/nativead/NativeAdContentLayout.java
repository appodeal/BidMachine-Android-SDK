package io.bidmachine.nativead;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import io.bidmachine.R;
import io.bidmachine.core.Logger;
import io.bidmachine.nativead.view.NativeIconView;
import io.bidmachine.nativead.view.NativeMediaView;

import java.util.ArrayList;
import java.util.List;

public class NativeAdContentLayout extends NativeAdContainer {

    protected View titleView;
    protected View callToActionView;
    protected View ratingView;
    protected View descriptionView;
    protected View providerView;
    protected NativeIconView iconView;
    protected NativeMediaView mediaView;

    private NativeAd currentAd;

    private int titleViewId = NO_ID;
    private int callToActionViewId = NO_ID;
    private int ratingViewId = NO_ID;
    private int descriptionViewId = NO_ID;
    private int providerViewId = NO_ID;
    private int iconViewId = NO_ID;
    private int mediaViewId = NO_ID;

    public NativeAdContentLayout(Context context) {
        this(context, null);
    }

    public NativeAdContentLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NativeAdContentLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (attrs != null) {
            final TypedArray attrsArray = context.obtainStyledAttributes(attrs, R.styleable.NativeAdContentLayout, defStyleAttr, 0);
            titleViewId = attrsArray.getResourceId(R.styleable.NativeAdContentLayout_titleViewId, NO_ID);
            callToActionViewId = attrsArray.getResourceId(R.styleable.NativeAdContentLayout_callToActionViewId, NO_ID);
            ratingViewId = attrsArray.getResourceId(R.styleable.NativeAdContentLayout_ratingViewId, NO_ID);
            descriptionViewId = attrsArray.getResourceId(R.styleable.NativeAdContentLayout_descriptionViewId, NO_ID);
            providerViewId = attrsArray.getResourceId(R.styleable.NativeAdContentLayout_providerViewId, NO_ID);
            iconViewId = attrsArray.getResourceId(R.styleable.NativeAdContentLayout_iconViewId, NO_ID);
            mediaViewId = attrsArray.getResourceId(R.styleable.NativeAdContentLayout_mediaViewId, NO_ID);
            attrsArray.recycle();
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        setTitleView(findViewById(titleViewId));
        setCallToActionView(findViewById(callToActionViewId));
        setRatingView(findViewById(ratingViewId));
        setDescriptionView(findViewById(descriptionViewId));
        setProviderView(findViewById(providerViewId));
        setIconView((NativeIconView) findViewById(iconViewId));
        setMediaView((NativeMediaView) findViewById(mediaViewId));
    }

    public void setTitleView(View view) {
        titleView = view;
    }

    public void setCallToActionView(View view) {
        callToActionView = view;
    }

    public void setRatingView(View view) {
        ratingView = view;
    }

    public void setDescriptionView(View view) {
        descriptionView = view;
    }

    public void setProviderView(View view) {
        providerView = view;
    }

    public void setIconView(NativeIconView view) {
        iconView = view;
    }

    public void setMediaView(NativeMediaView mediaView) {
        this.mediaView = mediaView;
    }

    public View getTitleView() {
        return titleView;
    }

    public View getCallToActionView() {
        return callToActionView;
    }

    public View getRatingView() {
        return ratingView;
    }

    public View getDescriptionView() {
        return descriptionView;
    }

    public View getProviderView() {
        return providerView;
    }

    public View getIconView() {
        return iconView;
    }

    public NativeMediaView getMediaView() {
        return mediaView;
    }

    public List<View> getClickableViews() {
        List<View> clickableViews = new ArrayList<>();
        if (titleView != null) {
            clickableViews.add(titleView);
        }
        if (descriptionView != null) {
            clickableViews.add(descriptionView);
        }
        if (callToActionView != null) {
            clickableViews.add(callToActionView);
        }
        if (ratingView != null) {
            clickableViews.add(ratingView);
        }
        if (iconView != null) {
            clickableViews.add(iconView);
        }
        if (mediaView != null) {
            clickableViews.add(mediaView);
        }
        return clickableViews;
    }

    public void bind(@Nullable NativeAd ad) {
        if (ad == null || !ad.isLoaded()) {
            return;
        }
        if (titleView instanceof TextView) {
            ((TextView) titleView).setText(ad.getTitle());
        }
        if (descriptionView instanceof TextView) {
            ((TextView) descriptionView).setText(ad.getDescription());
        }
        if (ratingView instanceof RatingBar) {
            RatingBar ratingBar = (RatingBar) ratingView;
            if (ad.getRating() == 0) {
                ratingBar.setVisibility(View.INVISIBLE);
            } else {
                ratingBar.setVisibility(View.VISIBLE);
                ratingBar.setStepSize(0.1f);
                ratingBar.setRating(ad.getRating());
            }
        }
        if (callToActionView instanceof TextView) {
            ((TextView) callToActionView).setText(ad.getCallToAction());
        }
        if (providerView instanceof ViewGroup) {
            final View providerContent = ad.getProviderView(getContext());
            if (providerContent != null) {
                if (providerContent.getParent() != null
                        && providerContent.getParent() instanceof ViewGroup) {
                    ((ViewGroup) providerContent.getParent()).removeView(providerView);
                }
                ((ViewGroup) providerView).addView(providerContent,
                        new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT));
            }
        }
    }

    public void registerViewForInteraction(NativeAd ad) {
        if (!ad.isLoaded()) {
            Logger.log("You using not loaded native ad, please load it first!");
            return;
        }
        unregisterViewForInteraction();
        if (iconView != null) {
            iconView.removeAllViews();
        }
        if (mediaView != null) {
            mediaView.removeAllViews();
        }
        this.currentAd = ad;
        if (iconView != null) {
            currentAd.setNativeIconView(iconView);
        }
        if (mediaView != null) {
            currentAd.setNativeMediaView(mediaView);
        }
        currentAd.registerViewForInteraction(this);
    }

    public void unregisterViewForInteraction() {
        if (currentAd != null) {
            currentAd.unregisterViewForInteraction();
        }
    }

    public void destroy() {
        if (currentAd != null) {
            currentAd.destroy();
        }
    }

    public boolean isRegistered() {
        return currentAd != null && currentAd.isRegisteredForInteraction();
    }

}