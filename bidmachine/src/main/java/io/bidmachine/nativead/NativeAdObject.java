package io.bidmachine.nativead;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import org.nexage.sourcekit.vast.model.VASTModel;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.bidmachine.AdObjectImpl;
import io.bidmachine.BMException;
import io.bidmachine.MediaAssetType;
import io.bidmachine.core.Logger;
import io.bidmachine.core.Utils;
import io.bidmachine.core.VisibilityTracker;
import io.bidmachine.displays.NativeAdObjectParams;
import io.bidmachine.nativead.utils.ImageHelper;
import io.bidmachine.nativead.utils.NativeContainer;
import io.bidmachine.nativead.utils.NativeInteractor;
import io.bidmachine.nativead.utils.NativeMediaPrivateData;
import io.bidmachine.nativead.utils.NativePrivateData;
import io.bidmachine.nativead.view.MediaView;
import io.bidmachine.nativead.view.NativeIconView;
import io.bidmachine.nativead.view.NativeMediaView;

public abstract class NativeAdObject extends AdObjectImpl<NativeAd, NativeAdObjectParams>
        implements NativePrivateData, NativeMediaPrivateData, NativeContainer, NativeInteractor, View.OnClickListener {

    private static final String INSTALL = "Install";
    private static final float DEFAULT_RATING = 5;

    private NativeAdContentLayout container;
    private MediaView mediaView;
    private ProgressDialog progressDialog;

    private Handler progressDialogCanceller;
    private Runnable progressRunnable;

    private boolean impressionTracked;
    private boolean isRegisteredForInteraction;

    @Nullable
    private Bitmap iconBitmap;
    @Nullable
    private Uri iconUri;
    @Nullable
    private Bitmap imageBitmap;
    @Nullable
    private Uri imageUri;
    @Nullable
    private Uri videoUri;
    @Nullable
    private VASTModel videoVastModel;

    public NativeAdObject(NativeAdObjectParams adObjectParams) {
        super(adObjectParams);
    }

    @NonNull
    @Override
    public String getTitle() {
        return getParams().getTitle();
    }

    @NonNull
    @Override
    public String getDescription() {
        return getParams().getTitle();
    }

    @Nullable
    @Override
    public String getCallToAction() {
        String callToAction = getParams().getCallToAction();
        return TextUtils.isEmpty(callToAction) ? INSTALL : callToAction;
    }

    @Override
    public String getSponsored() {
        return getParams().getSponsored();
    }

    @Override
    public String getAgeRestrictions() {
        return getParams().getAgeRestrictions();
    }

    @Override
    public float getRating() {
        return Math.max(DEFAULT_RATING, getParams().getRating());
    }

    @Nullable
    @Override
    public String getIconUrl() {
        return getParams().getIconUrl();
    }

    @Nullable
    @Override
    public String getImageUrl() {
        return getParams().getImageUrl();
    }

    @Nullable
    @Override
    public String getClickUrl() {
        return getParams().getClickUrl();
    }

    @Nullable
    @Override
    public String getVideoUrl() {
        return getParams().getVideoUrl();
    }

    @Nullable
    @Override
    public String getVideoAdm() {
        return getParams().getVideoAdm();
    }

    @Override
    public void setIconBitmap(@Nullable Bitmap iconBitmap) {
        this.iconBitmap = iconBitmap;
    }

    @Override
    public void setIconUri(@Nullable Uri iconUri) {
        this.iconUri = iconUri;
    }

    @Override
    @Nullable
    public Uri getIconUri() {
        return iconUri;
    }

    @Nullable
    @Override
    public Bitmap getIconBitmap() {
        return iconBitmap;
    }

    @Override
    public void setImageUri(@Nullable Uri imageUri) {
        this.imageUri = imageUri;
    }

    @Override
    @Nullable
    public Uri getImageUri() {
        return imageUri;
    }

    @Override
    public void setImageBitmap(@Nullable Bitmap imageBitmap) {
        this.imageBitmap = imageBitmap;
    }

    @Nullable
    @Override
    public Bitmap getImageBitmap() {
        return imageBitmap;
    }

    @Override
    public void setVideoUri(@Nullable Uri videoUri) {
        this.videoUri = videoUri;
    }

    @Nullable
    @Override
    public Uri getVideoUri() {
        return videoUri;
    }

    @Override
    public void setVideoVastModel(@Nullable VASTModel videoVastModel) {
        this.videoVastModel = videoVastModel;
    }

    @Override
    @Nullable
    public VASTModel getVideoVastModel() {
        return videoVastModel;
    }

    @Override
    public boolean hasVideo() {
        return videoUri != null
                || !TextUtils.isEmpty(getParams().getVideoUrl())
                || !TextUtils.isEmpty(getParams().getVideoAdm());
    }

    @Override
    protected void onDestroy() {
        unregisterViewForInteraction();
        if (iconBitmap != null) {
            if (!iconBitmap.isRecycled()) {
                iconBitmap.recycle();
            }
            iconBitmap = null;
        }
        if (imageBitmap != null) {
            if (!imageBitmap.isRecycled()) {
                imageBitmap.recycle();
            }
            imageBitmap = null;
        }
        if (videoUri != null && videoUri.getPath() != null) {
            File file = new File(videoUri.getPath());
            if (file.exists()) {
                //noinspection ResultOfMethodCallIgnored
                file.delete();
            }
            videoUri = null;
        }
    }

    @Override
    public void registerViewForInteraction(final NativeAdContentLayout view) {
        if (container != null) {
            container.setOnClickListener(null);
        }
        view.setOnClickListener(this);
        processChildViews(view);
        container = view;
        if (!impressionTracked) {
            VisibilityTracker.startTracking(
                    container,
                    getParams().getViewabilityTimeThresholdMs(),
                    getParams().getViewabilityPixelThreshold(),
                    new VisibilityTracker.VisibilityChangeCallback() {
                        @Override
                        public void onViewShown() {
                            impressionTracked = true;
                            processShown();
                            checkRequiredAssets(container);
                        }

                        @Override
                        public void onViewTrackingFinished() {
                            processImpression();
                        }
                    });
        }
        if (mediaView != null) {
            mediaView.onViewAppearOnScreen();
            mediaView.startVideoVisibilityCheckerTimer();
        }
        isRegisteredForInteraction = true;
    }

    private void processChildViews(ViewGroup view) {
        for (int i = 0; i < view.getChildCount(); i++) {
            View v = view.getChildAt(i);
            if (!(v instanceof MediaView)) {
                if (v instanceof Button) {
                    Button b = (Button) v;
                    b.setOnClickListener(this);
                }
                if (v instanceof ViewGroup) {
                    processChildViews((ViewGroup) v);
                }
            }
        }
    }

    @Override
    public void unregisterViewForInteraction() {
        if (container != null) {
            container.setOnClickListener(null);
            VisibilityTracker.stopTracking(container);
        }
        if (mediaView != null) {
            mediaView.stopVideoVisibilityCheckerTimer();
        }
        isRegisteredForInteraction = false;
    }

    @Override
    public View getProviderView(Context context) {
        return null;
    }

    @Override
    public void setNativeIconView(@NonNull NativeIconView nativeIconView) {
        Context context = nativeIconView.getContext();
        View iconView = obtainIconView(context);
        Utils.removeViewFromParent(iconView);
        nativeIconView.removeAllViews();
        nativeIconView.addView(iconView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT)
        );
    }

    protected View obtainIconView(Context context) {
        return createIconView(context);
    }

    private View createIconView(Context context) {
        final ImageView iconView = new ImageView(context);
        iconView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        final NativeRequest request = getAd().getAdRequest();
        if (request != null && request.containsAssetType(MediaAssetType.Icon)) {
            ImageHelper.fillImageView(getContext(), iconView, iconUri, iconBitmap);
        }
        return iconView;
    }

    @Override
    public void setNativeMediaView(@NonNull NativeMediaView nativeMediaView) {
        final RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        mediaView = new MediaView(nativeMediaView.getContext());
        final NativeRequest request = getAd().getAdRequest();
        if (request != null
                && (request.containsAssetType(MediaAssetType.Image)
                || request.containsAssetType(MediaAssetType.Video))) {
            mediaView.setNativeAdObject(this);
        }
        nativeMediaView.removeAllViews();
        nativeMediaView.addView(mediaView, layoutParams);
    }

    /* progress dialog */
    protected void showProgressDialog(Context context) {
        if (container != null && context instanceof Activity && mayShowProgressDialog()) {
            Activity activity = (Activity) context;
            if (Utils.canAddWindowToActivity(activity)) {
                container.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
                    @Override
                    public void onViewAttachedToWindow(View v) {

                    }

                    @Override
                    public void onViewDetachedFromWindow(View v) {
                        v.removeOnAttachStateChangeListener(this);
                        hideProgressDialog();
                    }
                });
                progressDialog = ProgressDialog.show(activity, "", "Loading...");
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.setCancelable(false);
                progressRunnable = new Runnable() {
                    @Override
                    public void run() {
                        hideProgressDialog();
                    }
                };
                progressDialogCanceller = new Handler(Looper.getMainLooper());
                progressDialogCanceller.postDelayed(progressRunnable, 5000);
            }
        }
    }

    private boolean mayShowProgressDialog() {
        return progressDialog == null || !progressDialog.isShowing();
    }

    protected void hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
            progressDialog = null;
        }
        if (progressRunnable != null && progressDialogCanceller != null) {
            progressDialogCanceller.removeCallbacks(progressRunnable);
            progressDialogCanceller = null;
            progressRunnable = null;
        }
    }

    private void checkRequiredAssets(NativeAdContentLayout nativeAdContentLayout) {
        Map<View, String> requiredViews = new HashMap<>();
        List<String> nonNecessaryView = new ArrayList<>();
        List<String> notAddedViews = new ArrayList<>();

        if (nativeAdContentLayout.getTitleView() == null) {
            notAddedViews.add("Title");
        } else {
            requiredViews.put(nativeAdContentLayout.getTitleView(), "Title");
        }

        if (nativeAdContentLayout.getCallToActionView() == null) {
            notAddedViews.add("CallToAction");
        } else {
            requiredViews.put(nativeAdContentLayout.getCallToActionView(), "CallToAction");
        }

        if (nativeAdContentLayout.getIconView() == null && nativeAdContentLayout.getMediaView() == null) {
            notAddedViews.add("NativeIconView/NativeMediaView");
        } else {
            final NativeRequest request = getAd().getAdRequest();
            if (request != null && request.containsAssetType(MediaAssetType.Icon)) {
                requiredViews.put(nativeAdContentLayout.getIconView(), "NativeIconView");
            } else if (nativeAdContentLayout.getIconView() != null) {
                nonNecessaryView.add("NativeIconView");
            }

            if (request != null
                    && (request.containsAssetType(MediaAssetType.Image)
                    || request.containsAssetType(MediaAssetType.Video))) {
                requiredViews.put(nativeAdContentLayout.getMediaView(), "NativeMediaView");
            } else if (nativeAdContentLayout.getMediaView() != null) {
                nonNecessaryView.add("NativeMediaView");
            }
        }

        if (getProviderView(nativeAdContentLayout.getContext()) != null) {
            if (nativeAdContentLayout.getProviderView() == null) {
                notAddedViews.add("ProviderView");
            } else {
                requiredViews.put(nativeAdContentLayout.getProviderView(), "ProviderView");
            }
        }

        if (!notAddedViews.isEmpty()) {
            Logger.log(new BMException(String.format("Required assets: %s are not added to NativeAdView", notAddedViews.toString())));
        }

        if (!nonNecessaryView.isEmpty()) {
            Logger.log(new BMException(String.format("Non necessary assets: %s are not added to NativeAdView", nonNecessaryView.toString())));
        }

        Map<View, String> notFoundViews = findRequiredViews(Utils.getViewRectangle(nativeAdContentLayout), nativeAdContentLayout, requiredViews);
        if (!notFoundViews.isEmpty()) {
            Logger.log(new BMException(String.format("Required assets: %s are not visible or not found", notFoundViews.values().toString())));
        }
    }

    private static Map<View, String> findRequiredViews(Rect parentRect, View view, Map<View, String> requiredViews) {
        if (requiredViews.containsKey(view)) {
            if (Utils.isViewHaveSize(view) && view.isShown() && !Utils.isViewTransparent(view) && Utils.isViewInsideParentRect(parentRect, view)) {
                requiredViews.remove(view);
            }
        } else if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                findRequiredViews(parentRect, viewGroup.getChildAt(i), requiredViews);
            }
        }
        return requiredViews;
    }

    protected void loadAsset() {
        NativeRequest adRequest = getAd().getAdRequest();
        if (adRequest != null) {
            (new AssetLoader(getContext(), adRequest, this, getParams(), this)).downloadNativeAdsImages();
        }
    }

    @Override
    public boolean isRegisteredForInteraction() {
        return isRegisteredForInteraction;
    }

    @Override
    public void dispatchShown() {
        processShown();
    }

    @Override
    public void onClick(View view) {
        onClicked(view.getContext());
        processClicked();
    }

    @Override
    public void dispatchClick() {
        onClicked(getContext());
        processClicked();
    }

    @Override
    public void dispatchImpression() {
        processImpression();
    }

    @Override
    public void dispatchVideoPlayFinished() {
    }

    protected abstract void onClicked(Context context);
}