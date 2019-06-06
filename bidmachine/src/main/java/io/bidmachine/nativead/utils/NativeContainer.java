package io.bidmachine.nativead.utils;

import android.content.Context;
import android.view.View;

import io.bidmachine.nativead.NativeAdContentLayout;
import io.bidmachine.nativead.view.NativeIconView;
import io.bidmachine.nativead.view.NativeMediaView;

public interface NativeContainer {

    /**
     * Get provider view, that must be shown with ad
     *
     * @return provider view
     */
    View getProviderView(Context context);

    /**
     * Set NativeIconView for display icon (see {@link NativeMediaPublicData#getIconBitmap()}
     *
     * @param nativeIconView - instance of {@link NativeIconView}
     */
    void setNativeIconView(NativeIconView nativeIconView);

    /**
     * Set NativeMediaView for display media content (see {@link NativeMediaPublicData#getVideoUri()},
     * {@link NativeMediaPublicData#getImageBitmap()}
     *
     * @param nativeMediaView - instance of {@link NativeMediaView}
     */
    void setNativeMediaView(NativeMediaView nativeMediaView);

    /**
     * Register view for handle Ad interactions
     *
     * @param contentLayout - instance of {@link NativeAdContentLayout}
     */
    void registerViewForInteraction(NativeAdContentLayout contentLayout);

    /**
     * Unregister registered view from receive Ad interactions (see {@link NativeContainer#registerViewForInteraction(NativeAdContentLayout)}
     */
    void unregisterViewForInteraction();

    /**
     * @return {@code true} if Ad has registered view for handle interactions
     */
    boolean isRegisteredForInteraction();

}
