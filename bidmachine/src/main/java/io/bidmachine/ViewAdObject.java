package io.bidmachine;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import io.bidmachine.core.Utils;
import io.bidmachine.core.VisibilityTracker;
import io.bidmachine.displays.DisplayAdObjectParams;
import io.bidmachine.utils.BMError;

public abstract class ViewAdObject<AdType extends ViewAd>
        extends AdObjectImpl<AdType, DisplayAdObjectParams> {

    protected enum MeasureMode {
        Match, Wrap, Direct;

        int getSize(Context context, int directSize) {
            switch (this) {
                case Direct: {
                    return Math.round(directSize * Utils.getScreenDensity(context));
                }
                case Wrap: {
                    return ViewGroup.LayoutParams.WRAP_CONTENT;
                }
                default:
                    return ViewGroup.LayoutParams.MATCH_PARENT;
            }
        }
    }

    private View bannerView;

    private MeasureMode widthMeasureMode = MeasureMode.Direct;
    private MeasureMode heightMeasureMode = MeasureMode.Direct;

    public ViewAdObject(DisplayAdObjectParams adDisplay) {
        super(adDisplay);
    }

    void show(ViewGroup container) {
        if (container == null) {
            processLoadFail(BMError.Internal);
            return;
        }
        if (bannerView != null) {
            VisibilityTracker.stopTracking(bannerView);
        }
        container.removeAllViews();
        bannerView = obtainBannerView();
        final ViewGroup.LayoutParams params;
        if (container instanceof FrameLayout) {
            params = new FrameLayout.LayoutParams(getScaledWidth(), getScaledHeight(), Gravity.CENTER);
        } else {
            params = new ViewGroup.LayoutParams(getScaledWidth(), getScaledHeight());
        }
        container.addView(bannerView, params);
        VisibilityTracker.startTracking(
                bannerView,
                getParams().getViewabilityTimeThresholdMs(),
                getParams().getViewabilityPixelThreshold(),
                new VisibilityTracker.VisibilityChangeCallback() {
                    @Override
                    public void onViewShown() {
                        processShown();
                    }

                    @Override
                    public void onViewTrackingFinished() {
                        processImpression();
                    }
                });
    }

    @Override
    protected void onImpression() {
        super.onImpression();
        VisibilityTracker.stopTracking(bannerView);
    }

    @Override
    public void processDestroy() {
        if (bannerView != null) {
            if (bannerView.getParent() instanceof ViewGroup) {
                ((ViewGroup) bannerView.getParent()).removeView(bannerView);
            }
            VisibilityTracker.stopTracking(bannerView);
        }
        super.processDestroy();
    }

    public int getWidth() {
        return getParams().getWidth();
    }

    public int getHeight() {
        return getParams().getHeight();
    }

    private int getScaledWidth() {
        return widthMeasureMode.getSize(getContext(), getWidth());
    }

    private int getScaledHeight() {
        return heightMeasureMode.getSize(getContext(), getHeight());
    }

    protected abstract View obtainBannerView();

}