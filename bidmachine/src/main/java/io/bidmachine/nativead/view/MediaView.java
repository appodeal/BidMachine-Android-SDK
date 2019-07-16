package io.bidmachine.nativead.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import com.explorestack.iab.utils.Assets;
import com.explorestack.iab.vast.TrackingEvent;
import com.explorestack.iab.vast.VastError;
import com.explorestack.iab.vast.VastRequest;
import com.explorestack.iab.vast.view.CircleCountdownView;
import io.bidmachine.core.Logger;
import io.bidmachine.core.Utils;
import io.bidmachine.nativead.NativeAdObject;
import io.bidmachine.nativead.tasks.DownloadVastVideoTask;
import io.bidmachine.nativead.tasks.DownloadVideoTask;
import io.bidmachine.nativead.utils.*;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static io.bidmachine.core.Utils.getScreenDensity;

public class MediaView extends RelativeLayout implements MediaPlayer.OnCompletionListener,
        MediaPlayer.OnErrorListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnVideoSizeChangedListener,
        TextureView.SurfaceTextureListener, VideoPlayerActivity.VideoPlayerActivityListener {

    private static final float ASPECT_MULTIPLIER_WIDTH_TO_HEIGHT = 9f / 16;
    private static final float ASPECT_MULTIPLIER_HEIGHT_TO_WIDTH = 16f / 9;

    @Nullable
    NativePrivateData nativeData;
    @Nullable
    NativeMediaPrivateData nativeMediaData;
    @Nullable
    NativeInteractor nativeInteractor;

    boolean isInitialized = false;
    private ImageView imageView;
    private ProgressBar progressBarView;
    private ImageView playButton;
    private CircleCountdownView muteButton;
    private MediaPlayer mediaPlayer;
    private TextureView textureView;
    private Timer videoVisibilityCheckerTimer;
    private boolean isVideoStartNotified;
    private boolean isVideoFinishNotified;
    private boolean isMuted = true;
    private boolean mediaPlayerPrepared;
    private boolean mediaPlayerPreparing;
    private boolean viewOnScreen;
    private boolean startPlayVideoWhenReady;
    private boolean hasVideo;
    private volatile boolean error;
    private boolean finishedOrExpanded;
    private VastRequest vastRequest;
    private int videoDuration;
    private int quartile;

    public static VideoPlayerActivity.VideoPlayerActivityListener listener;

    private enum State {IMAGE, PLAYING, LOADING, PAUSED}

    private State state = State.IMAGE;

    public MediaView(Context context) {
        super(context);
    }

    public MediaView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MediaView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(21)
    public MediaView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setNativeAdObject(@NonNull NativeAdObject nativeAdObject) {
        applyNative(nativeAdObject, nativeAdObject, nativeAdObject);
    }

    public void applyNative(@NonNull NativePrivateData nativeData,
                            @NonNull NativeMediaPrivateData nativeMediaData,
                            @NonNull NativeInteractor nativeInteractor) {
        this.nativeData = nativeData;
        this.nativeMediaData = nativeMediaData;
        this.nativeInteractor = nativeInteractor;

        if ((nativeData.getVideoUrl() != null && !nativeData.getVideoUrl().isEmpty())
                || (nativeData.getVideoAdm() != null && !nativeData.getVideoAdm().isEmpty())) {
            hasVideo = true;
            if (nativeMediaData.getVastRequest() != null) {
                vastRequest = nativeMediaData.getVastRequest();
            }
        }
        createView();
    }

    void createView() {
        if (!isInitialized) {
            isInitialized = true;
            imageView = new ImageView(getContext());
            LayoutParams imageViewParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            imageView.setLayoutParams(imageViewParams);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageView.setAdjustViewBounds(true);
            this.addView(imageView);
            if (hasVideo) {
                int playAndProgressViewSize = Math.round(50 * getScreenDensity(getContext()));
                progressBarView = new ProgressBar(getContext(), null, android.R.attr.progressBarStyleLarge);
                LayoutParams progressBarViewParams = new LayoutParams(playAndProgressViewSize, playAndProgressViewSize);
                progressBarViewParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
                progressBarView.setLayoutParams(progressBarViewParams);
                progressBarView.setBackgroundColor(Color.parseColor("#6b000000"));
                this.addView(progressBarView);
                playButton = new ImageView(getContext());
                playButton.setImageResource(android.R.drawable.ic_media_play);
                LayoutParams playButtonParams = new LayoutParams(playAndProgressViewSize, playAndProgressViewSize);
                playButtonParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
                playButton.setLayoutParams(playButtonParams);
                playButton.setBackgroundColor(Color.parseColor("#6b000000"));
                playButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startPlayVideoWhenReady = true;
                        tryPlayVideo();
                    }
                });
                this.addView(playButton);
                textureView = new TextureView(getContext());
                textureView.setSurfaceTextureListener(this);
                LayoutParams textureViewParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);
                textureViewParams.addRule(RelativeLayout.CENTER_IN_PARENT);
                textureView.setLayoutParams(textureViewParams);
                textureView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (nativeMediaData == null || nativeMediaData.getVideoUri() == null) {
                            return;
                        }
                        Logger.log("Video clicked");
                        listener = MediaView.this;
                        finishedOrExpanded = true;
                        int currentPosition = 0;
                        if (isMediaPlayerAvailable() && mediaPlayer.isPlaying()) {
                            currentPosition = mediaPlayer.getCurrentPosition();
                        }
                        pausePlayer();
                        Intent intent = VideoPlayerActivity.getIntent(getContext(),
                                nativeMediaData.getVideoUri().getPath(), currentPosition);
                        getContext().startActivity(intent);
                    }
                });
                this.addView(textureView);
                createMuteButton();
                createMediaPlayer();
                if (nativeMediaData != null
                        && nativeMediaData.getVideoUri() != null
                        && nativeMediaData.getVideoUri().getPath() != null
                        && new File(nativeMediaData.getVideoUri().getPath()).exists()) {
                    startPlayVideoWhenReady = true;
                } else if (nativeData != null) {
                    state = State.LOADING;
                    updateViewState();
                    if (nativeData.getVideoUrl() != null && !nativeData.getVideoUrl().isEmpty()) {
                        final DownloadVideoTask downloadVideoTask = new DownloadVideoTask(getContext(),
                                new DownloadVideoTask.OnLoadedListener() {
                                    @Override
                                    public void onVideoLoaded(DownloadVideoTask task, Uri videoFileUri) {
                                        Logger.log("MediaView video has been loaded");
                                        nativeMediaData.setVideoUri(videoFileUri);
                                        prepareMediaPlayer();
                                    }

                                    @Override
                                    public void onVideoLoadingError(DownloadVideoTask task) {
                                        Logger.log("MediaView video hasn't been loaded");
                                        state = State.IMAGE;
                                        updateViewState();
                                        hasVideo = false;
                                    }
                                }, nativeData.getVideoUrl());
                        executeTask(downloadVideoTask);
                    } else if (nativeData.getVideoAdm() != null && !nativeData.getVideoAdm().isEmpty()) {
                        final DownloadVastVideoTask downloadVideoVastTask = new DownloadVastVideoTask(getContext(),
                                new DownloadVastVideoTask.OnLoadedListener() {
                                    @Override
                                    public void onVideoLoaded(DownloadVastVideoTask task, Uri videoFileUri, VastRequest vastRequest) {
                                        MediaView.this.vastRequest = vastRequest;
                                        nativeMediaData.setVideoUri(videoFileUri);
                                        nativeMediaData.setVastRequest(vastRequest);
                                        prepareMediaPlayer();
                                    }

                                    @Override
                                    public void onVideoLoadingError(DownloadVastVideoTask task) {
                                        state = State.IMAGE;
                                        updateViewState();
                                        hasVideo = false;
                                    }
                                }, nativeData.getVideoAdm());
                        executeTask(downloadVideoVastTask);
                    }
                }
            } else {
                state = State.IMAGE;
                updateViewState();
                imageView.bringToFront();
            }
        }
        if (nativeData != null && nativeMediaData != null) {
            ImageHelper.fillImageView(getContext(), imageView, nativeMediaData.getImageUri(),
                    nativeMediaData.getImageBitmap());
        }
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        //Logger.log(String.format("onWindowVisibilityChanged: %s", visibility));
        if (visibility == VISIBLE) {
            if (startPlayVideoWhenReady) {
                tryPlayVideo();
            }
        } else {
            pausePlayer();
        }
        super.onWindowVisibilityChanged(visibility);
    }

    private void pausePlayer() {
        if (isMediaPlayerAvailable() && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
        if (state != State.LOADING) {
            state = State.PAUSED;
            updateViewState();
        }
    }

    private void createMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnVideoSizeChangedListener(this);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        updateVolume();
    }

    private void prepareMediaPlayer() {
        try {
            if (!mediaPlayerPrepared
                    && nativeMediaData != null
                    && nativeMediaData.getVideoUri() != null
                    && !mediaPlayerPreparing
                    && !error) {
                mediaPlayer.setDataSource(getContext(), nativeMediaData.getVideoUri());
                mediaPlayer.prepareAsync();
                mediaPlayerPreparing = true;
            }
        } catch (Exception e) {
            Logger.log(e);
        }
    }

    private void cleanUpMediaPlayer() {
        if (mediaPlayer != null) {
            try {
                if (!error) {
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.stop();
                    }
                    mediaPlayer.reset();
                }

                mediaPlayer.setOnCompletionListener(null);
                mediaPlayer.setOnErrorListener(null);
                mediaPlayer.setOnPreparedListener(null);
                mediaPlayer.setOnVideoSizeChangedListener(null);
                mediaPlayer.release();
            } catch (Exception e) {
                Logger.log(e);
            }
            mediaPlayer = null;
        }
    }

    private void tryPlayVideo() {
        if (mediaPlayer == null) {
            createMediaPlayer();
        }
        if (!mediaPlayerPrepared) {
            prepareMediaPlayer();
        }
        if (isMediaPlayerAvailable() && !mediaPlayer.isPlaying()
                && mediaPlayerPrepared && viewOnScreen && isAdOnScreen()) {
            state = State.PLAYING;
            updateViewState();
            mediaPlayer.start();
            notifyVideoStarted();
            if (videoVisibilityCheckerTimer == null) {
                startVideoVisibilityCheckerTimer();
            }
        }
    }

    private void updateViewState() {
        switch (state) {
            case IMAGE:
                if (imageView != null) {
                    imageView.setVisibility(VISIBLE);
                    imageView.bringToFront();
                }
                if (hasVideo) {
                    textureView.setVisibility(INVISIBLE);
                    progressBarView.setVisibility(INVISIBLE);
                    playButton.setVisibility(INVISIBLE);
                    muteButton.setVisibility(INVISIBLE);
                }
                break;
            case LOADING:
                if (imageView != null) {
                    imageView.setVisibility(VISIBLE);
                    imageView.bringToFront();
                }
                if (hasVideo) {
                    progressBarView.setVisibility(VISIBLE);
                    progressBarView.bringToFront();
                    textureView.setVisibility(INVISIBLE);
                    playButton.setVisibility(INVISIBLE);
                    muteButton.setVisibility(INVISIBLE);
                }
                break;
            case PLAYING:
                if (imageView != null) {
                    imageView.setVisibility(INVISIBLE);
                }
                if (hasVideo) {
                    textureView.setVisibility(VISIBLE);
                    textureView.bringToFront();
                    muteButton.setVisibility(VISIBLE);
                    muteButton.bringToFront();
                    updateMuteButton();
                    progressBarView.setVisibility(INVISIBLE);
                    playButton.setVisibility(INVISIBLE);
                }
                break;
            case PAUSED:
                if (imageView != null) {
                    imageView.setVisibility(VISIBLE);
                    imageView.bringToFront();
                }
                if (hasVideo) {
                    playButton.setVisibility(VISIBLE);
                    playButton.bringToFront();
                    textureView.setVisibility(INVISIBLE);
                    progressBarView.setVisibility(INVISIBLE);
                    muteButton.setVisibility(INVISIBLE);
                }
            default:
                break;
        }
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        final int measWidth = MeasureSpec.getSize(widthMeasureSpec);
        final int measHeight = MeasureSpec.getSize(heightMeasureSpec);

        final int curWidth = getMeasuredWidth();
        final int curHeight = getMeasuredHeight();

        int finalWidth;
        if (widthMode == MeasureSpec.EXACTLY) {
            finalWidth = measWidth;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            // Cap width at max width.
            finalWidth = Math.min(measWidth, curWidth);
        } else {
            // MeasWidth is meaningless. Stay with current width.
            finalWidth = curWidth;
        }
        // Set height based on width + height constraints.
        int finalHeight = (int) (ASPECT_MULTIPLIER_WIDTH_TO_HEIGHT * finalWidth);
        // Check if the layout is giving us bounds smaller than we want, conform to those if needed.
        if (heightMode == MeasureSpec.EXACTLY && measHeight < finalHeight) {
            finalHeight = measHeight;
            finalWidth = (int) (ASPECT_MULTIPLIER_HEIGHT_TO_WIDTH * finalHeight);
        }
        if (Math.abs(finalHeight - curHeight) >= 2
                || Math.abs(finalWidth - curWidth) >= 2) {
//           Logger.log(String.format("Resetting mediaLayout size to w: %d h: %d", finalWidth, finalHeight));
            getLayoutParams().width = finalWidth;
            getLayoutParams().height = finalHeight;
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }


    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
        try {
            if (mediaPlayer == null) {
                createMediaPlayer();
            }
            Surface surface = new Surface(surfaceTexture);
            mediaPlayer.setSurface(surface);
            prepareMediaPlayer();
        } catch (Exception e) {
            Logger.log(e);
            state = State.IMAGE;
            updateViewState();
            hasVideo = false;
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        videoFinished();
    }

    private void videoFinished() {
        notifyVideoFinished();
        stopVideoVisibilityCheckerTimer();
        pausePlayer();
        if (isMediaPlayerAvailable()) {
            mediaPlayer.seekTo(0);
        }
        finishedOrExpanded = true;
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Logger.log("MediaView onError");
        clearPlayerOnError();
        return true;
    }

    private void clearPlayerOnError() {
        startPlayVideoWhenReady = false;
        cleanUpMediaPlayer();
        state = State.IMAGE;
        updateViewState();
        stopVideoVisibilityCheckerTimer();
        error = true;
        hasVideo = false;
        processErrorEvent();
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        Logger.log("MediaView onPrepared");
        mediaPlayerPrepared = true;
        if (startPlayVideoWhenReady) {
            tryPlayVideo();
        } else {
            state = State.PAUSED;
            updateViewState();
        }
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
        ViewGroup.LayoutParams videoParams = textureView.getLayoutParams();
        int currentWidth = getWidth();
        int currentHeight = getHeight();
        if (width > height) {
            videoParams.width = currentWidth;
            videoParams.height = currentWidth * height / width;
        } else {
            videoParams.width = currentHeight * width / height;
            videoParams.height = currentHeight;
        }
        textureView.setLayoutParams(videoParams);
    }

    public void onViewAppearOnScreen() {
        Logger.log("MediaView onViewAppearOnScreen");
        viewOnScreen = true;
        if (startPlayVideoWhenReady) {
            tryPlayVideo();
        } else {
            if (state != State.LOADING) {
                state = State.PAUSED;
                updateViewState();
            }
        }
    }

    private boolean isMediaPlayerAvailable() {
        return !error && mediaPlayer != null;
    }

    private void notifyVideoStarted() {
        if (!isVideoStartNotified) {
            processImpressions();
            isVideoStartNotified = true;
            Logger.log("MediaView video started");
        }
    }

    private void notifyVideoFinished() {
        if (!isVideoFinishNotified) {
            processEvent(TrackingEvent.complete);
            isVideoFinishNotified = true;
            Logger.log("MediaView video finished");
        }
    }

    private void createMuteButton() {
        muteButton = new CircleCountdownView(getContext(), Assets.mainAssetsColor, Assets.backgroundColor);
        int muteButtonSize = Math.round(50 * getScreenDensity(getContext()));
        LayoutParams muteButtonParams = new LayoutParams(muteButtonSize, muteButtonSize);
        muteButtonParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        muteButtonParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        muteButton.setLayoutParams(muteButtonParams);
        updateMuteButton();
        muteButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isMediaPlayerAvailable()) {
                    if (isMuted) {
                        mediaPlayer.setVolume(1, 1);
                        isMuted = false;
                    } else {
                        mediaPlayer.setVolume(0, 0);
                        isMuted = true;
                    }
                    updateMuteButton();
                }
            }
        });
        addView(muteButton);
    }

    private void updateVolume() {
        if (isMediaPlayerAvailable()) {
            if (isMuted) {
                mediaPlayer.setVolume(0, 0);
            } else {
                mediaPlayer.setVolume(1, 1);
            }
        }
    }

    private void updateMuteButton() {
        if (muteButton != null) {
            if (isMuted) {
                muteButton.setImage(Assets.getBitmapFromBase64(Assets.unmute));
            } else {
                muteButton.setImage(Assets.getBitmapFromBase64(Assets.mute));
            }
        }
    }

    private boolean isAdOnScreen() {
        Rect viewRect = new Rect();
        boolean isAdVisible = getGlobalVisibleRect(viewRect);
        boolean isAdShown = isShown();
        boolean windowHasFocus = hasWindowFocus();
        return isAdVisible && isAdShown && windowHasFocus;
    }

    public void startVideoVisibilityCheckerTimer() {
        if (!hasVideo) {
            return;
        }
        videoVisibilityCheckerTimer = new Timer();
        int VIDEO_VISIBILITY_CHECKER_TIMER_INTERVAL = 500;
        videoVisibilityCheckerTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (error) {
                    Utils.onUiThread(new Runnable() {
                        public void run() {
                            clearPlayerOnError();
                        }
                    });
                    return;
                }
                if (!isAdOnScreen()) {
                    //Logger.log("MediaView out of screen");
                    Utils.onUiThread(new Runnable() {
                        public void run() {
                            pausePlayer();
                            if (finishedOrExpanded) {
                                stopVideoVisibilityCheckerTimer();
                            }
                        }
                    });
                } else {
                    try {
                        if (isMediaPlayerAvailable() && !error && mediaPlayer.isPlaying()) {
                            if (videoDuration == 0) {
                                videoDuration = mediaPlayer.getDuration();
                            }
                            if (videoDuration != 0) {
                                int curPos = mediaPlayer.getCurrentPosition();
                                int percentage = 100 * curPos / videoDuration;
                                if (percentage >= 25 * quartile) {
                                    if (quartile == 0) {
                                        Logger.log(String.format("Video started: %s%%", percentage));
                                        processEvent(TrackingEvent.start);
                                    } else if (quartile == 1) {
                                        Logger.log(String.format("Video at first quartile: %s%%", percentage));
                                        processEvent(TrackingEvent.firstQuartile);
                                    } else if (quartile == 2) {
                                        Logger.log(String.format("Video at midpoint: %s%%", percentage));
                                        processEvent(TrackingEvent.midpoint);
                                    } else if (quartile == 3) {
                                        Logger.log(String.format("Video at third quartile: %s%%", percentage));
                                        processEvent(TrackingEvent.thirdQuartile);
                                    }
                                    quartile++;
                                }
                            }
                        }
                        Logger.log("MediaView on screen");
                        Utils.onUiThread(new Runnable() {
                            public void run() {
                                tryPlayVideo();
                            }
                        });
                    } catch (IllegalStateException e) {
                        Utils.onUiThread(new Runnable() {
                            @Override
                            public void run() {
                                clearPlayerOnError();
                            }
                        });
                    }
                }
            }
        }, 0, VIDEO_VISIBILITY_CHECKER_TIMER_INTERVAL);
    }

    public void stopVideoVisibilityCheckerTimer() {
        if (videoVisibilityCheckerTimer != null) {
            videoVisibilityCheckerTimer.cancel();
            videoVisibilityCheckerTimer = null;
        }
    }

    private void executeTask(Runnable task) {
        NativeNetworkExecutor.getInstance().execute(task);
    }

    private void processImpressions() {
        if (vastRequest != null && vastRequest.getVastAd() != null) {
            List<String> impressions = vastRequest.getVastAd().getImpressionUrlList();
            fireUrls(impressions);
        }
    }

    private void processEvent(TrackingEvent eventName) {
        if (vastRequest != null && vastRequest.getVastAd() != null) {
            Map<TrackingEvent, List<String>> trackingUrls =
                    vastRequest.getVastAd().getTrackingEventListMap();
            List<String> urls = trackingUrls.get(eventName);
            fireUrls(urls);
        }
        if (eventName == TrackingEvent.complete && nativeInteractor != null) {
            nativeInteractor.dispatchVideoPlayFinished();
        }
    }

    private void fireUrls(List<String> urls) {
        if (urls != null) {
            for (String url : urls) {
                Utils.httpGetURL(url, NativeNetworkExecutor.getInstance());
            }
        }
    }

    private void processErrorEvent() {
        if (vastRequest != null) {
            vastRequest.sendError(VastError.ERROR_CODE_ERROR_SHOWING);
        }
    }

    @Override
    public void videoPlayerActivityClosed(int position, boolean finished) {
        Logger.log(String.format("MediaView videoPlayerActivityClosed, position: %s, finished: %s", position, finished));
        try {
            if (finished) {
                videoFinished();
            } else if (isMediaPlayerAvailable()) {
                mediaPlayer.seekTo(position);
            }
        } catch (Exception e) {
            Logger.log(e);
        }
        listener = null;
    }

}
