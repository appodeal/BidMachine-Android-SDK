package org.nexage.sourcekit.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import org.nexage.sourcekit.mraid.MRAIDView;

import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.util.List;


public class Utils {
    public static Pair<Integer, Integer> getScreenSize(Context context) {
        WindowManager window = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = window.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return new Pair<>(size.x, size.y);
    }

    public static boolean isTablet(Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        double width = metrics.widthPixels / metrics.xdpi;
        double height = metrics.heightPixels / metrics.ydpi;
        Double screenSize = Math.sqrt(width * width + height * height);
        return screenSize >= 6.6d;
    }

    public static float getScreenDensity(Context context) {
        WindowManager window = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = window.getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);
        return displayMetrics.density;
    }

    public static void runOnUiThread(Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
    }

    public static void runOnUiThread(Runnable runnable, long delayMillis) {
        new Handler(Looper.getMainLooper()).postDelayed(runnable, delayMillis);
    }

    public static boolean openBrowser(final Context context, String url, final Runnable finishRunnable) {
        final String validUrl = getValidUrl(url);
        if (isHttpUrl(validUrl)) {
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    launchUrl(context, findEndpoint(validUrl));
                    if (finishRunnable != null) {
                        Utils.runOnUiThread(finishRunnable);
                    }
                }
            }.start();
            return true;
        } else {
            if (finishRunnable != null) {
                Utils.runOnUiThread(finishRunnable);
            }
            return launchUrl(context, validUrl);
        }
    }

    public static String getValidUrl(String urlString) {
        try {
            //noinspection UnusedAssignment
            URL testUrl = new URL(urlString);
            return urlString;
        } catch (MalformedURLException e) {
            try {
                urlString = URLDecoder.decode(urlString, "UTF-8");
                return urlString;
            } catch (UnsupportedEncodingException ex) {
                return urlString;
            } catch (IllegalArgumentException exception) {
                return urlString;
            }
        }
    }

    public static boolean isHttpUrl(String url) {
        return url.startsWith("http://") || url.startsWith("https://");
    }

    private static boolean launchUrl(Context context, String url) {
        try {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            browserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ComponentName componentName = pickBrowser(context, browserIntent);
            if (componentName != null) {
                browserIntent.setComponent(componentName);
                context.startActivity(browserIntent);
                return true;
            } else {
                url = URLDecoder.decode(url, "UTF-8");
                browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                browserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                componentName = pickBrowser(context, browserIntent);
                if (componentName != null) {
                    browserIntent.setComponent(componentName);
                    context.startActivity(browserIntent);
                    return true;
                } else {
                    Log.e("Appodealx", String.format("No activities to handle intent: %s", url));
                }
            }
        } catch (Exception e) {
            Log.w("Appodealx", e);
        }
        return false;
    }

    static String findEndpoint(String urlString) {
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setInstanceFollowRedirects(false);
            urlConnection.setConnectTimeout(500);
            urlConnection.setReadTimeout(500);
            switch (urlConnection.getResponseCode()) {
                case HttpURLConnection.HTTP_MOVED_PERM:
                case HttpURLConnection.HTTP_MOVED_TEMP:
                case HttpURLConnection.HTTP_SEE_OTHER:
                case HttpURLConnection.HTTP_USE_PROXY:
                case 307:
                    String nextUrl = urlConnection.getHeaderField("Location");
                    if (nextUrl != null) {
                        if (isHttpUrl(nextUrl)) {
                            return findEndpoint(urlConnection.getHeaderField("Location"));
                        } else if (new URI(nextUrl).getScheme() == null) {
                            try {
                                String localNextUrl = new URL(url, nextUrl).toString();
                                if (localNextUrl.trim().length() > 0) {
                                    nextUrl = localNextUrl;
                                } else {
                                    return nextUrl;
                                }
                            } catch (Exception e) {
                                Log.w("AppodealX", e);
                                return nextUrl;
                            }
                            return findEndpoint(nextUrl);
                        } else {
                            return nextUrl;
                        }
                    } else {
                        return url.toString();
                    }
                default:
                    return url.toString();
            }
        } catch (Exception e) {
            Log.w("AppodealX", e);
            return urlString;
        } finally {
            if (urlConnection != null) {
                try {
                    urlConnection.disconnect();
                } catch (Exception e) {
                    Log.w("AppodealX", e);
                }
            }
        }
    }

    public static ComponentName pickBrowser(Context context, Intent browserIntent) {
        PackageManager manager = context.getPackageManager();
        List<ResolveInfo> intentActivities = manager.queryIntentActivities(browserIntent, 0);
        if (!intentActivities.isEmpty()) {
            for (ResolveInfo resolveInfo : intentActivities) {
                if (resolveInfo.activityInfo.packageName.equals("com.android.vending")) {
                    return new ComponentName(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name);
                }
            }
            return new ComponentName(intentActivities.get(0).activityInfo.packageName, intentActivities.get(0).activityInfo.name);
        } else {
            return null;
        }
    }

    public static int getScreenOrientation(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        int rotation = windowManager.getDefaultDisplay().getRotation();
        int orientation = context.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            switch (rotation) {
                case Surface.ROTATION_180:
                case Surface.ROTATION_270:
                    return ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;

                case Surface.ROTATION_0:
                case Surface.ROTATION_90:
                default:
                    return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
            }
        } else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            switch (rotation) {
                case Surface.ROTATION_180:
                case Surface.ROTATION_270:
                    return ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;

                case Surface.ROTATION_0:
                case Surface.ROTATION_90:
                default:
                    return ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
            }
        } else {
            return ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
        }
    }

    public static void addBannerSpinnerView(View view) {
        if (view == null || !(view instanceof MRAIDView)) {
            return;
        }

        ProgressBar spinnerView = new ProgressBar(view.getContext());
        RelativeLayout.LayoutParams spinnerParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        spinnerParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        spinnerView.setLayoutParams(spinnerParams);
        spinnerView.setBackgroundColor(Color.TRANSPARENT);
        spinnerView.setTag("Appodeal Spinner View");

        ((MRAIDView) view).addView(spinnerView);
    }

    public static void hideBannerSpinnerView(final View view) {
        if (view == null || !(view instanceof MRAIDView)) {
            return;
        }

        Utils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    int childCount = ((MRAIDView) view).getChildCount();
                    for (int i = 0; i < childCount; i++) {
                        View child = ((MRAIDView) view).getChildAt(i);
                        Object tagObj = child.getTag();
                        if (tagObj != null && tagObj.equals("Appodeal Spinner View")) {
                            child.setVisibility(View.GONE);
                        }
                    }
                } catch (Exception e) {
                    Log.w("AppodealX", e);
                }
            }
        });
    }

}
