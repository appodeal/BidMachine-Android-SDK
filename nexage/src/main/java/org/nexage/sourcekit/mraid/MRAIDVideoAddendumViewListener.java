package org.nexage.sourcekit.mraid;

public interface MRAIDVideoAddendumViewListener {

    /**
     * ***************************************************************************
     * A listener for basic MRAIDView banner ad functionality.
     * ****************************************************************************
     */

    void mraidVideoAddendumViewLoaded(MRAIDVideoAddendumView mraidView);

    void mraidVideoAddendumViewExpand(MRAIDVideoAddendumView mraidView);

    void mraidVideoAddendumViewClose(MRAIDVideoAddendumView mraidView);

    boolean mraidVideoAddendumViewResize(MRAIDVideoAddendumView mraidView, int width, int height, int offsetX, int offsetY);

    void mraidVideoAddendumViewNoFill(MRAIDVideoAddendumView mraidView);

    void mraidVideoAddendumViewStarted(MRAIDVideoAddendumView mraidView);
    void mraidVideoAddendumViewStopped(MRAIDVideoAddendumView mraidView);
    void mraidVideoAddendumViewSkipped(MRAIDVideoAddendumView mraidView);
    void mraidVideoAddendumViewVideoStart(MRAIDVideoAddendumView mraidView);
    void mraidVideoAddendumViewFirstQuartile(MRAIDVideoAddendumView mraidView);
    void mraidVideoAddendumViewMidpoint(MRAIDVideoAddendumView mraidView);
    void mraidVideoAddendumViewThirdQuartile(MRAIDVideoAddendumView mraidView);
    void mraidVideoAddendumViewComplete(MRAIDVideoAddendumView mraidView);
    void mraidVideoAddendumViewPaused(MRAIDVideoAddendumView mraidView);
    void mraidVideoAddendumViewPlaying(MRAIDVideoAddendumView mraidView);
    void mraidVideoAddendumViewError(MRAIDVideoAddendumView mraidView, String error);
    void mraidVideoAddendumViewClickThru(MRAIDVideoAddendumView mraidView, String url);
    void mraidVideoAddendumViewUserClose(MRAIDVideoAddendumView mraidView);
    void mraidVideoAddendumViewSkippableStateChange(MRAIDVideoAddendumView mraidView, boolean state);
    void mraidVideoAddendumViewLog(MRAIDVideoAddendumView mraidView, String log);

}
