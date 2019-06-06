package io.bidmachine;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;

import java.net.URLConnection;
import java.util.List;

import io.bidmachine.core.Logger;
import io.bidmachine.core.NetworkRequest;
import io.bidmachine.utils.BMError;

abstract class SessionTracker {

    abstract String getSessionId();

    abstract void trackEventStart(@Nullable TrackingObject trackingObject,
                                  @Nullable TrackEventType trackEventType,
                                  @Nullable AdsType adsType);

    abstract void trackEventFinish(@Nullable TrackingObject trackingObject,
                                   @Nullable TrackEventType trackEventType,
                                   @Nullable AdsType adsType,
                                   @Nullable BMError error);

    abstract void clearTrackingEvent(@Nullable TrackingObject trackingObject,
                                     @Nullable TrackEventType trackEventType);

    abstract void clearTrackers(@Nullable TrackingObject trackingObject);

    abstract int getEventCount(@NonNull AdsType adsType, @Nullable TrackEventType trackEventType);

    abstract int getTotalEventCount(@Nullable TrackEventType eventType);

    static void eventStart(@Nullable TrackingObject trackingObject,
                           @Nullable TrackEventType trackEventType,
                           @Nullable AdsType adsType) {
        SessionTrackerImpl sessionTracker = BidMachineImpl.get().getSessionTracker();
        if (sessionTracker != null) {
            sessionTracker.trackEventStart(trackingObject, trackEventType, adsType);
        }
    }

    static void eventFinish(@Nullable TrackingObject trackingObject,
                            @Nullable TrackEventType trackEventType,
                            @Nullable AdsType adsType,
                            @Nullable BMError error) {
        SessionTrackerImpl sessionTracker = BidMachineImpl.get().getSessionTracker();
        if (sessionTracker != null) {
            sessionTracker.trackEventFinish(trackingObject, trackEventType, adsType, error);
        }
    }

    static void clearEvent(@Nullable TrackingObject trackingObject,
                           @Nullable TrackEventType trackEventType) {
        SessionTrackerImpl sessionTracker = BidMachineImpl.get().getSessionTracker();
        if (sessionTracker != null) {
            sessionTracker.clearTrackingEvent(trackingObject, trackEventType);
        }
    }

    static void clear(@Nullable TrackingObject trackingObject) {
        SessionTrackerImpl sessionTracker = BidMachineImpl.get().getSessionTracker();
        if (sessionTracker != null) {
            sessionTracker.clearTrackers(trackingObject);
        }
    }

    static void notifyTrack(@NonNull TrackingObject trackingObject,
                            @NonNull TrackEventType eventType,
                            @Nullable TrackEventInfo eventInfo,
                            @Nullable BMError error) {
        if (error != null) {
            notifyError(trackingObject.getTrackingUrls(TrackEventType.Error),
                    trackingObject.getTrackingUrls(TrackEventType.TrackingError),
                    eventInfo,
                    eventType.getOrtbActionValue(),
                    error);
        } else {
            notifyTrack(
                    trackingObject.getTrackingUrls(eventType),
                    trackingObject.getTrackingUrls(TrackEventType.TrackingError),
                    eventInfo,
                    eventType);
        }
    }

    private static void notifyTrack(@Nullable List<String> urls,
                                    @Nullable final List<String> trackErrorUrls,
                                    @Nullable final TrackEventInfo eventInfo,
                                    @NonNull final TrackEventType eventType) {
        if (urls == null) {
            return;
        }
        Logger.log("dispatch event to server: " + eventType);
        for (String url : urls) {
            executeNotify(replaceMacros(url, eventInfo, eventType.getOrtbActionValue(), -1),
                    new NetworkRequest.Callback<String, BMError>() {
                        @Override
                        public void onSuccess(@Nullable String result) {
                            //ignore
                        }

                        @Override
                        public void onFail(@Nullable BMError result) {
                            if (result == null) result = BMError.Internal;
                            notifyTrackingError(trackErrorUrls, eventInfo, eventType.getOrtbActionValue(), result);
                        }
                    });
        }

    }

    private static void notifyError(@Nullable List<String> urls,
                                    @Nullable final List<String> trackErrorUrls,
                                    @Nullable final TrackEventInfo info,
                                    int processCode,
                                    @NonNull BMError error) {
        if (urls == null) {
            return;
        }
        if (error.getCode() == BMError.NOT_SET) {
            return;
        }
        Logger.log("dispatch error event to server: (" + error.getCode() + ")" + error.getMessage());
        for (String url : urls) {
            executeNotify(replaceMacros(url, info, processCode, error.getCode()), new NetworkRequest.Callback<String, BMError>() {
                @Override
                public void onSuccess(@Nullable String s) {
                    //ignore
                }

                @Override
                public void onFail(@Nullable BMError result) {
                    if (result == null) result = BMError.Internal;
                    notifyTrackingError(trackErrorUrls, info, TrackEventType.Error.getOrtbActionValue(), result);
                }
            });
        }
    }

    private static void notifyTrackingError(@Nullable List<String> urls,
                                            @Nullable TrackEventInfo eventInfo,
                                            int processCode,
                                            @NonNull BMError error) {
        if (urls == null) {
            return;
        }
        if (error.getCode() == BMError.NOT_SET) {
            return;
        }
        Logger.log("dispatch tracking fail to server: (" + error.getCode() + ")" + error.getMessage());
        for (String url : urls) {
            executeNotify(replaceMacros(url, eventInfo, processCode, error.getCode()), null);
        }
    }

    @Nullable
    @VisibleForTesting
    static String replaceMacros(@Nullable String url,
                                @Nullable TrackEventInfo info,
                                int processCode,
                                int errorCode) {
        if (TextUtils.isEmpty(url)) return null;
        assert url != null;
        String outUrl = url
                .replace("${BM_EVENT_CODE}", String.valueOf(processCode))
                .replace("%24%7BBM_EVENT_CODE%7D", String.valueOf(processCode))
                .replace("${BM_ACTION_CODE}", String.valueOf(processCode))
                .replace("%24%7BBM_ACTION_CODE%7D", String.valueOf(processCode))
                .replace("${BM_ERROR_REASON}", String.valueOf(errorCode))
                .replace("%24%7BBM_ERROR_REASON%7D", String.valueOf(errorCode));
        if (info != null) {
            outUrl = outUrl
                    .replace("${BM_ACTION_START}", String.valueOf(info.startTimeMs))
                    .replace("%24%7BBM_ACTION_START%7D", String.valueOf(info.startTimeMs))
                    .replace("${BM_ACTION_FINISH}", String.valueOf(info.finishTimeMs))
                    .replace("%24%7BBM_ACTION_FINISH%7D", String.valueOf(info.finishTimeMs));
        }
        return outUrl;
    }

    private static void executeNotify(@Nullable String url,
                                      @Nullable NetworkRequest.Callback<String, BMError> callback) {
        if (TextUtils.isEmpty(url)) return;
        new ApiRequest.Builder<Object, String>()
                .url(url)
                .setMethod(NetworkRequest.Method.Get)
                .setDataBinder(new ApiRequest.ApiDataBinder<Object, String>() {
                    @Override
                    protected void prepareHeaders(NetworkRequest<Object, String, BMError> request,
                                                  URLConnection connection) {
                        connection.addRequestProperty("Accept", "application/json");
                        connection.addRequestProperty("Content-Type", "application/json");
                    }

                    @Nullable
                    @Override
                    protected byte[] obtainData(NetworkRequest<Object, String, BMError> request,
                                                URLConnection connection,
                                                @Nullable Object requestData) throws Exception {
                        return null;
                    }

                    @Override
                    protected String createSuccessResult(NetworkRequest<Object, String, BMError> request,
                                                         URLConnection connection,
                                                         byte[] resultData) throws Exception {
                        return null;
                    }
                })
                .setCallback(callback)
                .request();
    }

}
