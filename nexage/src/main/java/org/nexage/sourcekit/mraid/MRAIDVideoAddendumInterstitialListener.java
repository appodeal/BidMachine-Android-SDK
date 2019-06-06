package org.nexage.sourcekit.mraid;

public interface MRAIDVideoAddendumInterstitialListener {

    /**
     * ***************************************************************************
     * A listener for basic MRAIDInterstitial ad functionality.
     * ****************************************************************************
     */

    void mraidVideoAddendumInterstitialLoaded(MRAIDVideoAddendumInterstitial mraidInterstitial);

    void mraidVideoAddendumInterstitialShow(MRAIDVideoAddendumInterstitial mraidInterstitial);

    void mraidVideoAddendumInterstitialHide(MRAIDVideoAddendumInterstitial mraidInterstitial);

    void mraidVideoAddendumInterstitialNoFill(MRAIDVideoAddendumInterstitial mraidInterstitial);

    void mraidVideoAddendumViewStarted(MRAIDVideoAddendumInterstitial mraidView);
    void mraidVideoAddendumViewStopped(MRAIDVideoAddendumInterstitial mraidView);
    void mraidVideoAddendumViewSkipped(MRAIDVideoAddendumInterstitial mraidView);
    void mraidVideoAddendumViewVideoStart(MRAIDVideoAddendumInterstitial mraidView);
    void mraidVideoAddendumViewFirstQuartile(MRAIDVideoAddendumInterstitial mraidView);
    void mraidVideoAddendumViewMidpoint(MRAIDVideoAddendumInterstitial mraidView);
    void mraidVideoAddendumViewThirdQuartile(MRAIDVideoAddendumInterstitial mraidView);
    void mraidVideoAddendumViewComplete(MRAIDVideoAddendumInterstitial mraidView);
    void mraidVideoAddendumViewPaused(MRAIDVideoAddendumInterstitial mraidView);
    void mraidVideoAddendumViewPlaying(MRAIDVideoAddendumInterstitial mraidView);
    void mraidVideoAddendumViewError(MRAIDVideoAddendumInterstitial mraidView, String error);
    void mraidVideoAddendumViewClickThru(MRAIDVideoAddendumInterstitial mraidView, String url);
    void mraidVideoAddendumViewUserClose(MRAIDVideoAddendumInterstitial mraidView);
    void mraidVideoAddendumViewSkippableStateChange(MRAIDVideoAddendumInterstitial mraidView, boolean state);
    void mraidVideoAddendumViewLog(MRAIDVideoAddendumInterstitial mraidView, String log);

}
