package org.nexage.sourcekit.vast.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.nexage.sourcekit.util.VASTLog;
import org.nexage.sourcekit.util.Video;
import org.nexage.sourcekit.vast.VASTPlayer;
import org.nexage.sourcekit.vast.model.VASTModel;

import java.util.Timer;
import java.util.TimerTask;

import static android.view.View.VISIBLE;


public class VPAIDActivity extends Activity {
    private static String TAG = "VPAIDActivity";
    private Context context;
    private String url;
    private RelativeLayout mRootLayout;
    private WebView mWebView;
    private ProgressBar mProgressBar;
    private Video.Type mType;
    private TextView mSkipText;
    private int mSkipTime = 5;
    private Timer mSkipTimer;
    private static final long SKIP_TIMER_INTERVAL = 1000;
    private int mScreenWidth;
    private int mScreenHeight;
    private boolean isClosed = false;
    VASTPlayer.VASTPlayerListener mListener;
    private VASTModel mVastModel = null;
    private boolean canSkip = false;
    private String html;
    private boolean videoStarted = false;
    private boolean mStarted = false;
    private boolean mPaused = false;
    private final int LOADER_TIMEOUT_MS = 10000;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int currentOrientation = this.getResources().getConfiguration().orientation;

        if (currentOrientation != Configuration.ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        } else {
            hideTitleStatusBars();
            context = this;
            Intent i = getIntent();
            url = i.getStringExtra("android.net.url");
            if(i.hasExtra("com.nexage.android.vast.player.type")) {
                mType = (Video.Type) i.getExtras().get("com.nexage.android.vast.player.type");
            } else {
                VASTLog.e(TAG, "video type undefined.");
                mType = Video.Type.NON_REWARDED;
            }
            mListener = VASTPlayer.listener;
            mVastModel = (VASTModel) i
                    .getSerializableExtra("com.nexage.android.vast.player.vastModel");
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
            screenHeight = size.y - statusBarHeight;
            mScreenWidth = Math.round(screenWidth / density);
            mScreenHeight = Math.round(screenHeight / density);
            if (mVastModel != null) {
                mSkipTime = mSkipTime > mVastModel.getSkipoffset() ? mSkipTime : mVastModel.getSkipoffset();
            }
            createUi();
        }
        if (mListener != null) {
            mListener.vastShown();
        }
    }

    private void createUi() {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);

        this.createRootLayout(params);
        this.createWebView(params);
        this.createProgressBar();
        this.createSkipText();

        this.setContentView(mRootLayout);

        html = "<html>\n" +
                "<head>\n" +
                "    <title>IMA HTML5 Simple Demo</title>\n" +
                "    <script src=\"https://s3.amazonaws.com/appodeal-externallibs/android/ima3.js\"></script>\n" +
                "    <style>\n" +
                "        #mainContainer {\n" +
                "            position: relative;\n" +
                "            width: " + mScreenWidth + ";\n" +
                "            height: " + mScreenHeight + "px;\n" +
                "        }\n" +
                "\n" +
                "        #content, #adContainer {\n" +
                "            position: absolute;\n" +
                "            top: 0px;\n" +
                "            left: 0px;\n" +
                "            width: " + mScreenWidth + ";\n" +
                "            height: " + mScreenHeight + ";\n" +
                "        }\n" +
                "\n" +
                "        #contentElement {\n" +
                "            width: " + mScreenWidth + ";\n" +
                "            height: " + mScreenHeight + ";\n" +
                "            overflow: hidden;\n" +
                "        }\n" +
                "    </style>\n" +
                "</head>\n" +
                "\n" +
                "<body style='margin:0;padding:0;background-color: black; position: fixed;'>\n" +
                "<div id=\"mainContainer\">\n" +
                "    <div id=\"content\">\n" +
                "        <video id=\"contentElement\" poster=\"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNg+A8AAQIBANEay48AAAAASUVORK5CYII=\">\n" +
                "        </video>\n" +
                "    </div>\n" +
                "    <div id=\"adContainer\"></div>\n" +
                "</div>\n" +
                "<script type=\"text/javascript\">\n" +
                "        var videoContent = document.getElementById('contentElement');\n" +
                "        videoContent.play();\n" +
                "\n" +
                "        var videoContent = document.getElementById('contentElement');\n" +
                "        var adDisplayContainer = new google.ima.AdDisplayContainer(\n" +
                "                        document.getElementById('adContainer'),\n" +
                "                        videoContent);\n" +
                "        // Must be done as the result of a user action on mobile\n" +
                "        adDisplayContainer.initialize();\n" +
                "\n" +
                "        // Re-use this AdsLoader instance for the entire lifecycle of your page.\n" +
                "        var adsLoader = new google.ima.AdsLoader(adDisplayContainer);\n" +
                "        adsLoader.getSettings().setVpaidMode(google.ima.ImaSdkSettings.VpaidMode.INSECURE);\n" +
                "\n" +
                "        // Add event listeners\n" +
                "        adsLoader.addEventListener(\n" +
                "                google.ima.AdsManagerLoadedEvent.Type.ADS_MANAGER_LOADED,\n" +
                "                onAdsManagerLoaded,\n" +
                "                false);\n" +
                "        adsLoader.addEventListener(\n" +
                "                google.ima.AdErrorEvent.Type.AD_ERROR,\n" +
                "                onAdError,\n" +
                "                false);\n" +
                "\n" +
                "        function onAdError(adErrorEvent) {\n" +
                "            // Handle the error logging and destroy the AdsManager\n" +
                "            console.error(JSON.stringify(adErrorEvent.getError()));\n" +
                "            Android.close();\n" +
                "            adsManager.destroy();\n" +
                "        }\n" +
                "\n" +
                "        // An event listener to tell the SDK that our content video\n" +
                "        // is completed so the SDK can play any post-roll ads.\n" +
                "        var contentEndedListener = function () {\n" +
                "            adsLoader.contentComplete();\n" +
                "        };\n" +
                "        videoContent.onended = contentEndedListener;\n" +
                "\n" +
                "        // Request video ads.\n" +
                "        var adsRequest = new google.ima.AdsRequest();\n" +
                "        adsRequest.adTagUrl = '" + url + "';\n" +
                "\n" +
                "        // Specify the linear and nonlinear slot sizes. This helps the SDK to\n" +
                "        // select the correct creative if multiple are returned.\n" +
                "        adsRequest.linearAdSlotWidth = " + mScreenWidth + ";\n" +
                "        adsRequest.linearAdSlotHeight = " + mScreenHeight + ";\n" +
                "        adsRequest.nonLinearAdSlotWidth = " + mScreenWidth + ";\n" +
                "        adsRequest.nonLinearAdSlotHeight = " + mScreenHeight + ";\n" +
                "\n" +
                "        function requestAds() {\n" +
                "            adsLoader.requestAds(adsRequest);\n" +
                "        }\n" +
                "\n" +
                "        function onAdsManagerLoaded(adsManagerLoadedEvent) {\n" +
                "            // Get the ads manager.\n" +
                "            adsManager = adsManagerLoadedEvent.getAdsManager(\n" +
                "                    videoContent);  // See API reference for contentPlayback\n" +
                "\n" +
                "            // Add listeners to the required events.\n" +
                "            adsManager.addEventListener(\n" +
                "                    google.ima.AdErrorEvent.Type.AD_ERROR,\n" +
                "                    onAdAdError);\n" +
                "            adsManager.addEventListener(\n" +
                "                    google.ima.AdEvent.Type.CONTENT_PAUSE_REQUESTED,\n" +
                "                    onContentPauseRequested);\n" +
                "            adsManager.addEventListener(\n" +
                "                    google.ima.AdEvent.Type.CONTENT_RESUME_REQUESTED,\n" +
                "                    onContentResumeRequested);\n" +
                "            adsManager.addEventListener(\n" +
                "                    google.ima.AdEvent.Type.ALL_ADS_COMPLETED,\n" +
                "                    onAdCompleted);\n" +
                "            adsManager.addEventListener(\n" +
                "                    google.ima.AdEvent.Type.COMPLETE,\n" +
                "                    onAdCompleted);\n" +
                "            adsManager.addEventListener(\n" +
                "                    google.ima.AdEvent.Type.LOADED,\n" +
                "                    onAdLoaded);\n" +
                "            adsManager.addEventListener(\n" +
                "                    google.ima.AdEvent.Type.SKIPPED,\n" +
                "                    onAdSkipped);\n" +
                "            adsManager.addEventListener(\n" +
                "                    google.ima.AdEvent.Type.STARTED,\n" +
                "                    onAdStarted);\n" +
                "            Android.loaded();\n" +
                "        }\n" +
                "\n" +
                "        function startAd() {\n" +
                "            try {\n" +
                "                // Initialize the ads manager. Ad rules playlist will start at this time.\n" +
                "                adsManager.init(" + mScreenWidth + ", " + mScreenHeight + ", google.ima.ViewMode.NORMAL);\n" +
                "                // Call start to show ads. Single video and overlay ads will\n" +
                "                // start at this time; this call will be ignored for ad rules, as ad rules\n" +
                "                // ads start when the adsManager is initialized.\n" +
                "                adsManager.start();\n" +
                "            } catch (adError) {\n" +
                "                console.error(JSON.stringify(adError));\n" +
                "                Android.close();\n" +
                "                // An error may be thrown if there was a problem with the VAST response.\n" +
                "            }\n" +
                "        }" +
                "\n" +
                "        function onAdStarted () {\n" +
                "            Android.started();\n" +
                "        }" +
                "        function onAdCompleted () {\n" +
                "           console.log('ad completed');\n" +
                "            Android.finish();\n" +
                "        }" +
                "        function onAdAdError () {\n" +
                "            console.error('ad error');\n" +
                "            Android.close();\n" +
                "        }" +
                "        function onAdSkipped () {\n" +
                "            console.log('skipped');\n" +
                "            Android.close();\n" +
                "        }" +
                "        function onAdLoaded () {\n" +
                "           \n" +
                "        }" +
                "\n" +
                "        function onContentPauseRequested() {\n" +
                "            // This function is where you should setup UI for showing ads (e.g.\n" +
                "            // display ad timer countdown, disable seeking, etc.)\n" +
                "            videoContent.removeEventListener('ended', contentEndedListener);\n" +
                "            videoContent.pause();\n" +
                "        }\n" +
                "\n" +
                "        function onContentResumeRequested() {\n" +
                "            // This function is where you should ensure that your UI is ready\n" +
                "            // to play content.\n" +
                "            videoContent.addEventListener('ended', contentEndedListener);\n" +
                "            videoContent.play();\n" +
                "        }\n" +
                "\n" +
                "</script>\n" +
                "</body>\n" +
                "</html>";
    }

    private void startWebView() {
        mStarted = true;
        mWebView.loadDataWithBaseURL("http://localhost", html,
                "text/html", "utf-8", null);

        this.showProgressBar();
        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    public void run() {
                        if (!videoStarted) {
                            VASTLog.d(TAG, "can skip while loading");
                            showSkipButton();
                        }
                    }
                });
            }
        }, LOADER_TIMEOUT_MS);
    }

    private void createRootLayout(RelativeLayout.LayoutParams params) {

        mRootLayout = new RelativeLayout(this);
        mRootLayout.setLayoutParams(params);
        mRootLayout.setPadding(0, 0, 0, 0);
        mRootLayout.setBackgroundColor(Color.BLACK);

    }

    private void createWebView(RelativeLayout.LayoutParams params) {
        mWebView = new WebView(this);
        mWebView.setLayoutParams(params);

        mWebView.getSettings().setLoadsImagesAutomatically(true);
        mWebView.getSettings().setPluginState(WebSettings.PluginState.ON);
        mWebView.getSettings().setJavaScriptEnabled(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            mWebView.getSettings().setMediaPlaybackRequiresUserGesture(false);
        }

        mWebView.addJavascriptInterface(new WebAppInterface(this), "Android");
        mWebView.getSettings().setAppCacheEnabled(true);
        mWebView.getSettings().setDomStorageEnabled(true);


        mWebView.setWebChromeClient(new WebChromeClient() {
            public boolean onConsoleMessage(final ConsoleMessage consoleMessage) {
                String logMessage = consoleMessage.message() + " in " + consoleMessage.sourceId() + " (Line: " + consoleMessage.lineNumber() + ")";
                VASTLog.d(TAG, logMessage);
                if (consoleMessage.messageLevel() == ConsoleMessage.MessageLevel.ERROR) {
                    if (consoleMessage.sourceId() != null && consoleMessage.lineNumber() != 0) {
                        closeVPAID();
                    }
                }
                return true;
            }
        });

        mWebView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (mWebView != null && mStarted && !mPaused) {
                    mWebView.loadUrl("javascript:requestAds()");
                }
            }

            @SuppressWarnings("deprecation")
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (!url.startsWith("about:")) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(browserIntent);
                }
                return true;
            }

            @Override
            @TargetApi(24)
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return shouldOverrideUrlLoading(view, request.getUrl().toString());
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }
        });

        mRootLayout.addView(mWebView);

    }

    private void createSkipText() {
        mSkipText = new TextView(this);
        mSkipText.setVisibility(View.INVISIBLE);
        mSkipText.setTextColor(Color.WHITE);
        mSkipText.setPadding(5, 5, 5, 5);
        mSkipText.setBackgroundColor(Color.parseColor("#6b000000"));

        mRootLayout.addView(mSkipText);
    }

    private void createProgressBar() {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);

        mProgressBar = new ProgressBar(this);
        mProgressBar.setLayoutParams(params);

        mRootLayout.addView(mProgressBar);
        mProgressBar.setVisibility(View.GONE);
    }

    private void showProgressBar() {
        mProgressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        mProgressBar.setVisibility(View.GONE);
        startSkipTimer();
    }

    private void hideTitleStatusBars() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

    }

    public class WebAppInterface {
        Context mContext;

        WebAppInterface(Context c) {
            mContext = c;
        }

        @JavascriptInterface
        public void finish() {
            runOnUiThread(new Runnable() {
                public void run() {
                    VASTLog.d(TAG, "finish");
                    finishVPAID();
                }
            });
        }
        @JavascriptInterface
        public void close() {
            runOnUiThread(new Runnable() {
                public void run() {
                    VASTLog.d(TAG, "close");
                    closeVPAID();
                }
            });
        }
        @JavascriptInterface
        public void started() {
            runOnUiThread(new Runnable() {
                public void run() {
                    VASTLog.d(TAG, "ad started");
                    videoStarted = true;
                    hideProgressBar();
                }
            });
        }
        @JavascriptInterface
        public void loaded() {
            runOnUiThread(new Runnable() {
                public void run() {
                    VASTLog.d(TAG, "adsManager loaded");
                    mWebView.loadUrl("javascript:startAd()");
                }
            });
        }
    }

    private void startSkipTimer() {
        if(mSkipTime == 0 || mType != Video.Type.NON_REWARDED || canSkip) {
            return;
        }
        mSkipTimer = new Timer();
        mSkipTimer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                mSkipTime--;
                if (mSkipTime <= 0) {
                    runOnUiThread(new Runnable() {
                        public void run() {

                            runOnUiThread(new Runnable() {
                                public void run() {
                                    showSkipButton();
                                }
                            });
                        }
                    });
                    this.cancel();
                    return;
                } else {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            final String skipText = "You can skip this video in " + String.valueOf(mSkipTime) + " seconds";
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    mSkipText.setVisibility(VISIBLE);
                                    mSkipText.setText(skipText);
                                }
                            });
                        }
                    });
                }
            }

        }, 0, SKIP_TIMER_INTERVAL);
    }

    private void showSkipButton() {
        if (!canSkip) {
            String skipText = "Skip video";
            mSkipText.setText(skipText);
            canSkip = true;
            mSkipText.setVisibility(VISIBLE);
            mSkipText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    closeVPAID();
                }
            });
        }
    }

    private void closeVPAID() {
        VASTLog.d(TAG, "closeVPAID");
        isClosed = true;
        if (mListener != null) {
            mListener.vastDismiss(false);
        }
        finishVPAID();
    }

    private void finishVPAID() {
        VASTLog.d(TAG, "finishVPAID");
        if (mListener != null && !isClosed) {
            mListener.vastComplete();
            mListener.vastDismiss(true);
        }
        finish();
    }

    private boolean isSkippable() {
        return canSkip;
    }

    @Override
    public void onBackPressed() {
        if (isSkippable()) {
            this.closeVPAID();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mStarted) {
            mPaused = false;
            if (videoStarted) {
                showSkipButton();
            }
        }
        restartWebView();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mStarted) {
            mPaused = true;
            destroyWebView();
        }
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }

    public void restartWebView() {
        if(mWebView != null) {
            mWebView.onResume();
            startWebView();
        }
    }

    public void destroyWebView() {
        if(mWebView != null) {
            VASTLog.d(TAG, "destroyWebView");
            mWebView.loadUrl("about:blank");
            mWebView.onPause();
        }
    }
}