package io.bidmachine.ads.networks.criteo;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.net.ssl.HttpsURLConnection;

import io.bidmachine.AdsType;
import io.bidmachine.BMLog;
import io.bidmachine.ContextProvider;
import io.bidmachine.NetworkAdapter;
import io.bidmachine.NetworkConfigParams;
import io.bidmachine.models.TargetingInfo;
import io.bidmachine.unified.UnifiedAdRequestParams;

class CriteoAdapter extends NetworkAdapter {

    private static final String TAG = "CriteoAdapter";

    private static final String EVENT_LAUNCH = "Launch";
    private static final String EVENT_ACTIVE = "Active";
    private static final String EVENT_INACTIVE = "Inactive";

    private static final Executor networkExecutor = Executors.newFixedThreadPool(2);

    @Nullable
    private String senderId;
    @Nullable
    private TargetingInfo targetingInfo;
    private volatile long nextValidRequestTime;

    CriteoAdapter() {
        super("criteo", BuildConfig.VERSION_NAME, BuildConfig.VERSION_NAME, new AdsType[]{});
    }

    @Override
    protected void onInitialize(@NonNull ContextProvider contextProvider,
                                @NonNull UnifiedAdRequestParams adRequestParams,
                                @NonNull NetworkConfigParams networkConfigParams) {
        super.onInitialize(contextProvider, adRequestParams, networkConfigParams);
        Map<String, String> params = networkConfigParams.obtainNetworkParams();
        if (params != null) {
            senderId = params.get(CriteoConfig.SENDER_ID);
            if (senderId == null) {
                Log.e(TAG, "Initialize failed: sender_id not provided");
                return;
            }
            targetingInfo = adRequestParams.getTargetingParams();
            ((Application) contextProvider.getContext().getApplicationContext())
                    .registerActivityLifecycleCallbacks(lifecycleCallbacks);
            sendRequest(contextProvider.getContext(), EVENT_LAUNCH);
        }
    }

    private boolean maySendRequest(@NonNull Context context, @NonNull TargetingInfo targetingInfo) {
        boolean mayByThrottle = nextValidRequestTime == 0
                || System.currentTimeMillis() > nextValidRequestTime;
        return !TextUtils.isEmpty(senderId)
                && !TextUtils.isEmpty(targetingInfo.getHttpAgent(context))
                && mayByThrottle;
    }

    private URL getUrl(@NonNull Context context,
                       @NonNull String eventType,
                       @NonNull TargetingInfo targetingInfo) throws Exception {
        String url = String.format(Locale.ENGLISH,
                                   "https://gum.criteo.com/appevent/v1/%s?gaid=%s&appId=%s&eventType=%s&limitedAdTracking=%d",
                                   senderId,
                                   targetingInfo.getIfa(context),
                                   context.getPackageName(),
                                   eventType,
                                   targetingInfo.isLimitAdTrackingEnabled() ? 1 : 0);
        return new URL(url);
    }

    private void sendRequest(@NonNull final Context context, @NonNull final String eventType) {
        BMLog.log(TAG, String.format("Sending event: %s", eventType));
        if (targetingInfo == null || !maySendRequest(context, targetingInfo)) {
            BMLog.log(TAG, "Event sending consumed");
            return;
        }
        networkExecutor.execute(new Runnable() {
            @Override
            public void run() {
                HttpsURLConnection urlConnection = null;
                InputStream inputStream;
                try {
                    urlConnection = (HttpsURLConnection) getUrl(context,
                                                                eventType,
                                                                targetingInfo).openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setRequestProperty("User-Agent",
                                                     targetingInfo.getHttpAgent(context));
                    urlConnection.setConnectTimeout(10000);
                    urlConnection.setReadTimeout(10000);

                    int responseCode = urlConnection.getResponseCode();
                    if (responseCode == HttpsURLConnection.HTTP_OK) {
                        long time = 0;
                        inputStream = urlConnection.getInputStream();
                        JSONObject response = getResponse(inputStream);
                        if (response != null && response.has("throttleSec")) {
                            int throttleSec = response.getInt("throttleSec");
                            time = System.currentTimeMillis() + throttleSec * 1000;
                        }
                        nextValidRequestTime = time;
                    } else if (responseCode == HttpsURLConnection.HTTP_BAD_REQUEST) {
                        inputStream = urlConnection.getErrorStream();
                        JSONObject response = getResponse(inputStream);
                        if (response != null && response.has("error")) {
                            BMLog.log(TAG,
                                      String.format(Locale.ENGLISH,
                                                    "Error: %s",
                                                    response.getString("error")));
                        }
                    }
                } catch (Exception e) {
                    BMLog.log(e);
                } finally {
                    try {
                        if (urlConnection != null) {
                            urlConnection.disconnect();
                        }
                    } catch (Exception ignore) {
                    }
                }
            }
        });
    }

    private JSONObject getResponse(InputStream inputStream) {
        BufferedReader reader = null;
        try {
            StringBuilder builder = new StringBuilder(inputStream.available());
            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append('\n');
            }
            if (builder.length() > 0) {
                builder.setLength(builder.length() - 1);
            }
            return new JSONObject(builder.toString());
        } catch (Exception e) {
            BMLog.log(e);
            return null;
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (Exception ignore) {
            }
        }
    }

    private final Application.ActivityLifecycleCallbacks lifecycleCallbacks = new Application.ActivityLifecycleCallbacks() {

        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            //ignore
        }

        @Override
        public void onActivityStarted(Activity activity) {
            //ignore
        }

        @Override
        public void onActivityResumed(Activity activity) {
            sendRequest(activity, EVENT_ACTIVE);
        }

        @Override
        public void onActivityPaused(Activity activity) {
            sendRequest(activity, EVENT_INACTIVE);
        }

        @Override
        public void onActivityStopped(Activity activity) {
            //ignore
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
            //ignore
        }

        @Override
        public void onActivityDestroyed(Activity activity) {
            //ignore
        }
    };
}
