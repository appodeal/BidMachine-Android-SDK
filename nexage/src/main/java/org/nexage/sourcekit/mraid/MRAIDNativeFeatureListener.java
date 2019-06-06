package org.nexage.sourcekit.mraid;

import android.webkit.WebView;

/**
 * ***************************************************************************
 * A listener for MRAIDView/MRAIDInterstitial to listen for notifications
 * when the following native features are requested from a creative:
 *
 * * make a phone call
 * * add a calendar entry
 * * play a video (external)
 * * open a web page in a browser
 * * store a picture
 * * send an SMS
 *
 * If you don't implement this interface, the default for
 * supporting these features in the creative will be false.
 * ****************************************************************************
 */

public interface MRAIDNativeFeatureListener {

    void mraidNativeFeatureCallTel(String url);

    void mraidNativeFeatureCreateCalendarEvent(String eventJSON);

    void mraidNativeFeaturePlayVideo(String url);

    void mraidNativeFeatureOpenBrowser(String url, WebView view);

    void mraidNativeFeatureStorePicture(String url);

    void mraidNativeFeatureSendSms(String url);

}
