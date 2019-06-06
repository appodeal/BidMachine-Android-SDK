//
//  VastActivity.java
//

//  Copyright (c) 2014 Nexage. All rights reserved.
//

package org.nexage.sourcekit.vast.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import org.nexage.sourcekit.mraid.MRAIDInterstitial;
import org.nexage.sourcekit.mraid.MRAIDInterstitialListener;
import org.nexage.sourcekit.mraid.MRAIDNativeFeatureListener;
import org.nexage.sourcekit.util.Assets;
import org.nexage.sourcekit.util.HttpTools;
import org.nexage.sourcekit.util.Utils;
import org.nexage.sourcekit.util.VASTLog;
import org.nexage.sourcekit.util.Video;
import org.nexage.sourcekit.vast.VASTPlayer;
import org.nexage.sourcekit.vast.model.Extensions;
import org.nexage.sourcekit.vast.model.TRACKING_EVENTS_TYPE;
import org.nexage.sourcekit.vast.model.VASTCompanion;
import org.nexage.sourcekit.vast.model.VASTMediaFile;
import org.nexage.sourcekit.vast.model.VASTModel;
import org.nexage.sourcekit.vast.view.CircleCountdownView;
import org.nexage.sourcekit.vast.view.VastLinearCountdown;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class VASTActivity extends Activity implements OnCompletionListener,
        OnErrorListener, OnPreparedListener, OnVideoSizeChangedListener,
        SurfaceHolder.Callback {

    private static String TAG = "VASTActivity";

    // timer delays
    private static final long QUARTILE_TIMER_INTERVAL = 250;
    private static final long SKIP_TIMER_INTERVAL = 50;
    private static final long VIDEO_PROGRESS_TIMER_INTERVAL = 50;


    // timers
    private Timer mSkipTimer;
    private Timer mTrackingEventTimer;
    private Timer mStartVideoProgressTimer;

    private LinkedList<Integer> mVideoProgressTracker = null;
    private final int mMaxProgressTrackingPoints = 20;

    VASTPlayer.VASTPlayerListener mListener;
    private VASTModel mVastModel = null;
    private HashMap<TRACKING_EVENTS_TYPE, List<String>> mTrackingEventMap;

    private MediaPlayer mMediaPlayer;
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private RelativeLayout mOverlay;
    private RelativeLayout mRootLayout;
    private CircleCountdownView mSkipOrCloseButton;
    private CircleCountdownView mRepeatButton;
    private CircleCountdownView mMuteButton;
    private boolean mIsMuted;
    private TextView mLearnMoreText;
    private final String learnMoreText = "Learn more";

    private int mVideoHeight;
    private int mVideoWidth;
    private int mScreenWidth;
    private int mScreenHeight;
    private float mScreenDensity;
    private boolean mIsVideoPaused = false;
    private boolean mIsPlayBackError = false;
    private boolean mIsProcessedImpressions = false;
    private boolean mIsCompleted = false;
    private int mCurrentVideoPosition;
    private int mQuartile = 0;

    private ProgressBar mProgressBar;
    private Uri fileUrl;
    private Video.Type mType;
    private int mSkipTime = 5;
    private boolean canSkip = false;

    private VASTCompanion mBanner;
    private boolean hasBanner;
    private HashMap<TRACKING_EVENTS_TYPE, List<String>> mBannerTrackingEventMap;
    private WebView mBannerView;

    private VASTCompanion mCompanion;
    private View mCompanionView;
    private HashMap<TRACKING_EVENTS_TYPE, List<String>> mCompanionTrackingEventMap;
    private boolean mCompanionShown = false;
    private boolean autoClose;
    private int maxDuration;
    private List<View> touchedWebViews = new ArrayList<>();

    private int duration;
    private VastLinearCountdown mVideoProgressView;
    private Extensions mExtensions;
    private MRAIDInterstitial mraidInterstitial;
    private int closeTime;

    private int companionOrientation = Configuration.ORIENTATION_LANDSCAPE;
    private int videoOrientation = Configuration.ORIENTATION_LANDSCAPE;

    private String segmentId;
    private String placementId;

    private boolean useLayoutInCompanion;
    private int assetsColor = Assets.mainAssetsColor;
    private int assetsBackgroundColor = Assets.backgroundColor;

    private ViewPropertyAnimator mOverlayAnimator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        VASTLog.d(TAG, "in onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (savedInstanceState != null) {
            mCompanionShown = savedInstanceState.getBoolean("mCompanionShown", false);
            canSkip = savedInstanceState.getBoolean("canSkip", false);
            mCurrentVideoPosition = savedInstanceState.getInt("mCurrentVideoPosition");
            duration = savedInstanceState.getInt("duration");
            mIsProcessedImpressions = savedInstanceState.getBoolean("mIsProcessedImpressions");
            mIsPlayBackError = savedInstanceState.getBoolean("mIsPlayBackError");
            mIsCompleted = savedInstanceState.getBoolean("mIsCompleted");
            mIsMuted = savedInstanceState.getBoolean("mIsMuted");
            companionOrientation = savedInstanceState.getInt("companionOrientation");
        }
        Intent i = getIntent();
        mVastModel = (VASTModel) i.getSerializableExtra("com.nexage.android.vast.player.vastModel");
        if (mVastModel == null) {
            VASTLog.e(TAG, "vastModel is null. Stopping activity.");
            finishActivity();
        }
        VASTMediaFile mediaFile = mVastModel.getPickedMediaFile();
        if (mediaFile != null) {
            if (mediaFile.getWidth().compareTo(mediaFile.getHeight()) > 0) {
                videoOrientation = Configuration.ORIENTATION_LANDSCAPE;
            } else {
                videoOrientation = Configuration.ORIENTATION_PORTRAIT;
            }
        }

        int currentOrientation = this.getResources().getConfiguration().orientation;
        VASTLog.d(TAG, "currentOrientation:" + currentOrientation);

        if (mCompanionShown) {
            //noinspection WrongConstant
            setRequestedOrientation(getRequestedOrientation(companionOrientation));
        } else {
            //noinspection WrongConstant
            setRequestedOrientation(getRequestedOrientation(videoOrientation));
        }

        mListener = VASTPlayer.listener;
        fileUrl = i.getParcelableExtra("android.net.url");
        autoClose = i.getBooleanExtra("com.nexage.android.vast.player.autoClose", false);
        maxDuration = i.getIntExtra("com.nexage.android.vast.player.maxDuration", 0);
        closeTime = i.getIntExtra("com.nexage.android.vast.player.closeTime", 0);
        segmentId = i.getStringExtra("com.nexage.android.vast.player.segmentId");
        placementId = i.getStringExtra("com.nexage.android.vast.player.placementId");
        useLayoutInCompanion = i.getBooleanExtra("com.nexage.android.vast.player.useLayoutInCompanion", true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Pair<Integer, Integer> sizes = Utils.getScreenSize(this);
            if (videoOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                mCompanion = mVastModel.getCompanion(new Pair<>(Math.max(sizes.first, sizes.second), Math.min(sizes.first, sizes.second)));
            } else {
                mCompanion = mVastModel.getCompanion(new Pair<>(Math.min(sizes.first, sizes.second), Math.max(sizes.first, sizes.second)));
            }
        } else {
            mCompanion = mVastModel.getCompanion(Utils.getScreenSize(this));
        }
        if (mCompanion != null) {
            if (mCompanion.getWidth() >= mCompanion.getHeight()) {
                companionOrientation = Configuration.ORIENTATION_LANDSCAPE;
            } else {
                companionOrientation = Configuration.ORIENTATION_PORTRAIT;
            }
        } else {
            companionOrientation = videoOrientation;
        }
        mExtensions = mVastModel.getExtensions();
        if (mExtensions != null && mExtensions.getVastCompanion() != null) {
            mBanner = mExtensions.getVastCompanion();
        } else {
            mBanner = mVastModel.getBanner(this);
        }
        if (mExtensions != null) {
            assetsColor = mExtensions.getAssetsColor();
            assetsBackgroundColor = mExtensions.getAssetsBackgroundColor();
            if (mExtensions.getCompanionCloseTime() > 0) {
                closeTime = mExtensions.getCompanionCloseTime();
            }
        }
        if (i.hasExtra("com.nexage.android.vast.player.type")) {
            mType = (Video.Type) i.getExtras().get("com.nexage.android.vast.player.type");
        } else {
            VASTLog.e(TAG, "video type undefined.");
            mType = Video.Type.NON_REWARDED;
        }
        if (mVastModel == null) {
            VASTLog.e(TAG, "vastModel is null. Stopping activity.");
            finishActivity();
        } else {
            hideTitleStatusBars();
            DisplayMetrics displayMetrics = this.getResources()
                    .getDisplayMetrics();

            mScreenWidth = displayMetrics.widthPixels;
            mScreenHeight = displayMetrics.heightPixels;
            WindowManager window = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
            Display display = window.getDefaultDisplay();
            display.getMetrics(displayMetrics);
            mScreenDensity = displayMetrics.density;
            mTrackingEventMap = mVastModel.getTrackingUrls();
            mSkipTime = mSkipTime > mVastModel.getSkipoffset() ? mSkipTime : mVastModel.getSkipoffset();

            if (isOrientationMatchesScreenSizes()) {
                createUIComponents();
            }
        }
        if (mListener != null) {
            mListener.vastShown();
        }
    }

    @Override
    protected void onStart() {
        VASTLog.d(TAG, "entered onStart --(life cycle event)");
        super.onStart();

    }

    @Override
    protected void onResume() {
        VASTLog.d(TAG, "entered on onResume --(life cycle event)");
        super.onResume();

    }

    @Override
    protected void onStop() {
        VASTLog.d(TAG, "entered on onStop --(life cycle event)");
        super.onStop();

    }

    @Override
    protected void onRestart() {
        VASTLog.d(TAG, "entered on onRestart --(life cycle event)");
        super.onRestart();
        if (!mCompanionShown) {
            createUIComponents();
        }

    }

    public void restartVideo() {
        if (!mCompanionShown) {
            createUIComponents();
        } else {
            finishActivity();
        }
    }

    @Override
    protected void onPause() {
        VASTLog.d(TAG, "entered on onPause --(life cycle event)");
        super.onPause();
        if (mMediaPlayer != null) {
            mCurrentVideoPosition = mMediaPlayer.getCurrentPosition();
            processEvent(TRACKING_EVENTS_TYPE.pause);
        }
        cleanActivityUp();
    }

    @Override
    protected void onDestroy() {
        VASTLog.d(TAG, "entered on onDestroy --(life cycle event)");
        super.onDestroy();

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        VASTLog.d(TAG, "entered onSaveInstanceState ");
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt("mCurrentVideoPosition", mCurrentVideoPosition);
        savedInstanceState.putInt("duration", duration);
        savedInstanceState.putBoolean("mCompanionShown", mCompanionShown);
        savedInstanceState.putBoolean("canSkip", canSkip);
        savedInstanceState.putBoolean("mIsProcessedImpressions", mIsProcessedImpressions);
        savedInstanceState.putBoolean("mIsPlayBackError", mIsPlayBackError);
        savedInstanceState.putBoolean("mIsCompleted", mIsCompleted);
        savedInstanceState.putBoolean("mIsMuted", mIsMuted);
        savedInstanceState.putInt("companionOrientation", companionOrientation);
    }

    private void hideTitleStatusBars() {
        // hide title bar of application
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // hide status bar of Android
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

    }

    private void createUIComponents() {
        LayoutParams params = new LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);

        this.createRootLayout(params);
        this.createSurface(params);
        this.createMediaPlayer();
        this.createOverlay(params);
        this.createSkipTimePanel();
        this.createMuteButton();

        hasBanner = mBanner != null;
        this.createBottomPanel();

        createCompanion();
        if (mCompanion != null) {
            mCompanionTrackingEventMap = mCompanion.getTrackings();
        }

        this.setContentView(mRootLayout);

        this.createProgressBar();

        if (mCompanionShown) {
            showCompanion();
        }

    }

    private void createProgressBar() {
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL;
        mProgressBar = new ProgressBar(this);
        mProgressBar.setLayoutParams(params);

        addContentView(mProgressBar, params);
        mProgressBar.setVisibility(View.GONE);
    }

    public void showProgressBar() {
        mProgressBar.setVisibility(View.VISIBLE);
        mProgressBar.bringToFront();
    }

    public void hideProgressBar() {
        mProgressBar.setVisibility(View.GONE);
    }


    private void createRootLayout(LayoutParams params) {

        mRootLayout = new RelativeLayout(this);
        mRootLayout.setLayoutParams(params);
        mRootLayout.setPadding(0, 0, 0, 0);
        mRootLayout.setBackgroundColor(Color.BLACK);
        mRootLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                showOverlay(0);
            }
        });
    }

    @SuppressWarnings("deprecation")
    private void createSurface(LayoutParams params) {

        mSurfaceView = new SurfaceView(this);
        mSurfaceView.setLayoutParams(params);

        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(this);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mRootLayout.addView(mSurfaceView);
    }

    private void createMediaPlayer() {

        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnErrorListener(this);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnVideoSizeChangedListener(this);
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

    }

    private void createOverlay(LayoutParams params) {

        mOverlay = new RelativeLayout(this);
        mOverlay.setLayoutParams(params);
        mOverlay.setPadding(0, 0, 0, 0);
        mOverlay.setBackgroundColor(Color.TRANSPARENT);
        mOverlay.setVisibility(View.INVISIBLE);
        if (mExtensions != null && mExtensions.isVideoClickable()) {
            mOverlay.setVisibility(View.VISIBLE);
            mOverlay.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    infoClicked();
                }
            });
        }
        mRootLayout.addView(mOverlay);
    }

    private void showOverlay(int delay) {
        if (mOverlay.getVisibility() != VISIBLE) {
            mOverlay.setVisibility(VISIBLE);
            mOverlay.setAlpha(1.0f);
            Utils.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mOverlay != null && mOverlay.getVisibility() == VISIBLE && !mCompanionShown) {
                        if (Build.VERSION.SDK_INT >= 16) {
                            mOverlayAnimator = mOverlay.animate().alpha(0.0f).setDuration(1000).withLayer().setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationCancel(Animator animation) {
                                    super.onAnimationCancel(animation);
                                    animation.removeAllListeners();
                                }

                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);
                                    if (!mCompanionShown) {
                                        mOverlay.setVisibility(View.INVISIBLE);
                                    } else {
                                        mOverlay.setAlpha(1f);
                                    }
                                }
                            });
                            mOverlayAnimator.start();
                        } else {
                            mOverlay.setVisibility(View.INVISIBLE);
                        }
                    }
                }
            }, delay * 1000 + 3000);
        }
    }

    private void createBottomPanel() {
        if (hasBanner) {
            createBanner();
            mBannerTrackingEventMap = mBanner.getTrackings();
            processBannerEvent(TRACKING_EVENTS_TYPE.creativeView);
        } else {
            LayoutParams learnMoreTextParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            learnMoreTextParams.setMargins(0, 0, 5, 5);
            mLearnMoreText = new TextView(this);

            mLearnMoreText.setVisibility(VISIBLE);
            mLearnMoreText.setTextColor(assetsColor);
            mLearnMoreText.setGravity(Gravity.CENTER_VERTICAL);
            mLearnMoreText.setShadowLayer(6.0f, 0.0f, 0.0f, Assets.shadowColor);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                mLearnMoreText.setBackground(getButtonBackground());
            } else {
                //noinspection deprecation
                mLearnMoreText.setBackgroundDrawable(getButtonBackground());
            }
            mLearnMoreText.setPadding(30, 10, 30, 10);

            if (mExtensions == null) {
                learnMoreTextParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                learnMoreTextParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                mLearnMoreText.setText(learnMoreText);
                mLearnMoreText.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        infoClicked();
                    }
                });
            } else {
                if (mExtensions.canShowCta()) {
                    Pair<Integer, Integer> alignment = mExtensions.getCtaPosition();
                    learnMoreTextParams.addRule(alignment.first);
                    learnMoreTextParams.addRule(alignment.second);
                    if (mExtensions.getCtaText() != null) {
                        mLearnMoreText.setText(mExtensions.getCtaText());
                    } else {
                        mLearnMoreText.setText(learnMoreText);
                    }
                    mLearnMoreText.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            infoClicked();
                        }
                    });
                } else {
                    mLearnMoreText.setOnClickListener(null);
                    mLearnMoreText.setOnLongClickListener(null);
                    mLearnMoreText.setClickable(false);
                    mLearnMoreText.setVisibility(GONE);
                }
            }
            mLearnMoreText.setLayoutParams(learnMoreTextParams);
            mOverlay.addView(mLearnMoreText);
        }

        mVideoProgressView = new VastLinearCountdown(this, assetsColor);
        LayoutParams vastCountdownParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 5);
        vastCountdownParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        mVideoProgressView.setLayoutParams(vastCountdownParams);
        mVideoProgressView.changePercentage(0);
        mVideoProgressView.setVisibility(View.INVISIBLE);
        mOverlay.addView(mVideoProgressView);
    }

    @SuppressLint("SetTextI18n")
    private void createSkipTimePanel() {
        int height = Math.round(50 * Utils.getScreenDensity(this));
        LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                height);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP);

        LayoutParams skipButtonParams = new RelativeLayout.LayoutParams(height, height);
        if (mExtensions != null) {
            Pair<Integer, Integer> alignment = mExtensions.getClosePosition();
            skipButtonParams.addRule(alignment.first);
            skipButtonParams.addRule(alignment.second);
        } else {
            skipButtonParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            skipButtonParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        }
        skipButtonParams.addRule(RelativeLayout.CENTER_VERTICAL);
        mSkipOrCloseButton = new CircleCountdownView(this, assetsColor, assetsBackgroundColor);
        mSkipOrCloseButton.setLayoutParams(skipButtonParams);
        mSkipOrCloseButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSkippable()) {
                    closeClicked();
                }
            }
        });

        if (canSkip) {
            mSkipOrCloseButton.changePercentage(100, 0);
        }

        if (mType == Video.Type.REWARDED) {
            mSkipOrCloseButton.setVisibility(GONE);
            mSkipOrCloseButton.setImage(Assets.getBitmapFromBase64(Assets.close));
        }

        mRepeatButton = new CircleCountdownView(this, assetsColor, assetsBackgroundColor);
        LayoutParams bannerRepeatButtonParams = new RelativeLayout.LayoutParams(height, height);
        bannerRepeatButtonParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        bannerRepeatButtonParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        bannerRepeatButtonParams.addRule(RelativeLayout.CENTER_VERTICAL);
        mRepeatButton.setLayoutParams(bannerRepeatButtonParams);
        mRepeatButton.setImage(Assets.getBitmapFromBase64(Assets.repeat));
        mRepeatButton.setVisibility(GONE);
        mRepeatButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentVideoPosition = 0;
                mIsCompleted = false;
                createUIComponents();
            }
        });

        mOverlay.addView(mSkipOrCloseButton);
        mOverlay.addView(mRepeatButton);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void createCompanion() {
        if (mCompanion != null) {
            int companionWidth, companionHeight;
            if (companionOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                companionWidth = Math.max(mScreenWidth, mScreenHeight);
                companionHeight = Math.min(mScreenWidth, mScreenHeight);
            } else {
                companionWidth = Math.min(mScreenWidth, mScreenHeight);
                companionHeight = Math.max(mScreenWidth, mScreenHeight);
            }
            Pair<String, Pair<Integer, Integer>> data = mCompanion.getHtmlForMraid(companionWidth, companionHeight, mScreenDensity);
            String html = data.first;
            if (html != null) {
                CompanionListener companionListener = new CompanionListener();
                mraidInterstitial = new MRAIDInterstitial.builder(this, html, data.second.first, data.second.second)
                        .setBaseUrl(null)
                        .setListener(companionListener)
                        .setNativeFeatureListener(companionListener)
                        .setPreload(true)
                        .setCloseTime(closeTime)
                        .setIsTag(false)
                        .setUseLayout(useLayoutInCompanion)
                        .build();
            }
        } else {
            mCompanionView = new RelativeLayout(this);
            mCompanionView.setVisibility(GONE);
            mRootLayout.addView(mCompanionView);
        }
    }

    private void showCompanion() {
        mCompanionShown = true;
        int currentOrientation = this.getResources().getConfiguration().orientation;
        VASTLog.d(TAG, "currentOrientation:" + currentOrientation);
        if (currentOrientation != companionOrientation) {
            //noinspection WrongConstant
            setRequestedOrientation(getRequestedOrientation(companionOrientation));
        }
        mProgressBar.setVisibility(GONE);
        mVideoProgressView.setVisibility(GONE);
        mRepeatButton.setVisibility(GONE);
        mMuteButton.setVisibility(GONE);
        mOverlay.setVisibility(VISIBLE);
        mOverlay.setAlpha(1.0f);
        if (mOverlayAnimator != null) {
            mOverlayAnimator.cancel();
            mOverlayAnimator = null;
        }

        if (mCompanion == null) {
            mSkipOrCloseButton.changePercentage(100, 0);
            mSkipOrCloseButton.setImage(Assets.getBitmapFromBase64(Assets.close));
            mSkipOrCloseButton.setVisibility(VISIBLE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                mLearnMoreText.setBackground(getButtonBackground());
                mLearnMoreText.setShadowLayer(0, 0, 0, 0);
            } else {
                //noinspection deprecation
                mLearnMoreText.setBackgroundDrawable(getButtonBackground());
                mLearnMoreText.setShadowLayer(0, 0, 0, 0);
            }
            if (fileUrl != null && new File(fileUrl.getPath()).exists()) {
                try {
                    MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
                    mediaMetadataRetriever.setDataSource(this, fileUrl);
                    Bitmap bmFrame = mediaMetadataRetriever.getFrameAtTime(duration / 2 * 1000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
                    if (bmFrame != null) {
                        ImageView lastFrame = new ImageView(this);
                        lastFrame.setImageBitmap(bmFrame);
                        lastFrame.setAdjustViewBounds(true);
                        lastFrame.setScaleType(ImageView.ScaleType.FIT_CENTER);
                        LayoutParams lastFrameParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
                        lastFrameParams.addRule(RelativeLayout.CENTER_IN_PARENT);
                        lastFrame.setLayoutParams(lastFrameParams);
                        ((RelativeLayout) mCompanionView).addView(lastFrame);

                        mCompanionView.setOnTouchListener(new OnClickTouchListener() {
                            @Override
                            public void onClick() {
                                infoClicked();
                            }
                        });
                        mSurfaceView.setVisibility(GONE);
                    } else {
                        mOverlay.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                infoClicked();
                                finishActivity();
                            }
                        });
                    }
                    mediaMetadataRetriever.release();
                } catch (Exception e) {
                    VASTLog.e(TAG, e.getMessage());
                }
            } else {
                mCompanionView.setOnTouchListener(new OnClickTouchListener() {
                    @Override
                    public void onClick() {
                        infoClicked();
                        finishActivity();
                    }
                });
            }
            mCompanionView.setVisibility(VISIBLE);
            mSkipOrCloseButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    processCompanionEvent(TRACKING_EVENTS_TYPE.close);
                    finishActivity();
                }
            });
        } else {
            mSkipOrCloseButton.setVisibility(GONE);
            if (mBannerView != null) {
                mBannerView.setVisibility(GONE);
            }
            if (mLearnMoreText != null) {
                mLearnMoreText.setVisibility(GONE);
            }
            mSurfaceView.setVisibility(GONE);
            if (mraidInterstitial != null && mraidInterstitial.isReady) {
                mraidInterstitial.show();
            }
        }
        cleanUpMediaPlayer();

        mOverlay.bringToFront();

        processCompanionEvent(TRACKING_EVENTS_TYPE.creativeView);
    }

    private abstract static class OnClickTouchListener implements View.OnTouchListener {

        private double CLICK_ACTION_THRESHOLD_IN_INCHES = 0.2;
        private float startX;
        private float startY;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startX = event.getX();
                    startY = event.getY();
                    break;
                case MotionEvent.ACTION_UP:
                    float endX = event.getX();
                    float endY = event.getY();
                    if (isClick(v, startX, endX, startY, endY)) {
                        onClick();
                    }
                    break;
            }
            return true;
        }

        private boolean isClick(View v, float startX, float endX, float startY, float endY) {
            DisplayMetrics metrics = v.getContext().getResources().getDisplayMetrics();
            double distance = Math.sqrt(Math.pow((endX - startX) / metrics.xdpi, 2) + Math.pow((endY - startY) / metrics.ydpi, 2));
            return !(distance > CLICK_ACTION_THRESHOLD_IN_INCHES);
        }


        public abstract void onClick();

    }

    @SuppressLint("SetJavaScriptEnabled")
    private void createBanner() {
        int width;
        int height;
        if (Utils.isTablet(this) && mBanner.getWidth() == 728 && mBanner.getHeight() == 90) {
            width = Math.round(728 * Utils.getScreenDensity(this));
            height = Math.round(90 * Utils.getScreenDensity(this));
        } else {
            width = Math.round(320 * Utils.getScreenDensity(this));
            height = Math.round(50 * Utils.getScreenDensity(this));
        }

        LayoutParams params = new RelativeLayout.LayoutParams(width, height);
        if (mExtensions != null) {
            Pair<Integer, Integer> alignment = mExtensions.getCtaPosition();
            params.addRule(alignment.first);
            params.addRule(alignment.second);
        } else {
            params.addRule(RelativeLayout.CENTER_IN_PARENT);
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        }
        params.setMargins(0, 0, 0, 5);
        mBannerView = new WebView(this);
        mBannerView.getSettings().setJavaScriptEnabled(true);
        mBannerView.setScrollContainer(false);
        mBannerView.setVerticalScrollBarEnabled(false);
        mBannerView.setHorizontalScrollBarEnabled(false);
        mBannerView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        mBannerView.setFocusableInTouchMode(false);
        mBannerView.setBackgroundColor(Color.TRANSPARENT);
        mBannerView.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);
        mBannerView.setWebViewClient(companionWebViewClient);
        mBannerView.setWebChromeClient(companionWebChromeClient);
        mBannerView.setOnTouchListener(companionOnTouchListener);
        mBannerView.setLayoutParams(params);
        mBannerView.setPadding(0, 0, 0, 0);
        String html = mBanner.getHtml(width, height, mScreenDensity);
        if (html != null) {
            mBannerView.loadDataWithBaseURL("", html, "text/html", "utf-8", null);
        }
        mOverlay.addView(mBannerView);
    }

    public void hideBanner() {
        if (hasBanner) {
            mBannerView.setVisibility(GONE);
        }
    }

    private void createMuteButton() {
        int height = Math.round(50 * Utils.getScreenDensity(this));
        mMuteButton = new CircleCountdownView(this, assetsColor, assetsBackgroundColor);
        LayoutParams muteButtonParams = new RelativeLayout.LayoutParams(height, height);
        if (mExtensions != null) {
            Pair<Integer, Integer> alignment = mExtensions.getMutePosition();
            muteButtonParams.addRule(alignment.first);
            muteButtonParams.addRule(alignment.second);
        } else {
            muteButtonParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            muteButtonParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        }
        muteButtonParams.addRule(RelativeLayout.CENTER_VERTICAL);
        mMuteButton.setLayoutParams(muteButtonParams);
        if (mExtensions == null || mExtensions.canShowMute()) {
            mMuteButton.setVisibility(VISIBLE);
            mMuteButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mIsMuted = !mIsMuted;
                    updateMuted(true);
                }
            });
        } else {
            mMuteButton.setVisibility(GONE);
        }
        mOverlay.addView(mMuteButton);
    }

    private void updateMuted(boolean sendEvent) {
        if (mIsMuted) {
            if (mMuteButton != null) {
                mMuteButton.setImage(Assets.getBitmapFromBase64(Assets.unmute));
            }
            if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                mMediaPlayer.setVolume(0, 0);
            }
            if (sendEvent) {
                processEvent(TRACKING_EVENTS_TYPE.mute);
            }
        } else {
            if (mMuteButton != null) {
                mMuteButton.setImage(Assets.getBitmapFromBase64(Assets.mute));
            }
            if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                mMediaPlayer.setVolume(1, 1);
            }
            if (sendEvent) {
                processEvent(TRACKING_EVENTS_TYPE.unmute);
            }
        }
    }


    View.OnTouchListener companionOnTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_UP:
                    touchedWebViews.add(v);
                    if (!v.hasFocus()) {
                        v.requestFocus();
                    }
                    break;
            }
            return false;
        }
    };

    WebChromeClient companionWebChromeClient = new WebChromeClient() {
        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
            VASTLog.d("JS alert", message);
            return handlePopups(result);
        }


        @Override
        public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
            VASTLog.d("JS confirm", message);
            return handlePopups(result);
        }

        @Override
        public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
            VASTLog.d("JS prompt", message);
            return handlePopups(result);
        }

        private boolean handlePopups(JsResult result) {
            result.cancel();
            return true;
        }
    };

    WebViewClient companionWebViewClient = new WebViewClient() {
        @Override
        public void onPageFinished(WebView view, String url) {
            view.setBackgroundColor(Color.TRANSPARENT);
            view.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);
        }

        @Override
        @TargetApi(24)
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            if (request.hasGesture()) {
                touchedWebViews.add(view);
            }
            return shouldOverrideUrlLoading(view, request.getUrl().toString());
        }

        @SuppressWarnings("deprecation")
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (touchedWebViews.contains(view)) {
                VASTLog.d(TAG, "banner clicked");
                processClickThroughEvent(url);
            }
            return true;
        }
    };

    private void infoClicked() {
        VASTLog.d(TAG, "entered infoClicked:");
        String clickThroughUrl = mVastModel.getVideoClicks().getClickThrough();
        processClickThroughEvent(clickThroughUrl);
    }

    private void processClickThroughEvent(String clickThroughUrl) {
        VASTLog.d(TAG, "entered processClickThroughEvent:");
        VASTLog.d(TAG, "clickThrough url: " + clickThroughUrl);
        if (clickThroughUrl != null) {

            // Before we send the app to the click through url, we will process ClickTracking URL's.
            List<String> urls = mVastModel.getVideoClicks().getClickTracking();
            fireUrls(urls);
            if (mMediaPlayer != null) {
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.pause();
                }
                mCurrentVideoPosition = mMediaPlayer.getCurrentPosition();
            }
            cleanActivityUp();
            if (mListener != null) {
                mListener.vastClick(clickThroughUrl, this);
            }
        }

    }

    private void closeClicked() {
        if (mMediaPlayer != null) {
            mMediaPlayer.seekTo(mMediaPlayer.getDuration());
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
            }
            stopQuartileTimer();
            stopVideoProgressTimer();
            stopSkipTimer();
        }

        VASTLog.d(TAG, "entered closeClicked()");

        if (!mIsPlayBackError) {
            this.processEvent(TRACKING_EVENTS_TYPE.close);
            if (maxDuration > 0 && mType == Video.Type.REWARDED) {
                if (mListener != null) {
                    mListener.vastComplete();
                }
            }
            finishVAST();
        } else {
            finishActivity();
        }

        VASTLog.d(TAG, "leaving closeClicked()");
    }


    @Override
    public void onBackPressed() {
        VASTLog.d(TAG, "entered onBackPressed");
        if (isSkippable() && !mCompanionShown) {
            this.closeClicked();
        }
    }

    public void surfaceCreated(SurfaceHolder holder) {
        VASTLog.d(TAG, "surfaceCreated -- (SurfaceHolder callback)");
        try {
            if (mMediaPlayer == null) {
                createMediaPlayer();
            }
            if (!mCompanionShown) {
                this.showProgressBar();
                mMediaPlayer.setDisplay(mSurfaceHolder);
                String url = mVastModel.getPickedMediaFileURL();

                VASTLog.d(TAG, "URL for media file:" + url);
                if (fileUrl == null) {
                    mMediaPlayer.setDataSource(url);
                } else {
                    mMediaPlayer.setDataSource(this, fileUrl);
                }
                mMediaPlayer.prepareAsync();
            }
        } catch (Exception e) {
            VASTLog.e(TAG, e.getMessage(), e);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int arg1, int arg2,
                               int arg3) {
        VASTLog.d(TAG, "entered surfaceChanged -- (SurfaceHolder callback)");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        VASTLog.d(TAG, "entered surfaceDestroyed -- (SurfaceHolder callback)");
        cleanUpMediaPlayer();

    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
        VASTLog.d(TAG, "entered onVideoSizeChanged -- (MediaPlayer callback)");
        mVideoWidth = width;
        mVideoHeight = height;
        VASTLog.d(TAG, "video size: " + mVideoWidth + "x" + mVideoHeight);
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        VASTLog.d(TAG, "entered onPrepared called --(MediaPlayer callback) ....about to play");
        if (!mCompanionShown) {
            calculateAspectRatio();

            mMediaPlayer.start();
            processEvent(TRACKING_EVENTS_TYPE.creativeView);
            processEvent(TRACKING_EVENTS_TYPE.fullscreen);
            duration = mMediaPlayer.getDuration();

            if (mType == Video.Type.NON_REWARDED) {
                showOverlay(mSkipTime);
            } else {
                showOverlay(0);
            }

            this.startSkipTimer();

            this.hideProgressBar();

            if (mIsVideoPaused) {
                VASTLog.d(TAG, "pausing video");
                mMediaPlayer.pause();
            } else {
                this.startVideoProgressTimer();
            }

            VASTLog.d(TAG, "current location in video:" + mCurrentVideoPosition);
            if (mCurrentVideoPosition > 0) {
                VASTLog.d(TAG, "seeking to location:" + mCurrentVideoPosition);
                mMediaPlayer.seekTo(mCurrentVideoPosition);
                processEvent(TRACKING_EVENTS_TYPE.resume);
            }
            int curPos = mMediaPlayer.getCurrentPosition();
            VASTLog.d(TAG, "current location in player:" + curPos);

            if (!mIsProcessedImpressions) {
                this.processImpressions();
            }

            startQuartileTimer();

            if (!mMediaPlayer.isPlaying() && !mIsVideoPaused) {
                mMediaPlayer.start();
            }
            updateMuted(false);
        }
    }


    private void calculateAspectRatio() {
        VASTLog.d(TAG, "entered calculateAspectRatio");

        if (mVideoWidth == 0 || mVideoHeight == 0) {
            VASTLog.w(TAG, "mVideoWidth or mVideoHeight is 0, skipping calculateAspectRatio");
            return;
        }

        int videoViewWidth, videoViewHeight;
        if (videoOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            videoViewWidth = Math.max(mScreenWidth, mScreenHeight);
            videoViewHeight = Math.min(mScreenWidth, mScreenHeight);
        } else {
            videoViewWidth = Math.min(mScreenWidth, mScreenHeight);
            videoViewHeight = Math.max(mScreenWidth, mScreenHeight);
        }

        VASTLog.d(TAG, "calculating aspect ratio");
        double widthRatio = 1.0 * videoViewWidth / mVideoWidth;
        double heightRatio = 1.0 * videoViewHeight / mVideoHeight;

        double scale = Math.min(widthRatio, heightRatio);

        int surfaceWidth = (int) (scale * mVideoWidth);
        int surfaceHeight = (int) (scale * mVideoHeight);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                surfaceWidth, surfaceHeight);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        mSurfaceView.setLayoutParams(params);

        mSurfaceHolder.setFixedSize(surfaceWidth, surfaceHeight);

        VASTLog.d(TAG, " screen size: " + mScreenWidth + "x" + mScreenHeight);
        VASTLog.d(TAG, " video size:  " + mVideoWidth + "x" + mVideoHeight);
        VASTLog.d(TAG, " widthRatio:   " + widthRatio);
        VASTLog.d(TAG, " heightRatio:   " + heightRatio);
        VASTLog.d(TAG, "surface size: " + surfaceWidth + "x" + surfaceHeight);
    }

    private void cleanActivityUp() {

        this.cleanUpMediaPlayer();
        this.stopQuartileTimer();
        this.stopVideoProgressTimer();
        this.stopSkipTimer();
    }

    private void cleanUpMediaPlayer() {

        VASTLog.d(TAG, "entered cleanUpMediaPlayer ");

        if (mMediaPlayer != null) {

            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
            }

            mMediaPlayer.setOnCompletionListener(null);
            mMediaPlayer.setOnErrorListener(null);
            mMediaPlayer.setOnPreparedListener(null);
            mMediaPlayer.setOnVideoSizeChangedListener(null);

            mMediaPlayer.release();
            mMediaPlayer = null;
        }

    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        VASTLog.e(TAG, "entered onError -- (MediaPlayer callback)");
        mIsPlayBackError = true;
        VASTLog.e(TAG, "Shutting down Activity due to Media Player errors: WHAT:" + what + ": EXTRA:" + extra + ":");
        cleanUpMediaPlayer();
        processErrorEvent();
        finishVAST();

        return true;
    }

    private void processErrorEvent() {
        VASTLog.d(TAG, "entered processErrorEvent");
        try {
            if (mVastModel != null) {
                mVastModel.sendError(VASTModel.ERROR_CODE_ERROR_SHOWING);
            }
        } catch (Exception ex) {
            VASTLog.e(TAG, ex.getMessage());
        }
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        VASTLog
                .d(TAG, "entered onCOMPLETION -- (MediaPlayer callback)");
        stopVideoProgressTimer();
        canSkip = true;
        if (!mIsPlayBackError && !mIsCompleted) {
            mIsCompleted = true;
            this.processEvent(TRACKING_EVENTS_TYPE.complete);

            if (mListener != null) {
                mListener.vastComplete();
            }
            finishVAST();
        } else if (mIsCompleted) {
            finishVAST();
        }

    }


    private void processImpressions() {
        VASTLog.d(TAG, "entered processImpressions");

        mIsProcessedImpressions = true;
        List<String> impressions = mVastModel.getImpressions();
        fireUrls(impressions);

    }

    private void fireUrls(List<String> urls) {
        VASTLog.d(TAG, "entered fireUrls");

        if (urls != null) {

            for (String url : urls) {
                if (url != null) {
                    VASTLog.v(TAG, "\tfiring url:" + url);

                    if (segmentId != null && (url.contains("${APPODEALX_SEGMENT_ID}") || url.contains("%24%7BAPPODEALX_SEGMENT_ID%7D"))) {
                        url = url.replace("${APPODEALX_SEGMENT_ID}", segmentId);
                        url = url.replace("%24%7BAPPODEALX_SEGMENT_ID%7D", segmentId);
                    }
                    if (placementId != null && (url.contains("${APPODEALX_PLACEMENT_ID}") || url.contains("%24%7BAPPODEALX_PLACEMENT_ID%7D"))) {
                        url = url.replace("${APPODEALX_PLACEMENT_ID}", placementId);
                        url = url.replace("%24%7BAPPODEALX_PLACEMENT_ID%7D", placementId);
                    }

                    HttpTools.httpGetURL(url);
                }
            }

        } else {
            VASTLog.d(TAG, "\turl list is null");
        }


    }

    // Timers

    private void startSkipTimer() {
        VASTLog.d(TAG, "entered startSkipTimer");
        if (canSkip) {
            mSkipOrCloseButton.setVisibility(VISIBLE);
            mSkipOrCloseButton.setImage(Assets.getBitmapFromBase64(Assets.close));
        }
        if (mSkipTime == 0 || mType != Video.Type.NON_REWARDED) {
            return;
        }
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mSkipTimer = new Timer();
            mSkipTimer.scheduleAtFixedRate(new TimerTask() {

                @Override
                public void run() {
                    try {
                        final int curPos = mMediaPlayer.getCurrentPosition();
                        // wait for the video to really start
                        if (curPos == 0) {
                            return;
                        }
                        final int played = mSkipTime * 1000 - curPos;
                        runOnUiThread(new Runnable() {
                            public void run() {
                                int localSkipTime = mSkipTime;
                                int percent = localSkipTime != 0 ? 100 * curPos / (localSkipTime * 1000) : 100;
                                if (percent < 100) {
                                    mSkipOrCloseButton.changePercentage(percent, (int) Math.ceil((double) played / 1000));
                                } else {
                                    mSkipOrCloseButton.setImage(Assets.getBitmapFromBase64(Assets.close));
                                }
                            }
                        });
                        if (played <= 0) {
                            mSkipTime = 0;
                            canSkip = true;
                            this.cancel();
                        }
                    } catch (Exception e) {
                        VASTLog.e(TAG, "mediaPlayer.getCurrentPosition exception: " + e.getMessage());
                        this.cancel();
                    }
                }

            }, 0, SKIP_TIMER_INTERVAL);
        }
    }

    private void stopSkipTimer() {
        VASTLog.d(TAG, "entered stopSkipTimer");
        if (mSkipTimer != null) {
            mSkipTimer.cancel();
            mSkipTimer = null;
        }
    }

    private void startQuartileTimer() {
        VASTLog.d(TAG, "entered startQuartileTimer");
        stopQuartileTimer();

        if (mIsCompleted) {
            VASTLog.d(TAG, "ending quartileTimer because the video has been replayed");
            return;
        }

        final int videoDuration = mMediaPlayer.getDuration();

        mTrackingEventTimer = new Timer();
        mTrackingEventTimer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                int percentage;
                try {
                    int curPos = mMediaPlayer.getCurrentPosition();
                    // wait for the video to really start
                    if (curPos == 0) {
                        return;
                    }
                    percentage = 100 * curPos / videoDuration;

                    if (curPos > maxDuration && maxDuration > 0 && mType == Video.Type.REWARDED) {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                mSkipOrCloseButton.changePercentage(100, 0);
                                mSkipOrCloseButton.setVisibility(VISIBLE);
                                canSkip = true;
                            }
                        });
                    }
                } catch (Exception e) {
                    VASTLog.w(
                            TAG,
                            "mediaPlayer.getCurrentPosition exception: "
                                    + e.getMessage());
                    this.cancel();
                    return;
                }

                if (percentage >= 25 * mQuartile) {
                    if (mQuartile == 0) {
                        VASTLog.i(TAG, "Video at start: (" + percentage
                                + "%)");
                        processEvent(TRACKING_EVENTS_TYPE.start);
                    } else if (mQuartile == 1) {
                        VASTLog.i(TAG, "Video at first quartile: ("
                                + percentage + "%)");
                        processEvent(TRACKING_EVENTS_TYPE.firstQuartile);
                    } else if (mQuartile == 2) {
                        VASTLog.i(TAG, "Video at midpoint: ("
                                + percentage + "%)");
                        processEvent(TRACKING_EVENTS_TYPE.midpoint);
                    } else if (mQuartile == 3) {
                        VASTLog.i(TAG, "Video at third quartile: ("
                                + percentage + "%)");
                        processEvent(TRACKING_EVENTS_TYPE.thirdQuartile);
                        stopQuartileTimer();
                    }
                    mQuartile++;
                }
            }

        }, 0, QUARTILE_TIMER_INTERVAL);
    }

    private void stopQuartileTimer() {

        if (mTrackingEventTimer != null) {
            mTrackingEventTimer.cancel();
            mTrackingEventTimer = null;
        }
    }

    private void startVideoProgressTimer() {
        VASTLog.d(TAG, "entered startVideoProgressTimer");
        VASTLog.v(TAG, "video progressing (start)");
        mStartVideoProgressTimer = new Timer();
        mVideoProgressTracker = new LinkedList<>();

        mStartVideoProgressTimer.schedule(new TimerTask() {
            int maxAmountInList = mMaxProgressTrackingPoints - 1;
            int errorCount = 0;

            @Override
            public void run() {
                if (mMediaPlayer == null) {
                    return;
                }
                int firstPosition;
                int lastPosition;
                if (mVideoProgressTracker.size() == 2) {
                    firstPosition = mVideoProgressTracker.getFirst();
                    lastPosition = mVideoProgressTracker.getLast();
                    if (firstPosition > lastPosition) {
                        VASTLog.e(TAG, "video progressing (seek error)");
                        mVideoProgressTracker.removeFirst();
                    }
                }
                if (mVideoProgressTracker.size() == maxAmountInList) {
                    firstPosition = mVideoProgressTracker.getFirst();
                    lastPosition = mVideoProgressTracker.getLast();
                    VASTLog.v(TAG, "video progressing (position:" + lastPosition + ", first: " + firstPosition + ")");
                    if (lastPosition > firstPosition) {
                        mVideoProgressTracker.removeFirst();
                    } else {
                        errorCount++;
                        if (errorCount >= 3) {
                            VASTLog.e(TAG, "video progressing (detected video hang)");
                            mIsPlayBackError = true;
                            stopVideoProgressTimer();
                            processErrorEvent();
                            finishActivity();
                        }
                    }
                }

                try {
                    final int curPos = mMediaPlayer.getCurrentPosition();

//                    VASTLog.v(TAG, "video progressing (current position:" + curPos + ")");
                    mVideoProgressTracker.addLast(curPos);

                    if ((duration != 0 && curPos > 0) && (mExtensions == null || mExtensions.canShowProgress())) {
                        VASTLog.v(TAG, "video percentage:" + Math.round(100 * curPos / duration) + " remaining time: " + Math.round((duration - curPos) / 1000));
                        runOnUiThread(new Runnable() {
                            public void run() {
                                mVideoProgressView.changePercentage((float) 100 * curPos / duration);
                                mVideoProgressView.setVisibility(VISIBLE);
                            }
                        });
                    }
                } catch (Exception e) {
                    // occasionally the timer is in the middle of processing and
                    // the media player is cleaned up
                }
            }

        }, 0, VIDEO_PROGRESS_TIMER_INTERVAL);

    }

    private void stopVideoProgressTimer() {
        VASTLog.d(TAG, "entered stopVideoProgressTimer");

        if (mStartVideoProgressTimer != null) {

            mStartVideoProgressTimer.cancel();
        }
    }

    private void processEvent(TRACKING_EVENTS_TYPE eventName) {
        VASTLog.i(TAG, "entered Processing Event: " + eventName);
        if (mTrackingEventMap != null && !mTrackingEventMap.isEmpty()) {
            List<String> urls = mTrackingEventMap.get(eventName);
            fireUrls(urls);
        } else {
            VASTLog.i(TAG, "tracking event map is null or empty");
        }
    }

    private void processCompanionEvent(TRACKING_EVENTS_TYPE eventName) {
        if (mCompanion != null && mCompanionTrackingEventMap != null) {
            VASTLog.i(TAG, "entered Processing Event: " + eventName);
            List<String> urls = mCompanionTrackingEventMap.get(eventName);

            fireUrls(urls);
        }

    }

    private void processBannerEvent(TRACKING_EVENTS_TYPE eventName) {
        if (hasBanner) {
            VASTLog.i(TAG, "entered Processing Event: " + eventName);
            List<String> urls = mBannerTrackingEventMap.get(eventName);

            fireUrls(urls);
        }

    }

    private void finishVAST() {
        if (autoClose || (mExtensions != null && !mExtensions.canShowCompanion())) {
            finishActivity();
            return;
        }
        VASTLog.i(TAG, "show companion");
        hideProgressBar();
        hideBanner();
        showCompanion();
    }

    private void finishActivity() {
        if (mListener != null) {
            mListener.vastDismiss(isFinished());
        }
        runOnUiThread(new Runnable() {
            public void run() {
                try {
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                } catch (Exception e) {
                    VASTLog.e(TAG, e.getMessage());
                }
            }
        });
        finish();
    }

    private boolean isSkippable() {
        return canSkip;
    }

    private boolean isFinished() {
        return closeTime == 0 && mIsCompleted || closeTime > 0 && mCompanionShown;
    }

    private Drawable getButtonBackground() {
        GradientDrawable backgroundShape =  new GradientDrawable();
        backgroundShape.setShape(GradientDrawable.RECTANGLE);
        backgroundShape.setColor(assetsBackgroundColor);
        backgroundShape.setCornerRadius(100);
        return backgroundShape;
    }

    private class CompanionListener implements MRAIDInterstitialListener, MRAIDNativeFeatureListener {
        @Override
        public void mraidInterstitialLoaded(MRAIDInterstitial mraidInterstitial) {
            if (mCompanionShown) {
                mraidInterstitial.show();
            }
        }

        @Override
        public void mraidInterstitialShow(MRAIDInterstitial mraidInterstitial) {

        }

        @Override
        public void mraidInterstitialHide(MRAIDInterstitial mraidInterstitial) {
            finishActivity();
        }

        @Override
        public void mraidInterstitialNoFill(MRAIDInterstitial mraidInterstitial) {

        }

        @Override
        public void mraidNativeFeatureCallTel(String url) {

        }

        @Override
        public void mraidNativeFeatureCreateCalendarEvent(String eventJSON) {

        }

        @Override
        public void mraidNativeFeaturePlayVideo(String url) {

        }

        @Override
        public void mraidNativeFeatureOpenBrowser(String url, WebView view) {
            processClickThroughEvent(url);
        }

        @Override
        public void mraidNativeFeatureStorePicture(String url) {

        }

        @Override
        public void mraidNativeFeatureSendSms(String url) {

        }
    }

    private int getRequestedOrientation(int requestedOrientation) {
        switch (requestedOrientation) {
            case Configuration.ORIENTATION_PORTRAIT:
                return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
            case Configuration.ORIENTATION_LANDSCAPE:
                return ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
            default:
                return ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        int currentOrientation = this.getResources().getConfiguration().orientation;
        if (mCompanionShown) {
            if (currentOrientation != companionOrientation) {
                //noinspection WrongConstant
                setRequestedOrientation(getRequestedOrientation(companionOrientation));
            } else {
                if (mRootLayout == null) {
                    createUIComponents();
                }
            }
        } else {
            if (currentOrientation != videoOrientation) {
                //noinspection WrongConstant
                setRequestedOrientation(getRequestedOrientation(videoOrientation));
            } else {
                if (mRootLayout == null) {
                    createUIComponents();
                }
            }
        }
    }

    private boolean isOrientationMatchesScreenSizes() {
        int orientation = mCompanionShown ? companionOrientation : videoOrientation;
        Pair<Integer, Integer> sizes = Utils.getScreenSize(this);
        switch (orientation) {
            case Configuration.ORIENTATION_LANDSCAPE:
                return sizes.first >= sizes.second;
            case Configuration.ORIENTATION_PORTRAIT:
                return sizes.first < sizes.second;
            default:
                return true;
        }
    }
}
