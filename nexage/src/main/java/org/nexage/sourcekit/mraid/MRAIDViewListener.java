package org.nexage.sourcekit.mraid;

public interface MRAIDViewListener {

    /**
     * ***************************************************************************
     * A listener for basic MRAIDView banner ad functionality.
     * ****************************************************************************
     */

    void mraidViewLoaded(MRAIDView mraidView);

    void mraidViewExpand(MRAIDView mraidView);

    void mraidViewClose(MRAIDView mraidView);

    boolean mraidViewResize(MRAIDView mraidView, int width, int height, int offsetX, int offsetY);

    void mraidViewNoFill(MRAIDView mraidView);
}
