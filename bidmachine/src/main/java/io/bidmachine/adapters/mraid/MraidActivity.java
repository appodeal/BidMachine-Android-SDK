package io.bidmachine.adapters.mraid;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import org.nexage.sourcekit.util.Utils;
import org.nexage.sourcekit.util.Video;
import org.nexage.sourcekit.vast.view.CircleCountdownView;

import io.bidmachine.utils.BMError;

public class MraidActivity extends Activity {

    private static final int CLOSE_REGION_SIZE = 50;

    private static MraidFullScreenAdObject mraidInterstitial;

    private ProgressBar progressBar;
    private CircleCountdownView circleCountdownView;

    private Handler handler;
    private Runnable showCloseTime;
    private boolean canSkip;

    public static void show(Context context, MraidFullScreenAdObject mraidInterstitial, Video.Type adType) {
        MraidActivity.mraidInterstitial = mraidInterstitial;
        try {
            Intent adActivityIntent = new Intent(context, MraidActivity.class);
            adActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            adActivityIntent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            adActivityIntent.putExtra("type", adType);
            context.startActivity(adActivityIntent);
        } catch (Exception e) {
            e.printStackTrace();
            if (mraidInterstitial != null && mraidInterstitial.getAd() != null) {
                mraidInterstitial.processLoadFail(BMError.Internal);
            }
            MraidActivity.mraidInterstitial = null;
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            if (!getIntent().hasExtra("type")) {
                finishActivity();
                return;
            }
            Video.Type type = (Video.Type) getIntent().getSerializableExtra("type");
            //noinspection ResourceType
            setRequestedOrientation(Utils.getScreenOrientation(this));
            if (type == Video.Type.NON_REWARDED) {
                canSkip = true;
            }
            hideTitleStatusBars();
            addBackgroundView();
            if (mraidInterstitial != null) {
                mraidInterstitial.setShowingActivity(this);
                showMraidInterstitial();
                if (!mraidInterstitial.getParams().canPreload()) {
                    mraidInterstitial.getAdapterListener().setAfterStartShowRunnable(new Runnable() {
                        @Override
                        public void run() {
                            hideProgressBarWithCloseTime();
                        }
                    });
                    showProgressBarWithCloseTime(mraidInterstitial.getSkipAfterTimeSec());
                }
            }
        } catch (Exception e) {
            if (mraidInterstitial != null) {
                mraidInterstitial.processLoadFail(BMError.Internal);
            }
            finishActivity();
        }
    }

    @Override
    public void onBackPressed() {
        if (canSkip) {
            finishActivity();
        }
    }

    @Override
    protected void onDestroy() {
        if (mraidInterstitial != null) {
            mraidInterstitial.setShowingActivity(null);
            verifyClosedDispatched(mraidInterstitial);
            if (mraidInterstitial.getMraidInterstitial() != null) {
                mraidInterstitial.getMraidInterstitial().destroy();
            }
            mraidInterstitial = null;
        }
        super.onDestroy();
    }

    private void showMraidInterstitial() {
        if (mraidInterstitial != null && mraidInterstitial.getMraidInterstitial() != null) {
            mraidInterstitial.getMraidInterstitial().show(this);
        }
    }

    public void showProgressBar() {
        progressBar = new ProgressBar(this);
        FrameLayout.LayoutParams spinnerParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        spinnerParams.gravity = Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL;
        progressBar.setLayoutParams(spinnerParams);
        progressBar.setBackgroundColor(Color.TRANSPARENT);
        addContentView(progressBar, spinnerParams);
    }

    public void showProgressBarWithCloseTime(long closeTimeSec) {
        showProgressBar();
        int size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, CLOSE_REGION_SIZE,
                getResources().getDisplayMetrics());
        circleCountdownView = new CircleCountdownView(this);
        circleCountdownView.setBackgroundColor(Color.TRANSPARENT);
        circleCountdownView.setVisibility(View.GONE);
        circleCountdownView.setImage(org.nexage.sourcekit.util.Assets.getBitmapFromBase64(org.nexage.sourcekit.util.Assets.close));
        circleCountdownView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mraidInterstitial != null) {
                    mraidInterstitial.processShowFail(BMError.TimeoutError);
                }
                finishActivity();
            }
        });
        FrameLayout.LayoutParams spinnerParams = new FrameLayout.LayoutParams(size, size);
        spinnerParams.gravity = Gravity.TOP | Gravity.END;
        addContentView(circleCountdownView, spinnerParams);
        showCloseTime = new Runnable() {
            @Override
            public void run() {
                if (circleCountdownView != null) {
                    circleCountdownView.setVisibility(View.VISIBLE);
                    circleCountdownView.setClickable(true);
                }
            }
        };
        handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(showCloseTime, closeTimeSec * 1000);
    }

    public void hideProgressBar() {
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
    }

    public void hideProgressBarWithCloseTime() {
        hideProgressBar();
        if (handler != null && showCloseTime != null) {
            handler.removeCallbacks(showCloseTime);
        }
        if (circleCountdownView != null) {
            circleCountdownView.setVisibility(View.GONE);
        }
    }

    private void finishActivity() {
        finish();
        overridePendingTransition(0, 0);
        if (mraidInterstitial != null) {
            mraidInterstitial.setShowingActivity(null);
            verifyClosedDispatched(mraidInterstitial);
            mraidInterstitial = null;
        }
    }

    private void hideTitleStatusBars() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    private void addBackgroundView() {
        RelativeLayout mainView = new RelativeLayout(this);
        RelativeLayout.LayoutParams mainViewParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mainView.setLayoutParams(mainViewParams);
        mainView.setBackgroundColor(Color.parseColor("#7F000000"));
        setContentView(mainView);
    }

    private synchronized void verifyClosedDispatched(@Nullable MraidFullScreenAdObject mraidInterstitial) {
        if (mraidInterstitial != null
                && mraidInterstitial.getMraidInterstitial() != null
                && !mraidInterstitial.getMraidInterstitial().isClosed()) {
            mraidInterstitial.getMraidInterstitial().mraidViewClose(null);
        }
    }
}