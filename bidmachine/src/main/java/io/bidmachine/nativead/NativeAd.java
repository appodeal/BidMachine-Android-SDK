package io.bidmachine.nativead;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import io.bidmachine.*;
import io.bidmachine.core.Logger;
import io.bidmachine.models.AdObjectParams;
import io.bidmachine.nativead.utils.NativeContainer;
import io.bidmachine.nativead.utils.NativeInteractor;
import io.bidmachine.nativead.utils.NativeMediaPublicData;
import io.bidmachine.nativead.utils.NativePublicData;
import io.bidmachine.nativead.view.NativeIconView;
import io.bidmachine.nativead.view.NativeMediaView;
import io.bidmachine.unified.UnifiedNativeAd;
import io.bidmachine.unified.UnifiedNativeAdRequestParams;

public final class NativeAd
        extends BidMachineAd<NativeAd, NativeRequest, NativeAdObject, AdObjectParams, UnifiedNativeAdRequestParams, NativeListener>
        implements NativePublicData, NativeMediaPublicData, NativeContainer, NativeInteractor {

    public NativeAd(@NonNull Context context) {
        super(context, AdsType.Native);
    }

    @Override
    protected NativeAdObject createAdObject(@NonNull ContextProvider contextProvider,
                                            @NonNull NativeRequest adRequest,
                                            @NonNull NetworkAdapter adapter,
                                            @NonNull AdObjectParams adObjectParams,
                                            @NonNull AdProcessCallback processCallback) {
        UnifiedNativeAd unifiedNativeAd = adapter.createNativeAd();
        if (unifiedNativeAd == null) {
            return null;
        }
        return new NativeAdObject(contextProvider, processCallback, adRequest, adObjectParams, unifiedNativeAd);
    }

    @Nullable
    @Override
    public String getTitle() {
        return hasLoadedObject() ? getLoadedObject().getTitle() : null;
    }

    @Nullable
    @Override
    public String getDescription() {
        return hasLoadedObject() ? getLoadedObject().getDescription() : null;
    }

    @Nullable
    @Override
    public String getCallToAction() {
        return hasLoadedObject() ? getLoadedObject().getCallToAction() : null;
    }

    @Nullable
    @Override
    public String getSponsored() {
        return hasLoadedObject() ? getLoadedObject().getSponsored() : null;
    }

    @Nullable
    @Override
    public String getAgeRestrictions() {
        return hasLoadedObject() ? getLoadedObject().getAgeRestrictions() : null;
    }

    @Override
    public float getRating() {
        return hasLoadedObject() ? getLoadedObject().getRating() : 0.0f;
    }

    @Nullable
    @Override
    public Uri getIconUri() {
        return hasLoadedObject() ? getLoadedObject().getIconUri() : null;
    }

    @Override
    public Bitmap getIconBitmap() {
        return hasLoadedObject() ? getLoadedObject().getIconBitmap() : null;
    }

    @Nullable
    @Override
    public Uri getImageUri() {
        return hasLoadedObject() ? getLoadedObject().getImageUri() : null;
    }

    @Override
    public Bitmap getImageBitmap() {
        return hasLoadedObject() ? getLoadedObject().getImageBitmap() : null;
    }

    @Nullable
    @Override
    public Uri getVideoUri() {
        return hasLoadedObject() ? getLoadedObject().getVideoUri() : null;
    }

    @Override
    public boolean hasVideo() {
        return hasLoadedObject() && getLoadedObject().hasVideo();
    }

    @Override
    public View getProviderView(Context context) {
        return hasLoadedObject() ? getLoadedObject().getProviderView(context) : null;
    }

    @Override
    public void setNativeIconView(NativeIconView nativeIconView) {
        if (hasLoadedObject()) {
            getLoadedObject().setNativeIconView(nativeIconView);
        }
    }

    @Override
    public void setNativeMediaView(NativeMediaView nativeMediaView) {
        if (hasLoadedObject()) {
            getLoadedObject().setNativeMediaView(nativeMediaView);
        }
    }

    @Override
    public void registerViewForInteraction(NativeAdContentLayout contentLayout) {
        if (hasLoadedObject()) {
            getLoadedObject().registerViewForInteraction(contentLayout);
        }
    }

    @Override
    public void unregisterViewForInteraction() {
        if (hasLoadedObject()) {
            getLoadedObject().unregisterViewForInteraction();
        }
    }

    @Override
    public boolean isRegisteredForInteraction() {
        return hasLoadedObject() && getLoadedObject().isRegisteredForInteraction();
    }

    @Override
    public void dispatchShown() {
        if (hasLoadedObject()) {
            getLoadedObject().dispatchShown();
        }
    }

    @Override
    public void dispatchImpression() {
        if (hasLoadedObject()) {
            getLoadedObject().dispatchImpression();
        }
    }

    @Override
    public void dispatchClick() {
        if (hasLoadedObject()) {
            getLoadedObject().dispatchClick();
        }
    }

    @Override
    public void dispatchVideoPlayFinished() {
        if (hasLoadedObject()) {
            getLoadedObject().dispatchVideoPlayFinished();
        }
    }

    private boolean hasLoadedObject() {
        if (getLoadedObject() == null) {
            Logger.log(toStringShort() + ": not loaded, please load ads first!");
            return false;
        }
        return true;
    }

}