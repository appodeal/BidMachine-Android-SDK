package org.nexage.sourcekit.mraid;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.ConsoleMessage;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import org.nexage.sourcekit.mraid.internal.MRAIDHtmlProcessor;
import org.nexage.sourcekit.mraid.internal.MRAIDLog;
import org.nexage.sourcekit.mraid.internal.MRAIDLog.LOG_LEVEL;
import org.nexage.sourcekit.mraid.internal.MRAIDNativeFeatureManager;
import org.nexage.sourcekit.mraid.internal.MRAIDParser;
import org.nexage.sourcekit.mraid.properties.MRAIDOrientationProperties;
import org.nexage.sourcekit.mraid.properties.MRAIDResizeProperties;
import org.nexage.sourcekit.vast.view.CircleCountdownView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

@SuppressLint("ViewConstructor")
public class MRAIDVideoAddendumView extends RelativeLayout {

    public static final String VERSION = "1.1.1";
    public final static int STATE_LOADING = 0;
    public final static int STATE_DEFAULT = 1;
    public final static int STATE_EXPANDED = 2;
    public final static int STATE_RESIZED = 3;
    public final static int STATE_HIDDEN = 4;
    private final static String TAG = "MRAIDVideoAddendumView";
    // in dip
    private final static int CLOSE_REGION_SIZE = 50;
    // state
    private final boolean isInterstitial;
    // Stores the requested orientation for the Activity to which this MRAIDView belongs.
    // This is needed to restore the Activity's requested orientation in the event that
    // the view itself requires an orientation lock.
    private final int originalRequestedOrientation;
    // UI elements
    private WebView webView;
    private WebView webViewPart2;
    private WebView currentWebView;
    private MRAIDWebChromeClient mraidWebChromeClient;
    private MRAIDWebViewClient mraidWebViewClient;
    private RelativeLayout expandedView;
    private RelativeLayout resizedView;
    private CircleCountdownView closeRegion;
    private Context context;
    private Activity interstitialActivity;
    private String baseUrl;
    private boolean preload;
    private boolean isShown = false;
    private String mData;
    // gesture detector for capturing unwanted gestures
    private GestureDetector gestureDetector;
    private int state;
    private boolean isViewable;
    private boolean isCloseClickable = true;

    // The only property of the MRAID expandProperties we need to keep track of
    // on the native side is the useCustomClose property.
    // The width, height, and isModal properties are not used in MRAID v2.0.
    private boolean useCustomClose;
    private MRAIDOrientationProperties orientationProperties;
    private MRAIDResizeProperties resizeProperties;

    private MRAIDNativeFeatureManager nativeFeatureManager;

    // listeners
    private MRAIDVideoAddendumViewListener listener;
    private MRAIDNativeFeatureListener nativeFeatureListener;

    // used for setting positions and sizes (all in pixels, not dpi)
    private DisplayMetrics displayMetrics;
    private int contentViewTop;
    private Rect currentPosition;
    private Rect defaultPosition;
    private Size maxSize;

    private Size screenSize;
    // state to help set positions and sizes
    private boolean isPageFinished;
    private boolean isLaidOut;
    private boolean isForcingFullScreen;
    private boolean isExpandingFromDefault;
    private boolean isExpandingPart2;
    private boolean isClosing;
    // used to force full-screen mode on expand and to restore original state on close
    private View titleBar;
    private boolean isFullScreen;
    private boolean isForceNotFullScreen;
    private int origTitleBarVisibility;
    private boolean isActionBarShowing;
    // This is the contents of mraid.js. We keep it around in case we need to inject it
    // into webViewPart2 (2nd part of 2-part expanded ad).
    private String mraidJs;
    private Handler handler;

    private boolean isTouched = false;
    private int closeTime;
    private static final int PROGRESS_TIMER_INTERVAL = 40;
    private int closeTimerPosition = 0;
    private boolean isSkippable;

    @SuppressLint("NewApi")
    private MRAIDVideoAddendumView(Context context, String baseUrl, String data, String[] supportedNativeFeatures, MRAIDVideoAddendumViewListener listener,
                                  MRAIDNativeFeatureListener nativeFeatureListener, boolean isInterstitial, int width, int height, boolean preload, boolean skippable) {
        super(context);

        if (supportedNativeFeatures == null) {
            supportedNativeFeatures = new String[] {};
        }
        this.context = context;
        this.baseUrl = baseUrl;
        this.isInterstitial = isInterstitial;
        this.preload = preload;
        this.isSkippable = skippable;
        state = STATE_LOADING;
        isViewable = false;
        useCustomClose = false;
        orientationProperties = new MRAIDOrientationProperties();
        resizeProperties = new MRAIDResizeProperties();
        nativeFeatureManager = new MRAIDNativeFeatureManager(context, new ArrayList<String>(Arrays.asList(supportedNativeFeatures)));

        this.listener = listener;
        this.nativeFeatureListener = nativeFeatureListener;

        displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        currentPosition = new Rect();
        defaultPosition = new Rect();
        maxSize = new Size();
        screenSize = new Size();

        if (context instanceof Activity) {
            originalRequestedOrientation = ((Activity) context).getRequestedOrientation();
        } else {
            originalRequestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
        }
        MRAIDLog.d(TAG, "originalRequestedOrientation " + getOrientationString(originalRequestedOrientation));

        // ignore scroll gestures
        gestureDetector = new GestureDetector(getContext(), new SimpleOnGestureListener() {
            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                return true;
            }
        });

        handler = new Handler(Looper.getMainLooper());

        mraidWebChromeClient = new MRAIDWebChromeClient();
        mraidWebViewClient = new MRAIDWebViewClient();

        webView = createWebView();

        currentWebView = webView;
        webView.setBackgroundColor(Color.parseColor("#7F000000"));

        addView(webView);

        String js = getMraidJs();
        data = MRAIDHtmlProcessor.processRawHtml(data);

        //MRAID resize
        WindowManager window = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = window.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);
        float density = displayMetrics.density;

        int screenWidth = size.x;
        int statusBarHeight = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = context.getResources().getDimensionPixelSize(resourceId);
        }
        int screenHeight;
        if (isInterstitial) {
            screenHeight = size.y - statusBarHeight;
        } else {
            screenHeight = Math.round(height * density);
        }
        float screenAspectRatio = ((float) screenWidth) / screenHeight;

        float imageAspectRatio = ((float) width) / height;

        int newHeight, newWidth;
        if(!Float.isNaN(imageAspectRatio)) {
            if (imageAspectRatio <= screenAspectRatio) {
                newHeight = screenHeight;
                newWidth = Math.round(newHeight * imageAspectRatio);
            } else {
                newWidth = screenWidth;
                newHeight = Math.round(newWidth / imageAspectRatio);
            }
        } else {
            newHeight = screenHeight;
            newWidth = screenWidth;
        }


        int webViewWidth = Math.round(newWidth / density);
        int webViewHeight = Math.round(newHeight / density);
        if (webViewWidth > width && webViewHeight > height && width != 0 && height != 0) {
            webViewWidth = width;
            webViewHeight = height;
        }

        String style = String.format("body, p {margin:0; padding:0} img {max-width:%dpx; max-height:%dpx} #appnext-interstitial {min-width:%dpx; min-height:%dpx;}"
                        + "img[width='%d'][height='%d'] {width: %dpx; height: %dpx} "
                        + ".appodeal-outer {display: table; position: absolute; height: 100%%; width: 100%%;}"
                        + ".appodeal-middle {display: table-cell; vertical-align: middle;}"
                        + ".appodeal-inner {margin-left: auto; margin-right: auto; width: %dpx; height: %dpx;}"
                        + ".ad_slug_table {margin-left: auto !important; margin-right: auto !important;} #ad[align='center'] {height: %dpx;} "
                        + "#voxelPlayer {position: relative !important;} "
                        + "#lsm_mobile_ad #wrapper, #lsm_overlay {position: relative !important;}",
                webViewWidth, webViewHeight, webViewWidth, webViewHeight, width, height, webViewWidth, webViewHeight, webViewWidth, webViewHeight, webViewHeight);
        mData = String.format("<style type='text/css'>%s</style><script type='application/javascript'>%s</script><div class='appodeal-outer'><div class='appodeal-middle'><div class='appodeal-inner'>%s</div></div></div>", style, js, data);

        if (this.preload) {
            webView.loadDataWithBaseURL(baseUrl, mData, "text/html", "UTF-8", null);
        } else {
            listener.mraidVideoAddendumViewLoaded(MRAIDVideoAddendumView.this);
        }
        MRAIDLog.d("log level = " + MRAIDLog.getLoggingLevel());
        if (MRAIDLog.getLoggingLevel() == LOG_LEVEL.verbose) {
            injectJavaScript(webView, "mraid.logLevel = mraid.LogLevelEnum.DEBUG;");
        } else if (MRAIDLog.getLoggingLevel() == LOG_LEVEL.debug) {
            injectJavaScript(webView, "mraid.logLevel = mraid.LogLevelEnum.DEBUG;");
        } else if (MRAIDLog.getLoggingLevel() == LOG_LEVEL.info) {
            injectJavaScript(webView, "mraid.logLevel = mraid.LogLevelEnum.INFO;");
        } else if (MRAIDLog.getLoggingLevel() == LOG_LEVEL.warning) {
            injectJavaScript(webView, "mraid.logLevel = mraid.LogLevelEnum.WARNING;");
        } else if (MRAIDLog.getLoggingLevel() == LOG_LEVEL.error) {
            injectJavaScript(webView, "mraid.logLevel = mraid.LogLevelEnum.ERROR;");
        } else if (MRAIDLog.getLoggingLevel() == LOG_LEVEL.none) {
            injectJavaScript(webView, "mraid.logLevel = mraid.LogLevelEnum.NONE;");
        }
    }

    private static String getVisibilityString(int visibility) {
        switch (visibility) {
            case View.GONE:
                return "GONE";
            case View.INVISIBLE:
                return "INVISIBLE";
            case View.VISIBLE:
                return "VISIBLE";
            default:
                return "UNKNOWN";
        }
    }

    /**
     * ***********************************************************************
     * Methods for forcing orientation.
     * ************************************************************************
     */

    private static String getOrientationString(int orientation) {
        switch (orientation) {
            case ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED:
                return "UNSPECIFIED";
            case ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE:
                return "LANDSCAPE";
            case ActivityInfo.SCREEN_ORIENTATION_PORTRAIT:
                return "PORTRAIT";
            default:
                return "UNKNOWN";
        }
    }

    public int getState() {
        return state;
    }

    @SuppressLint("SetJavaScriptEnabled")
    private WebView createWebView() {
        WebView wv = new WebView(context) {

            private static final String TAG = "MRAIDView-WebView";

            @SuppressWarnings("deprecation")
            @Override
            protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
                super.onLayout(changed, left, top, right, bottom);
                onLayoutWebView(this, changed, left, top, right, bottom);
            }

            @Override
            public void onConfigurationChanged(Configuration newConfig) {
                super.onConfigurationChanged(newConfig);
                MRAIDLog.d(TAG, "onConfigurationChanged " + (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT ? "portrait" : "landscape"));
                if (isInterstitial) {
                    ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                }
            }

            @Override
            protected void onVisibilityChanged(View changedView, int visibility) {
                super.onVisibilityChanged(changedView, visibility);
                MRAIDLog.d(TAG, "onVisibilityChanged " + getVisibilityString(visibility));
                if (isInterstitial) {
                    setViewable(visibility);
                }
            }

            @Override
            protected void onWindowVisibilityChanged(int visibility) {
                super.onWindowVisibilityChanged(visibility);
                int actualVisibility = getVisibility();
                MRAIDLog.d(TAG, "onWindowVisibilityChanged " + getVisibilityString(visibility) +
                        " (actual " + getVisibilityString(actualVisibility) + ")");
                if (isInterstitial) {
                    setViewable(actualVisibility);
                }
                if (visibility != View.VISIBLE) {
                    pauseWebView(this);
                } else {
                    resumeWebView(this);
                }
            }
        };

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        wv.setLayoutParams(params);

        wv.setScrollContainer(false);
        wv.setVerticalScrollBarEnabled(false);
        wv.setHorizontalScrollBarEnabled(false);
        wv.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        wv.setFocusableInTouchMode(false);
        wv.setOnTouchListener(new OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_UP:
                        isTouched = true;
                        if (!v.hasFocus()) {
                            v.requestFocus();
                        }
                        break;
                }
                return false;
            }
        });
        wv.getSettings().setJavaScriptEnabled(true);
        wv.getSettings().setDomStorageEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && isInterstitial) {
            wv.getSettings().setMediaPlaybackRequiresUserGesture(false);
        }
        wv.setWebChromeClient(mraidWebChromeClient);
        wv.setWebViewClient(mraidWebViewClient);

        //		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
        //		    if (0 != (context.getApplicationInfo().flags &= ApplicationInfo.FLAG_DEBUGGABLE)) {
        //		        WebView.setWebContentsDebuggingEnabled(true);
        //		    }
        //		}

        return wv;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (gestureDetector.onTouchEvent(event)) {
            event.setAction(MotionEvent.ACTION_CANCEL);
        }
        return super.onTouchEvent(event);
    }

    public void clearView() {
        if (webView != null) {
            webView.setWebChromeClient(null);
            webView.setWebViewClient(new WebViewClient() {
                @SuppressWarnings("deprecation")
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    MRAIDLog.d(TAG, "shouldOverrideUrlLoading: " + url);
                    return true;
                }

                @Override
                @TargetApi(24)
                public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                    MRAIDLog.d(TAG, "shouldOverrideUrlLoading: " + request.getUrl().toString());
                    return true;
                }
            });
            webView.loadUrl("about:blank");
        }
    }

    public void destroy() {
        context = null;
        nativeFeatureManager = null;
        if (webView != null) {
            try {
                webView.setWebChromeClient(null);
                webView.setWebViewClient(null);
                WebView temp = webView;
                webView = null;
                currentWebView = null;
                temp.destroy();
            } catch (Exception e) {
                MRAIDLog.e(e.getMessage());
            }
        }
    }

    ///////////////////////////////////////////////////////
    // These are methods in the MRAID API.
    ///////////////////////////////////////////////////////

    /**
     * ***********************************************************************
     * JavaScript --> native support
     *
     * These methods are (indirectly) called by JavaScript code. They provide
     * the means for JavaScript code to talk to native code
     * ************************************************************************
     */

    // This is the entry point to all the "actual" MRAID methods below.
    private void parseCommandUrl(String commandUrl) {
        MRAIDLog.d(TAG, "parseCommandUrl " + commandUrl);

        MRAIDParser parser = new MRAIDParser();
        Map<String, String> commandMap = parser.parseCommandUrl(commandUrl);

        String command = commandMap.get("command");

        final String[] commandsWithNoParam = {"close", "resize", "noFill"};

        final String[] commandsWithString = {"createCalendarEvent", "expand", "open", "playVideo", "storePicture", "useCustomClose",};

        final String[] commandsWithMap = {"setOrientationProperties", "setResizeProperties",};

        final String[] commandsVideoAddendumWithNoParam = {"AdStarted", "AdStopped", "AdSkipped", "AdVideoStart",
                "AdVideoFirstQuartile", "AdVideoMidpoint", "AdVideoThirdQuartile", "AdVideoComplete", "AdUserClose", "AdPaused", "AdPlaying",};

        final String[] commandsVideoAddendumWithString = {"AdSkippableStateChange", "AdClickThru", "AdLog", "AdError",};

        try {
            if (Arrays.asList(commandsWithNoParam).contains(command) || Arrays.asList(commandsVideoAddendumWithNoParam).contains(command)) {
                Method method = getClass().getDeclaredMethod(command);
                method.invoke(this);
            } else if (Arrays.asList(commandsWithString).contains(command) || Arrays.asList(commandsVideoAddendumWithString).contains(command)) {
                Method method = getClass().getDeclaredMethod(command, String.class);
                String key;
                if (command.equals("createCalendarEvent")) {
                    key = "eventJSON";
                } else if (command.equals("useCustomClose")) {
                    key = "useCustomClose";
                } else if (command.equals("AdLog") || command.equals("AdError")) {
                    key = "msg";
                } else if (command.equals("AdSkippableStateChange")) {
                    key = "state";
                } else {
                    key = "url";
                }
                String val = commandMap.get(key);
                method.invoke(this, val);
            } else if (Arrays.asList(commandsWithMap).contains(command)) {
                Method method = getClass().getDeclaredMethod(command, Map.class);
                method.invoke(this, commandMap);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void noFill() {
        MRAIDLog.d(TAG + "-JS callback", "noFill");
        if (listener != null) {
            listener.mraidVideoAddendumViewNoFill(this);
        }
    }

    private void close() {
        MRAIDLog.d(TAG + "-JS callback", "close");
        if (!isCloseClickable && !useCustomClose) {
            return;
        }
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (state == STATE_LOADING || (state == STATE_DEFAULT && !isInterstitial) || state == STATE_HIDDEN) {
                    // do nothing
                    return;
                } else if (state == STATE_DEFAULT || state == STATE_EXPANDED) {
                    closeFromExpanded();
                } else if (state == STATE_RESIZED) {
                    closeFromResized();
                }
            }
        });
    }

    @SuppressWarnings("unused")
    private void createCalendarEvent(String eventJSON) {
        MRAIDLog.d(TAG + "-JS callback", "createCalendarEvent " + eventJSON);
        if (nativeFeatureListener != null) {
            nativeFeatureListener.mraidNativeFeatureCreateCalendarEvent(eventJSON);
        }
    }

    // Note: This method is also used to present an interstitial ad.
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void expand(String url, final Activity activity) {
        MRAIDLog.d(TAG + "-JS callback", "expand " + (url != null ? url : "(1-part)"));

        // The only time it is valid to call expand on a banner ad is
        // when the ad is currently in either default or resized state.
        // The only time it is valid to (internally) call expand on an interstitial ad is
        // when the ad is currently in loading state.
        if ((isInterstitial && state != STATE_LOADING) || (!isInterstitial && state != STATE_DEFAULT && state != STATE_RESIZED)) {
            // do nothing
            return;
        }

        // 1-part expansion
        if (TextUtils.isEmpty(url)) {
            if (isInterstitial || state == STATE_DEFAULT) {
                if (webView.getParent() != null) {
                    ((ViewGroup) webView.getParent()).removeView(webView);
                } else {
                    removeView(webView);
                }
            } else if (state == STATE_RESIZED) {
                removeResizeView();
            }
            expandHelper(webView, activity);
            return;
        }

        // 2-part expansion

        // First, try to get the content of the second (expanded) part of the creative.

        try {
            url = URLDecoder.decode(url, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return;
        }

        // Check to see whether we've been given an absolute or relative URL.
        // If it's relative, prepend the base URL.
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = baseUrl + url;
        }

        final String finalUrl = url;

        // Go onto a background thread to read the content from the URL.
        (new Thread(new Runnable() {
            @Override
            public void run() {
                final String content = getStringFromUrl(finalUrl);
                if (!TextUtils.isEmpty(content)) {
                    // Get back onto the main thread to create and load a new WebView.
                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (state == STATE_RESIZED) {
                                removeResizeView();
                                addView(webView);
                            }
                            webView.setWebChromeClient(null);
                            webView.setWebViewClient(null);
                            webViewPart2 = createWebView();
                            webViewPart2.loadDataWithBaseURL(baseUrl, content, "text/html", "UTF-8", null);
                            currentWebView = webViewPart2;
                            isExpandingPart2 = true;
                            expandHelper(currentWebView, activity);
                        }
                    });
                } else {
                    MRAIDLog.e("Could not load part 2 expanded content for URL: " + finalUrl);
                }
            }
        }, "2-part-content")).start();
    }

    private void open(String url) {
        open(url, null);
    }

    private void open(String url, WebView view) {
//        try {
//            url = URLDecoder.decode(url, "UTF-8");
        if (isTouched) {
            MRAIDLog.d(TAG + "-JS callback", "open " + url);
            if (nativeFeatureListener != null) {
                if (url.startsWith("sms")) {
                    nativeFeatureListener.mraidNativeFeatureSendSms(url);
                } else if (url.startsWith("tel")) {
                    nativeFeatureListener.mraidNativeFeatureCallTel(url);
                } else {
                    nativeFeatureListener.mraidNativeFeatureOpenBrowser(url, view);
                    if (listener != null) {
                        listener.mraidVideoAddendumViewClickThru(this, url);
                    }
                }
            }
        } else {
            MRAIDLog.d(TAG, "mraid view not touched");
        }
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
    }

    @SuppressWarnings("unused")
    private void playVideo(String url) {
        try {
            url = URLDecoder.decode(url, "UTF-8");
            MRAIDLog.d(TAG + "-JS callback", "playVideo " + url);
            if (nativeFeatureListener != null) {
                nativeFeatureListener.mraidNativeFeaturePlayVideo(url);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    private void resize() {
        MRAIDLog.d(TAG + "-JS callback", "resize");

        // We need the cooperation of the app in order to do a resize.
        if (listener == null) {
            return;
        }
        boolean isResizeOK = listener.mraidVideoAddendumViewResize(this, resizeProperties.width, resizeProperties.height, resizeProperties.offsetX, resizeProperties.offsetY);
        if (!isResizeOK) {
            return;
        }

        state = STATE_RESIZED;

        if (resizedView == null) {
            resizedView = new RelativeLayout(context);
            removeAllViews();
            resizedView.addView(webView);
            addCloseRegion(resizedView);
            FrameLayout rootView = (FrameLayout) getRootView().findViewById(android.R.id.content);
            rootView.addView(resizedView);
        }
        setCloseRegionPosition(resizedView);
        setResizedViewSize();
        setResizedViewPosition();

        handler.post(new Runnable() {
            @Override
            public void run() {
                fireStateChangeEvent();
            }
        });
    }

    @SuppressWarnings("unused")
    private void setOrientationProperties(Map<String, String> properties) {
        boolean allowOrientationChange = Boolean.parseBoolean(properties.get("allowOrientationChange"));
        String forceOrientation = properties.get("forceOrientation");
        MRAIDLog.d(TAG + "-JS callback", "setOrientationProperties " + allowOrientationChange + " " + forceOrientation);
        if (orientationProperties.allowOrientationChange != allowOrientationChange || orientationProperties.forceOrientation != MRAIDOrientationProperties.forceOrientationFromString(forceOrientation)) {
            orientationProperties.allowOrientationChange = allowOrientationChange;
            orientationProperties.forceOrientation = MRAIDOrientationProperties.forceOrientationFromString(forceOrientation);
            if (isInterstitial || state == STATE_EXPANDED) {
                applyOrientationProperties();
            }
        }
    }

    @SuppressWarnings("unused")
    private void setResizeProperties(Map<String, String> properties) {
        int width = Integer.parseInt(properties.get("width"));
        int height = Integer.parseInt(properties.get("height"));
        int offsetX = Integer.parseInt(properties.get("offsetX"));
        int offsetY = Integer.parseInt(properties.get("offsetY"));
        String customClosePosition = properties.get("customClosePosition");
        boolean allowOffscreen = Boolean.parseBoolean(properties.get("allowOffscreen"));
        MRAIDLog.d(TAG + "-JS callback", "setResizeProperties " + width + " " + height + " " + offsetX + " " + offsetY + " " + customClosePosition + " " + allowOffscreen);
        resizeProperties.width = width;
        resizeProperties.height = height;
        resizeProperties.offsetX = offsetX;
        resizeProperties.offsetY = offsetY;
        resizeProperties.customClosePosition = MRAIDResizeProperties.customClosePositionFromString(customClosePosition);
        resizeProperties.allowOffscreen = allowOffscreen;
    }

    @SuppressWarnings("unused")
    private void storePicture(String url) {
        try {
            url = URLDecoder.decode(url, "UTF-8");
            MRAIDLog.d(TAG + "-JS callback", "storePicture " + url);
            if (nativeFeatureListener != null) {
                nativeFeatureListener.mraidNativeFeatureStorePicture(url);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    private void useCustomClose(String useCustomCloseString) {
        MRAIDLog.d(TAG + "-JS callback", "useCustomClose " + useCustomCloseString);
        boolean useCustomClose = Boolean.parseBoolean(useCustomCloseString);
        if (this.useCustomClose != useCustomClose) {
            this.useCustomClose = useCustomClose;
            if (useCustomClose) {
                removeDefaultCloseButton();
            } else {
                showDefaultCloseButton();
            }
        }
    }

    /**
     * ***********************************************************************
     * JavaScript --> native support helpers
     *
     * These methods are helper methods for the ones above.
     * ************************************************************************
     */

    private String getStringFromUrl(String url) {

        // Support second part from file system - mostly not used on real web creatives
        if (url.startsWith("file:///")) {
            return getStringFromFileUrl(url);
        }

        String content = null;
        InputStream is = null;
        try {
            HttpURLConnection conn = (HttpURLConnection) (new URL(url)).openConnection();
            int responseCode = conn.getResponseCode();
            MRAIDLog.d(TAG, "response code " + responseCode);
            if (responseCode == HttpURLConnection.HTTP_OK) {
                MRAIDLog.d(TAG, "getContentLength " + conn.getContentLength());
                is = conn.getInputStream();
                byte[] buf = new byte[1500];
                int count;
                StringBuilder sb = new StringBuilder();
                while ((count = is.read(buf)) != -1) {
                    String data = new String(buf, 0, count);
                    sb.append(data);
                }
                content = sb.toString();
                MRAIDLog.d(TAG, "getStringFromUrl ok, length=" + content.length());
            }
            conn.disconnect();
        } catch (IOException e) {
            MRAIDLog.e(TAG, "getStringFromUrl failed " + e.getLocalizedMessage());
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                // do nothing
            }
        }
        return content;
    }

    private String getStringFromFileUrl(String fileURL) {

        StringBuilder mLine = new StringBuilder("");
        String[] urlElements = fileURL.split("/");
        if (urlElements[3].equals("android_asset")) {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(context.getAssets().open(urlElements[4])));
                try {
                    // do reading, usually loop until end of file reading
                    String line = reader.readLine();
                    mLine.append(line);
                    while (line != null) {
                        line = reader.readLine();
                        mLine.append(line);
                    }
                } finally {
                    try {
                        reader.close();
                    } catch (Exception ignore) {
                    }
                }
            } catch (IOException e) {
                MRAIDLog.e("Error fetching file: " + e.getMessage());
            }

            return mLine.toString();
        } else {
            MRAIDLog.e("Unknown location to fetch file content");
        }

        return "";
    }

    protected void showAsInterstitial(Activity activity) {
        interstitialActivity = activity;
        expand(null, activity);
    }

    private void expandHelper(WebView webView, final Activity activity) {
        if (!isInterstitial) {
            state = STATE_EXPANDED;
        }
        // If this MRAIDView is an interstitial, we'll set the state to default and
        // fire the state change event after the view has been laid out.
        applyOrientationProperties();
        forceFullScreen();
        expandedView = new RelativeLayout(context);
        expandedView.addView(webView);
        if (isSkippable) {
            addCloseRegion(expandedView);
            setCloseRegionPosition(expandedView);
        }

        if (!preload) {
            webView.loadDataWithBaseURL(baseUrl, mData, "text/html", "UTF-8", null);
        }

        if (activity != null) {
            activity.addContentView(expandedView, new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        } else {
            ((Activity) context).addContentView(expandedView, new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        }
        isExpandingFromDefault = true;
        if (isInterstitial) {
            isLaidOut = true;
            state = STATE_DEFAULT;
            this.fireStateChangeEvent();
        }
    }

    private void setResizedViewSize() {
        MRAIDLog.d(TAG, "setResizedViewSize");
        int widthInDip = resizeProperties.width;
        int heightInDip = resizeProperties.height;
        Log.d(TAG, "setResizedViewSize " + widthInDip + "x" + heightInDip);
        int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, widthInDip, displayMetrics);
        int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, heightInDip, displayMetrics);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width, height);
        resizedView.setLayoutParams(params);
    }

    private void setResizedViewPosition() {
        MRAIDLog.d(TAG, "setResizedViewPosition");
        // resizedView could be null if it has been closed.
        if (resizedView == null) {
            return;
        }
        int widthInDip = resizeProperties.width;
        int heightInDip = resizeProperties.height;
        int offsetXInDip = resizeProperties.offsetX;
        int offsetYInDip = resizeProperties.offsetY;
        int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, widthInDip, displayMetrics);
        int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, heightInDip, displayMetrics);
        int offsetX = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, offsetXInDip, displayMetrics);
        int offsetY = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, offsetYInDip, displayMetrics);
        int x = defaultPosition.left + offsetX;
        int y = defaultPosition.top + offsetY;
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) resizedView.getLayoutParams();
        params.leftMargin = x;
        params.topMargin = y;
        resizedView.setLayoutParams(params);
        if (x != currentPosition.left || y != currentPosition.top || width != currentPosition.width() || height != currentPosition.height()) {
            currentPosition.left = x;
            currentPosition.top = y;
            currentPosition.right = x + width;
            currentPosition.bottom = y + height;
            setCurrentPosition();
        }
    }

    private void closeFromExpanded() {
        if (state == STATE_DEFAULT && isInterstitial) {
            state = STATE_HIDDEN;
            clearView();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    fireStateChangeEvent();
                    if (listener != null) {
                        listener.mraidVideoAddendumViewClose(MRAIDVideoAddendumView.this);
                    }
                }
            });
        } else if (state == STATE_EXPANDED || state == STATE_RESIZED) {
            state = STATE_DEFAULT;
        }
        isClosing = true;

        expandedView.removeAllViews();

        FrameLayout rootView = (FrameLayout) ((Activity) context).findViewById(android.R.id.content);
        rootView.removeView(expandedView);
        expandedView = null;
        closeRegion = null;

        handler.post(new Runnable() {
            @Override
            public void run() {
                restoreOriginalOrientation();
                restoreOriginalScreenState();
            }
        });
        if (webViewPart2 == null) {
            // close from 1-part expansion
            addView(webView);
        } else {
            // close from 2-part expansion
            webViewPart2.setWebChromeClient(null);
            webViewPart2.setWebViewClient(null);
            WebView temp = webViewPart2;
            webViewPart2 = null;
            temp.destroy();
            webView.setWebChromeClient(mraidWebChromeClient);
            webView.setWebViewClient(mraidWebViewClient);
            currentWebView = webView;
        }

        handler.post(new Runnable() {
            @Override
            public void run() {
                fireStateChangeEvent();
                if (listener != null) {
                    listener.mraidVideoAddendumViewClose(MRAIDVideoAddendumView.this);
                }
            }
        });
    }

    private void closeFromResized() {
        state = STATE_DEFAULT;
        isClosing = true;
        removeResizeView();
        addView(webView);
        handler.post(new Runnable() {
            @Override
            public void run() {
                fireStateChangeEvent();
                if (listener != null) {
                    listener.mraidVideoAddendumViewClose(MRAIDVideoAddendumView.this);
                }
            }
        });
    }

    private void removeResizeView() {
        resizedView.removeAllViews();
        FrameLayout rootView = (FrameLayout) ((Activity) context).findViewById(android.R.id.content);
        rootView.removeView(resizedView);
        resizedView = null;
        closeRegion = null;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void forceFullScreen() {
        MRAIDLog.d(TAG, "forceFullScreen");
        Activity activity = (Activity) context;

        // store away the original state
        int flags = activity.getWindow().getAttributes().flags;
        isFullScreen = ((flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) != 0);
        isForceNotFullScreen = ((flags & WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN) != 0);
        origTitleBarVisibility = -9;

        // First, see if the activity has an action bar.
        boolean hasActionBar = false;
        ActionBar actionBar = activity.getActionBar();
        if (actionBar != null) {
            hasActionBar = true;
            isActionBarShowing = actionBar.isShowing();
            actionBar.hide();
        }

        // If not, see if the app has a title bar
        if (!hasActionBar) {
            // http://stackoverflow.com/questions/6872376/how-to-hide-the-title-bar-through-code-in-android
            titleBar = null;
            try {
                titleBar = (View) activity.findViewById(android.R.id.title).getParent();
            } catch (NullPointerException npe) {
                // do nothing
            }
            if (titleBar != null) {
                origTitleBarVisibility = titleBar.getVisibility();
                titleBar.setVisibility(View.GONE);
            }
        }

        MRAIDLog.d(TAG, "isFullScreen " + isFullScreen);
        MRAIDLog.d(TAG, "isForceNotFullScreen " + isForceNotFullScreen);
        MRAIDLog.d(TAG, "isActionBarShowing " + isActionBarShowing);
        MRAIDLog.d(TAG, "origTitleBarVisibility " + getVisibilityString(origTitleBarVisibility));

        // force fullscreen mode
        //((Activity) context).getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //((Activity) context).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);

        isForcingFullScreen = !isFullScreen;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void restoreOriginalScreenState() {
        if (context != null && context instanceof Activity) {
            Activity activity = (Activity) context;
            if (!isFullScreen) {
                activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            }
            if (isForceNotFullScreen) {
                activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            }
            if (isActionBarShowing) {
                ActionBar actionBar = activity.getActionBar();
                if (actionBar != null) {
                    actionBar.show();
                }
            } else if (titleBar != null) {
                titleBar.setVisibility(origTitleBarVisibility);
            }
        }
    }

    private void addCloseRegion(View view) {
        // The input parameter should be either expandedView or resizedView.

        closeRegion = new CircleCountdownView(context);
        closeRegion.setBackgroundColor(Color.TRANSPARENT);
        closeRegion.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                close();
            }
        });

        // The default close button is shown only on expanded banners and interstitials,
        // but not on resized banners.
        if (view == expandedView && !useCustomClose) {
            showDefaultCloseButton();
        }

        ((ViewGroup) view).addView(closeRegion);
    }

    private void showDefaultCloseButton() {
        if (closeRegion != null) {
            isCloseClickable = false;
            final int delay = (closeTime == 0 ? 3 : closeTime) * 1000;
            closeTimerPosition = 0;
            final Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (closeRegion != null) {
                        closeTimerPosition = closeTimerPosition + PROGRESS_TIMER_INTERVAL;
                        closeRegion.changePercentage(100 * closeTimerPosition / delay, (int) Math.ceil((double) (delay - closeTimerPosition) / 1000));
                        if (closeTimerPosition >= delay) {
                            closeRegion.setImage(org.nexage.sourcekit.util.Assets.getBitmapFromBase64(org.nexage.sourcekit.util.Assets.close));
                            isCloseClickable = true;
                        } else {
                            handler.postDelayed(this, PROGRESS_TIMER_INTERVAL);
                        }
                    }
                }
            }, PROGRESS_TIMER_INTERVAL);
        }
    }

    private void removeDefaultCloseButton() {
        if (closeRegion != null) {
            closeRegion.setVisibility(INVISIBLE);
            closeRegion.setClickable(false);
            closeTimerPosition = 100000;
        }
    }

    private void setCloseRegionPosition(View view) {
        // The input parameter should be either expandedView or resizedView.

        int size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, CLOSE_REGION_SIZE, displayMetrics);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(size, size);

        // The close region on expanded banners and interstitials is always in the top right corner.
        // Its position on resized banners is determined by the customClosePosition property of the
        // resizeProperties.
        if (view == expandedView) {
            params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        } else if (view == resizedView) {

            switch (resizeProperties.customClosePosition) {
                case MRAIDResizeProperties.CUSTOM_CLOSE_POSITION_TOP_LEFT:
                case MRAIDResizeProperties.CUSTOM_CLOSE_POSITION_BOTTOM_LEFT:
                    params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                    break;
                case MRAIDResizeProperties.CUSTOM_CLOSE_POSITION_TOP_CENTER:
                case MRAIDResizeProperties.CUSTOM_CLOSE_POSITION_CENTER:
                case MRAIDResizeProperties.CUSTOM_CLOSE_POSITION_BOTTOM_CENTER:
                    params.addRule(RelativeLayout.CENTER_HORIZONTAL);
                    break;
                case MRAIDResizeProperties.CUSTOM_CLOSE_POSITION_TOP_RIGHT:
                case MRAIDResizeProperties.CUSTOM_CLOSE_POSITION_BOTTOM_RIGHT:
                    params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                    break;
            }

            switch (resizeProperties.customClosePosition) {
                case MRAIDResizeProperties.CUSTOM_CLOSE_POSITION_TOP_LEFT:
                case MRAIDResizeProperties.CUSTOM_CLOSE_POSITION_TOP_CENTER:
                case MRAIDResizeProperties.CUSTOM_CLOSE_POSITION_TOP_RIGHT:
                    params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                    break;
                case MRAIDResizeProperties.CUSTOM_CLOSE_POSITION_CENTER:
                    params.addRule(RelativeLayout.CENTER_VERTICAL);
                    break;
                case MRAIDResizeProperties.CUSTOM_CLOSE_POSITION_BOTTOM_LEFT:
                case MRAIDResizeProperties.CUSTOM_CLOSE_POSITION_BOTTOM_CENTER:
                case MRAIDResizeProperties.CUSTOM_CLOSE_POSITION_BOTTOM_RIGHT:
                    params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                    break;
            }
        }

        closeRegion.setLayoutParams(params);
    }

    /**
     * ***********************************************************************
     * native --> JavaScript support
     *
     * These methods provide the means for JavaScript code to talk to native
     * code.
     * ************************************************************************
     */
    private String getMraidJs() {
        if (TextUtils.isEmpty(mraidJs)) {
            String str = Assets.mraidVAJS;
            byte[] mraidjsBytes = Base64.decode(str, Base64.DEFAULT);
            return new String(mraidjsBytes);
        } else {
            return mraidJs;
        }
    }

    @SuppressLint("NewApi")
    private void injectJavaScript (String js) {
        injectJavaScript(currentWebView, js);
    }

    @SuppressLint("NewApi")
    private void injectJavaScript(WebView webView, String js) {
        if (!TextUtils.isEmpty(js) && webView != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                try {
                    MRAIDLog.d(TAG, "evaluating js: " + js);
                    webView.evaluateJavascript(js, new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String value) {
                            //MRAIDLog.d(TAG, "evaluate js complete: "+value);
                        }
                    });
                } catch (Exception e) {
                    MRAIDLog.e(e.getMessage());
                    MRAIDLog.d(TAG, "loading url: " + js);
                    webView.loadUrl("javascript:" + js);
                }
            } else {
                MRAIDLog.d(TAG, "loading url: " + js);
                webView.loadUrl("javascript:" + js);
            }
        }
    }

    // convenience methods
    private void fireReadyEvent() {
        MRAIDLog.d(TAG, "fireReadyEvent");
        injectJavaScript("mraid.fireReadyEvent();");
    }

    // We don't need to explicitly call fireSizeChangeEvent because it's taken care
    // of for us in the mraid.setCurrentPosition method in mraid.js.

    @SuppressLint("DefaultLocale")
    private void fireStateChangeEvent() {
        MRAIDLog.d(TAG, "fireStateChangeEvent");
        String[] stateArray = {"loading", "default", "expanded", "resized", "hidden"};
        injectJavaScript("mraid.fireStateChangeEvent('" + stateArray[state] + "');");
    }

    private void fireViewableChangeEvent() {
        MRAIDLog.d(TAG, "fireViewableChangeEvent");
        injectJavaScript("mraid.fireViewableChangeEvent(" + isViewable + ");");
    }

    private int px2dip(int pixels) {
        return pixels * DisplayMetrics.DENSITY_DEFAULT / displayMetrics.densityDpi;
        // return pixels;
    }

    private void setCurrentPosition() {
        int x = currentPosition.left;
        int y = currentPosition.top;
        int width = currentPosition.width();
        int height = currentPosition.height();
        MRAIDLog.d(TAG, "setCurrentPosition [" + x + "," + y + "] (" + width + "x" + height + ")");
        injectJavaScript("mraid.setCurrentPosition(" + px2dip(x) + "," + px2dip(y) + "," + px2dip(width) + "," + px2dip(height) + ");");
    }

    private void setDefaultPosition() {
        int x = defaultPosition.left;
        int y = defaultPosition.top;
        int width = defaultPosition.width();
        int height = defaultPosition.height();
        MRAIDLog.d(TAG, "setDefaultPosition [" + x + "," + y + "] (" + width + "x" + height + ")");
        injectJavaScript("mraid.setDefaultPosition(" + px2dip(x) + "," + px2dip(y) + "," + px2dip(width) + "," + px2dip(height) + ");");
    }

    private void setMaxSize() {
        MRAIDLog.d(TAG, "setMaxSize");
        int width = maxSize.width;
        int height = maxSize.height;
        MRAIDLog.d(TAG, "setMaxSize " + width + "x" + height);
        injectJavaScript("mraid.setMaxSize(" + px2dip(width) + "," + px2dip(height) + ");");
    }

    private void setScreenSize() {
        MRAIDLog.d(TAG, "setScreenSize");
        int width = screenSize.width;
        int height = screenSize.height;
        MRAIDLog.d(TAG, "setScreenSize " + width + "x" + height);
        injectJavaScript("mraid.setScreenSize(" + px2dip(width) + "," + px2dip(height) + ");");
    }

    private void setSupportedServices() {
        MRAIDLog.d(TAG, "setSupportedServices");
        injectJavaScript("mraid.setSupports(mraid.SUPPORTED_FEATURES.CALENDAR, " + nativeFeatureManager.isCalendarSupported() + ");");
        injectJavaScript("mraid.setSupports(mraid.SUPPORTED_FEATURES.INLINEVIDEO, " + nativeFeatureManager.isInlineVideoSupported() + ");");
        injectJavaScript("mraid.setSupports(mraid.SUPPORTED_FEATURES.SMS, " + nativeFeatureManager.isSmsSupported() + ");");
        injectJavaScript("mraid.setSupports(mraid.SUPPORTED_FEATURES.STOREPICTURE, " + nativeFeatureManager.isStorePictureSupported() + ");");
        injectJavaScript("mraid.setSupports(mraid.SUPPORTED_FEATURES.TEL, " + nativeFeatureManager.isTelSupported() + ");");
    }

    private void pauseWebView(WebView webView) {
        MRAIDLog.d(TAG, "pauseWebView " + webView.toString());
        try {
            // Stop any video/animation that may be running in the WebView.
            // Otherwise, it will keep playing in the background.
            webView.onPause();
        } catch (Exception e) {
            MRAIDLog.e(e.getMessage());
        }
    }

    private void resumeWebView(WebView webView) {
        MRAIDLog.d(TAG, "resumeWebView " + webView.toString());
        try {
            webView.onResume();
        } catch (Exception e) {
            MRAIDLog.e(e.getMessage());
        }
    }

    /**
     * ***********************************************************************
     * Methods for responding to changes of size and position.
     * ************************************************************************
     */

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        MRAIDLog.d(TAG, "onConfigurationChanged " + (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT ? "portrait" : "landscape"));
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
    }

    @Override
    protected void onAttachedToWindow() {
        MRAIDLog.d(TAG, "onAttachedToWindow");
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        MRAIDLog.d(TAG, "onDetachedFromWindow");
        super.onDetachedFromWindow();
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        MRAIDLog.d(TAG, "onVisibilityChanged " + getVisibilityString(visibility));
        setViewable(visibility);
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        int actualVisibility = getVisibility();
        MRAIDLog.d(TAG, "onWindowVisibilityChanged " + getVisibilityString(visibility) +
                " (actual " + getVisibilityString(actualVisibility) + ")");
        setViewable(actualVisibility);
    }

    private void setViewable(int visibility) {
        boolean isCurrentlyViewable = (visibility == View.VISIBLE);
        if (isCurrentlyViewable != isViewable) {
            isViewable = isCurrentlyViewable;
            if (isPageFinished && isLaidOut) {
                fireViewableChangeEvent();
            }
        }
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        MRAIDLog.w(TAG, "onLayout (" + state + ") " +
                changed + " " + left + " " + top + " " + right + " " + bottom);
        if (isForcingFullScreen) {
            MRAIDLog.d(TAG, "onLayout ignored");
            return;
        }
        if (state == STATE_EXPANDED || state == STATE_RESIZED) {
            calculateScreenSize();
            calculateMaxSize();
        }
        if (isClosing) {
            isClosing = false;
            currentPosition = new Rect(defaultPosition);
            setCurrentPosition();
        } else {
            calculatePosition(false);
        }
        if (state == STATE_RESIZED && changed) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    setResizedViewPosition();
                }
            });
        }
        isLaidOut = true;
        if (state == STATE_LOADING && isPageFinished && !isInterstitial) {
            state = STATE_DEFAULT;
            fireStateChangeEvent();
            fireReadyEvent();
            if (isViewable) {
                fireViewableChangeEvent();
            }
        }
    }

    private void onLayoutWebView(WebView wv, boolean changed, int left, int top, int right, int bottom) {
        boolean isCurrent = (wv == currentWebView);
        MRAIDLog.w(TAG, "onLayoutWebView " + (wv == webView ? "1 " : "2 ") + isCurrent + " (" + state + ") " +
                changed + " " + left + " " + top + " " + right + " " + bottom);
        if (!isCurrent) {
            MRAIDLog.d(TAG, "onLayoutWebView ignored, not current");
            return;
        }
        if (isForcingFullScreen) {
            MRAIDLog.d(TAG, "onLayoutWebView ignored, isForcingFullScreen");
            isForcingFullScreen = false;
            return;
        }
        if (state == STATE_LOADING || state == STATE_DEFAULT) {
            calculateScreenSize();
            calculateMaxSize();
        }

        // If closing from expanded state, just set currentPosition to default position in onLayout above.
        if (!isClosing) {
            calculatePosition(true);
            if (isInterstitial) {
                // For interstitials, the default position is always the current position
                if (!defaultPosition.equals(currentPosition)) {
                    defaultPosition = new Rect(currentPosition);
                    setDefaultPosition();
                }
            }
        }

        if (isExpandingFromDefault) {
            isExpandingFromDefault = false;
            if (isInterstitial) {
                state = STATE_DEFAULT;
                isLaidOut = true;
            }
            if (!isExpandingPart2) {
                MRAIDLog.d(TAG, "calling fireStateChangeEvent 1");
                fireStateChangeEvent();
            }
            if (isInterstitial) {
                fireReadyEvent();
                if (isViewable) {
                    fireViewableChangeEvent();
                }
            }
            if (listener != null) {
                listener.mraidVideoAddendumViewExpand(this);
            }
        }
    }

    private void calculateScreenSize() {
        int orientation = getResources().getConfiguration().orientation;
        boolean isPortrait = (orientation == Configuration.ORIENTATION_PORTRAIT);
        MRAIDLog.d(TAG, "calculateScreenSize orientation " + (isPortrait ? "portrait" : "landscape"));
        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;
        MRAIDLog.d(TAG, "calculateScreenSize screen size " + width + "x" + height);
        if (width != screenSize.width || height != screenSize.height) {
            screenSize.width = width;
            screenSize.height = height;
            if (isPageFinished) {
                setScreenSize();
            }
        }
    }

    private void calculateMaxSize() {
        int width, height;
        Rect frame = new Rect();
        Window window = ((Activity) context).getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(frame);
        MRAIDLog.d(TAG, "calculateMaxSize frame [" + frame.left + "," + frame.top + "][" + frame.right + "," + frame.bottom + "] (" +
                frame.width() + "x" + frame.height() + ")");
        int statusHeight = frame.top;
        View contentView =  window.findViewById(Window.ID_ANDROID_CONTENT);
        contentViewTop = 0;
        if (contentView != null) {
            contentViewTop = contentView.getTop();
            int titleHeight = contentViewTop - statusHeight;
            MRAIDLog.d(TAG, "calculateMaxSize statusHeight " + statusHeight);
            MRAIDLog.d(TAG, "calculateMaxSize titleHeight " + titleHeight);
            MRAIDLog.d(TAG, "calculateMaxSize contentViewTop " + contentViewTop);
        }
        width = frame.width();
        height = screenSize.height - contentViewTop;
        MRAIDLog.d(TAG, "calculateMaxSize max size " + width + "x" + height);
        if (width != maxSize.width || height != maxSize.height) {
            maxSize.width = width;
            maxSize.height = height;
            if (isPageFinished) {
                setMaxSize();
            }
        }
    }

    private void calculatePosition(boolean isCurrentWebView) {
        int x, y, width, height;
        int[] location = new int[2];

        View view = isCurrentWebView ? currentWebView : this;
        String name = (isCurrentWebView ? "current" : "default");

        // This is the default location regardless of the state of the MRAIDView.
        view.getLocationOnScreen(location);
        x = location[0];
        y = location[1];
        MRAIDLog.d(TAG, "calculatePosition " + name + " locationOnScreen [" + x + "," + y + "]");
        MRAIDLog.d(TAG, "calculatePosition " + name + " contentViewTop " + contentViewTop);
        y -= contentViewTop;
        width = view.getWidth();
        height = view.getHeight();

        MRAIDLog.d(TAG, "calculatePosition " + name + " position [" + x + "," + y + "] (" + width + "x" + height + ")");

        Rect position = isCurrentWebView ? currentPosition : defaultPosition;

        if (x != position.left || y != position.top || width != position.width() || height != position.height()) {
            if (isCurrentWebView) {
                currentPosition = new Rect(x, y, x + width, y + height);
            } else {
                defaultPosition = new Rect(x, y, x + width, y + height);
            }
            if (isPageFinished) {
                if (isCurrentWebView) {
                    setCurrentPosition();
                } else {
                    setDefaultPosition();
                }
            }
        }
    }

    private void applyOrientationProperties() {
        MRAIDLog.d(TAG, "applyOrientationProperties " +
                orientationProperties.allowOrientationChange + " " + orientationProperties.forceOrientationString());
        if (interstitialActivity != null) {

            int currentOrientation = getResources().getConfiguration().orientation;
            boolean isCurrentPortrait = (currentOrientation == Configuration.ORIENTATION_PORTRAIT);
            MRAIDLog.d(TAG, "currentOrientation " + (isCurrentPortrait ? "portrait" : "landscape"));

            int orientation;
            if (orientationProperties.forceOrientation == MRAIDOrientationProperties.FORCE_ORIENTATION_PORTRAIT) {
                orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
            } else if (orientationProperties.forceOrientation == MRAIDOrientationProperties.FORCE_ORIENTATION_LANDSCAPE) {
                orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
            } else {
                // orientationProperties.forceOrientation == MRAIDOrientationProperties.FORCE_ORIENTATION_NONE
                if (orientationProperties.allowOrientationChange) {
                    orientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
                } else {
                    // orientationProperties.allowOrientationChange == false
                    // lock the current orientation
                    orientation = (isCurrentPortrait ? ActivityInfo.SCREEN_ORIENTATION_PORTRAIT : ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                }
            }
            interstitialActivity.setRequestedOrientation(orientation);
        }
    }

    private void restoreOriginalOrientation() {
        MRAIDLog.d(TAG, "restoreOriginalOrientation");
        if (interstitialActivity != null) {
            int currentRequestedOrientation = interstitialActivity.getRequestedOrientation();
            if (currentRequestedOrientation != originalRequestedOrientation) {
                interstitialActivity.setRequestedOrientation(originalRequestedOrientation);
            }
        }
    }

    private final class Size {

        public int width;
        public int height;
    }

    /**
     * ***********************************************************************
     * WebChromeClient and WebViewClient
     * ************************************************************************
     */

    private class MRAIDWebChromeClient extends WebChromeClient {

        @Override
        public boolean onConsoleMessage(ConsoleMessage cm) {
            if (cm == null || cm.message() == null) {
                return false;
            }
            if (!cm.message().contains("Uncaught ReferenceError")) {
                MRAIDLog.i("JS console", cm.message() + (cm.sourceId() == null ? "" : " at " + cm.sourceId()) + ":" + cm.lineNumber());
            }
            return true;
        }

        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
        	MRAIDLog.d("JS alert", message);
        	return handlePopups(result);
        }

        @Override
        public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
            MRAIDLog.d("JS confirm", message);
            return handlePopups(result);
        }

        @Override
        public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
            MRAIDLog.d("JS prompt", message);
            return handlePopups(result);
        }

        private boolean handlePopups(JsResult result) {
                        /*
			 * if (NexageAdManager.areJavascriptPromptsAndAlertsAllowed()) {
			 * NexageAdManager.setIsPopupDisplayed(true); return false; } else {
			 *
			 * result.cancel(); return true; }
			 */
            result.cancel();
            return true;
        }

    }

    private class MRAIDWebViewClient extends WebViewClient {

        @Override
        public void onPageFinished(WebView view, String url) {
            MRAIDLog.d(TAG, "onPageFinished: " + url);
            super.onPageFinished(view, url);
            if (state == STATE_LOADING) {
                isPageFinished = true;
                injectJavaScript("mraid.setPlacementType('" + (isInterstitial ? "interstitial" : "inline") + "');");
                setSupportedServices();
                if (isLaidOut) {
                    setScreenSize();
                    setMaxSize();
                    setCurrentPosition();
                    setDefaultPosition();
                    if (isInterstitial) {
                        showAsInterstitial(null);
                    } else {
                        state = STATE_DEFAULT;
                        fireStateChangeEvent();
                        fireReadyEvent();
                        if (isViewable) {
                            fireViewableChangeEvent();
                        }
                    }
                }
                if (listener != null && !url.equals("data:text/html,<html></html>") && preload) {
                    listener.mraidVideoAddendumViewLoaded(MRAIDVideoAddendumView.this);
                }
            }
            if (isExpandingPart2) {
                isExpandingPart2 = false;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        injectJavaScript("mraid.setPlacementType('" + (isInterstitial ? "interstitial" : "inline") + "');");
                        setSupportedServices();
                        setScreenSize();
                        setDefaultPosition();
                        MRAIDLog.d(TAG, "calling fireStateChangeEvent 2");
                        fireStateChangeEvent();
                        fireReadyEvent();
                        if (isViewable) {
                            fireViewableChangeEvent();
                        }
                    }
                });
            }
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            MRAIDLog.d(TAG, "onReceivedError: " + description);
            super.onReceivedError(view, errorCode, description, failingUrl);
        }

        @SuppressWarnings("deprecation")
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            MRAIDLog.d(TAG, "shouldOverrideUrlLoading: " + url);
            if (url.startsWith("mraid://")) {
                parseCommandUrl(url);
                return true;
            } else {
                open(url, view);
                return true;
            }
        }

        @Override
        @TargetApi(24)
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            if (request.hasGesture()) {
                isTouched = true;
            }
            return shouldOverrideUrlLoading(view, request.getUrl().toString());
        }
    }

    public void show() {
        if (!preload && !isShown) {
            isShown = true;
            webView.loadDataWithBaseURL(baseUrl, mData, "text/html", "UTF-8", null);
        }
    }


    //VIDEO ADDENDUM METHODS

    private void AdStarted() {
        MRAIDLog.d(TAG + "-JS callback", "AdStarted");
        if (listener != null) {
            listener.mraidVideoAddendumViewStarted(this);
        }
    }

    private void AdStopped() {
        MRAIDLog.d(TAG + "-JS callback", "AdStopped");
        if (listener != null) {
            listener.mraidVideoAddendumViewStopped(this);
        }
    }

    private void AdSkipped() {
        MRAIDLog.d(TAG + "-JS callback", "AdSkipped");
        if (listener != null) {
            listener.mraidVideoAddendumViewSkipped(this);
        }
    }

    private void AdSkippableStateChange(String state) {
        MRAIDLog.d(TAG + "-JS callback", "AdSkippableStateChange");
        isSkippable = Boolean.valueOf(state);
        if (listener != null) {
            listener.mraidVideoAddendumViewSkippableStateChange(this, isSkippable);
        }
    }

    private void AdVideoStart() {
        MRAIDLog.d(TAG + "-JS callback", "AdVideoStart");
        if (listener != null) {
            listener.mraidVideoAddendumViewVideoStart(this);
        }
    }

    private void AdVideoFirstQuartile() {
        MRAIDLog.d(TAG + "-JS callback", "AdVideoFirstQuartile");
        if (listener != null) {
            listener.mraidVideoAddendumViewFirstQuartile(this);
        }
    }

    private void AdVideoMidpoint() {
        MRAIDLog.d(TAG + "-JS callback", "AdVideoMidpoint");
        if (listener != null) {
            listener.mraidVideoAddendumViewMidpoint(this);
        }
    }

    private void AdVideoThirdQuartile() {
        MRAIDLog.d(TAG + "-JS callback", "AdVideoThirdQuartile");
        if (listener != null) {
            listener.mraidVideoAddendumViewThirdQuartile(this);
        }
    }

    private void AdVideoComplete() {
        MRAIDLog.d(TAG + "-JS callback", "AdVideoComplete");
        if (listener != null) {
            listener.mraidVideoAddendumViewComplete(this);
        }
    }

    private void AdUserClose() {
        MRAIDLog.d(TAG + "-JS callback", "AdUserClose");
        if (listener != null) {
            listener.mraidVideoAddendumViewUserClose(this);
        }
    }

    private void AdPaused() {
        MRAIDLog.d(TAG + "-JS callback", "AdPaused");
        if (listener != null) {
            listener.mraidVideoAddendumViewPaused(this);
        }
    }

    private void AdPlaying() {
        MRAIDLog.d(TAG + "-JS callback", "AdPlaying");
        if (listener != null) {
            listener.mraidVideoAddendumViewPlaying(this);
        }
    }

    private void AdClickThru(String url) {
        MRAIDLog.d(TAG + "-JS callback", "AdClickThru");
        if (listener != null) {
            listener.mraidVideoAddendumViewClickThru(this, url);
        }
    }

    private void AdLog(String msg) {
        MRAIDLog.d(TAG + "-JS callback", "AdLog");
        if (listener != null) {
            listener.mraidVideoAddendumViewLog(this, msg);
        }
    }

    private void AdError(String msg) {
        MRAIDLog.d(TAG + "-JS callback", "AdError");
        if (listener != null) {
            listener.mraidVideoAddendumViewError(this, msg);
        }
    }


    public static class Builder {
        private Context context;
        private String baseUrl;
        private String data;
        private String[] supportedNativeFeatures;
        private MRAIDVideoAddendumViewListener listener;
        private MRAIDNativeFeatureListener nativeFeatureListener;
        private boolean isInterstitial;
        private int width;
        private int height;
        private boolean preload = true;
        private boolean skippable = true;

        public Builder() {

        }

        public Builder setContext(Context context) {
            this.context = context;
            return this;
        }

        public Builder setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        public Builder setData(String data) {
            this.data = data;
            return this;
        }

        public Builder setSupportedNativeFeatures(String[] supportedNativeFeatures) {
            this.supportedNativeFeatures = supportedNativeFeatures;
            return this;
        }

        public Builder setListener(MRAIDVideoAddendumViewListener listener) {
            this.listener = listener;
            return this;
        }

        public Builder setNativeFeatureListener(MRAIDNativeFeatureListener nativeFeatureListener) {
            this.nativeFeatureListener = nativeFeatureListener;
            return this;
        }

        public Builder setIsInterstitial(boolean isInterstitial) {
            this.isInterstitial = isInterstitial;
            return this;
        }

        public Builder setWidth(int width) {
            this.width = width;
            return this;
        }

        public Builder setHeight(int height) {
            this.height = height;
            return this;
        }

        public Builder setPreload(boolean preload) {
            this.preload = preload;
            return this;
        }

        public Builder setSkippable(boolean skippable) {
            this.skippable = skippable;
            return this;
        }

        public MRAIDVideoAddendumView build() {
            return new MRAIDVideoAddendumView(context, baseUrl, data, supportedNativeFeatures, listener,
                    nativeFeatureListener, isInterstitial, width, height, preload, skippable);
        }
    }
}

