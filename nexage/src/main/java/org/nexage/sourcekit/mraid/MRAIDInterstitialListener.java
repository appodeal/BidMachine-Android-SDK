package org.nexage.sourcekit.mraid;

public interface MRAIDInterstitialListener {

    /**
     * ***************************************************************************
     * A listener for basic MRAIDInterstitial ad functionality.
     * ****************************************************************************
     */

    void mraidInterstitialLoaded(MRAIDInterstitial mraidInterstitial);

    void mraidInterstitialShow(MRAIDInterstitial mraidInterstitial);

    void mraidInterstitialHide(MRAIDInterstitial mraidInterstitial);

    void mraidInterstitialNoFill(MRAIDInterstitial mraidInterstitial);
}
