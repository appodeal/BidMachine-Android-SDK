package io.bidmachine;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;
import android.util.Base64;
import io.bidmachine.core.AdvertisingIdClientInfo;
import io.bidmachine.core.Logger;
import io.bidmachine.core.NetworkRequest;
import io.bidmachine.core.Utils;
import io.bidmachine.protobuf.InitRequest;
import io.bidmachine.protobuf.InitResponse;
import io.bidmachine.utils.ActivityHelper;
import io.bidmachine.utils.BMError;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

final class BidMachineImpl implements TrackingObject {

    @SuppressLint("StaticFieldLeak")
    private static volatile BidMachineImpl instance;

    static BidMachineImpl get() {
        if (instance == null) {
            synchronized (BidMachineImpl.class) {
                if (instance == null) {
                    instance = new BidMachineImpl();
                }
            }
        }
        return instance;
    }

    static {
        Logger.setTag("BidMachine");
        Logger.setMessageBuilder(new Logger.LoggerMessageBuilder() {
            @Override
            public String buildMessage(String origin) {
                if (get().isTestMode()) {
                    return "(TEST MODE) " + origin;
                }
                return origin;
            }
        });
    }

    @VisibleForTesting
    static String DEF_INIT_URL = BuildConfig.BM_API_URL + "/init";
    private static final String PREF_INIT_DATA = "initData";
    private static final String DEF_AUCTION_URL = BuildConfig.BM_API_URL + "/openrtb3/auction";

    @Nullable
    private Context appContext;
    @Nullable
    private SessionTrackerImpl sessionTracker;
    @Nullable
    private String sellerId;
    @NonNull
    private TargetingParams targetingParams = new TargetingParams();
    @NonNull
    private ExtraParams extraParams = new ExtraParams();
    @NonNull
    private UserRestrictionParams userRestrictionParams = new UserRestrictionParams();
    @NonNull
    private PriceFloorParams priceFloorParams =
            new PriceFloorParams()
                    .addPriceFloor(UUID.randomUUID().toString(), 0.01);
    @NonNull
    private DeviceParams deviceParams = new DeviceParams();

    private boolean isTestMode;
    private boolean isInitialized;

    ApiRequest<InitRequest, InitResponse> currentInitRequest;

    @VisibleForTesting
    String currentAuctionUrl = DEF_AUCTION_URL;
    @VisibleForTesting
    private Map<TrackEventType, List<String>> trackingEventTypes =
            new EnumMap<>(TrackEventType.class);

    private long initRequestDelayMs = 0;
    private static final long MIN_INIT_REQUEST_DELAY_MS = TimeUnit.SECONDS.toMillis(2);
    private static final long MAX_INIT_REQUEST_DELAY_MS = TimeUnit.SECONDS.toMillis(128);

    Activity topActivity;

    private final Runnable rescheduleInitRunnable = new Runnable() {
        @Override
        public void run() {
            requestInitData(appContext, sellerId);
        }
    };

    synchronized void initialize(Context context, String sellerId) {
        if (isInitialized) return;
        this.sellerId = sellerId;
        appContext = context.getApplicationContext();
        sessionTracker = new SessionTrackerImpl();
        AdvertisingIdClientInfo.executeTask(context, new AdvertisingIdClientInfo.Closure() {
            @Override
            public void executed(@NonNull AdvertisingIdClientInfo.AdvertisingProfile advertisingProfile) {
                AdvertisingPersonalData.setLimitAdTrackingEnabled(advertisingProfile.isLimitAdTrackingEnabled());
                AdvertisingPersonalData.setDeviceAdvertisingId(advertisingProfile.getId());
            }
        });
        loadStoredInitResponse(context);
        requestInitData(context, sellerId);
        topActivity = ActivityHelper.getTopActivity();
        ((Application) context.getApplicationContext())
                .registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks());
        isInitialized = true;
    }

    private void requestInitData(final Context context, String sellerId) {
        if (currentInitRequest != null) return;
        SessionTracker.eventStart(this, TrackEventType.InitLoading, null);
        currentInitRequest = new ApiRequest.Builder<InitRequest, InitResponse>()
                .url(DEF_INIT_URL)
                .setDataBinder(new ApiRequest.ApiInitDataBinder())
                .setRequestData(OrtbUtils.obtainInitRequest(context, sellerId, targetingParams, userRestrictionParams))
                .setCallback(new NetworkRequest.Callback<InitResponse, BMError>() {
                    @Override
                    public void onSuccess(@Nullable InitResponse result) {
                        currentInitRequest = null;
                        if (result != null) {
                            handleInitResponse(context, result);
                            storeInitResponse(context, result);
                        }
                        initRequestDelayMs = 0;
                        Utils.cancelBackgroundThreadTask(rescheduleInitRunnable);
                        SessionTracker.eventFinish(BidMachineImpl.this,
                                TrackEventType.InitLoading,
                                null,
                                null);
                    }

                    @Override
                    public void onFail(@Nullable BMError result) {
                        currentInitRequest = null;
                        if (initRequestDelayMs <= 0) {
                            initRequestDelayMs = MIN_INIT_REQUEST_DELAY_MS;
                        } else {
                            initRequestDelayMs *= 2;
                            if (initRequestDelayMs >= MAX_INIT_REQUEST_DELAY_MS) {
                                initRequestDelayMs = MAX_INIT_REQUEST_DELAY_MS;
                            }
                        }
                        Logger.log("reschedule init request (" + initRequestDelayMs + ")");
                        Utils.onBackgroundThread(rescheduleInitRunnable, initRequestDelayMs);
                        SessionTracker.eventFinish(
                                BidMachineImpl.this,
                                TrackEventType.InitLoading,
                                null,
                                result);
                    }
                })
                .request();
    }

    private void handleInitResponse(@NonNull Context context, @NonNull InitResponse response) {
        if (!TextUtils.isEmpty(response.getEndpoint())) {
            currentAuctionUrl = response.getEndpoint();
        }
        trackingEventTypes.clear();
        OrtbUtils.prepareEvents(trackingEventTypes, response.getEventList());
    }

    private void storeInitResponse(Context context, InitResponse response) {
        SharedPreferences preferences = context.getSharedPreferences("BidMachinePref", Context.MODE_PRIVATE);
        preferences.edit().putString(PREF_INIT_DATA, Base64.encodeToString(response.toByteArray(), Base64.DEFAULT)).apply();
    }

    private void loadStoredInitResponse(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("BidMachinePref", Context.MODE_PRIVATE);
        if (preferences.contains(PREF_INIT_DATA)) {
            try {
                InitResponse initResponse = InitResponse.parseFrom(
                        Base64.decode(preferences.getString(PREF_INIT_DATA, null), Base64.DEFAULT));
                handleInitResponse(context, initResponse);
            } catch (Exception ignore) {
                preferences.edit().remove(PREF_INIT_DATA).apply();
            }
        }
    }

    @Override
    public Object getTrackingKey() {
        return getClass().getSimpleName();
    }

    @Nullable
    @Override
    public List<String> getTrackingUrls(@NonNull TrackEventType eventType) {
        return trackingEventTypes.get(eventType);
    }

    boolean isInitialized() {
        return isInitialized;
    }

    void setTestMode(boolean testMode) {
        isTestMode = testMode;
    }

    boolean isTestMode() {
        return isTestMode;
    }

    @Nullable
    Context getAppContext() {
        return appContext;
    }

    @Nullable
    SessionTrackerImpl getSessionTracker() {
        return sessionTracker;
    }

    @Nullable
    String getSellerId() {
        return sellerId;
    }

    void setTargetingParams(@Nullable TargetingParams targetingParams) {
        this.targetingParams = targetingParams != null ? targetingParams : new TargetingParams();
    }

    @NonNull
    TargetingParams getTargetingParams() {
        return targetingParams;
    }

    void setExtraParams(@Nullable ExtraParams extraParams) {
        this.extraParams = extraParams != null ? extraParams : new ExtraParams();
    }

    @NonNull
    ExtraParams getExtraParams() {
        return extraParams;
    }

    @NonNull
    UserRestrictionParams getUserRestrictionParams() {
        return userRestrictionParams;
    }

    @NonNull
    PriceFloorParams getPriceFloorParams() {
        return priceFloorParams;
    }

    @NonNull
    DeviceParams getDeviceParams() {
        return deviceParams;
    }

    String getAuctionUrl() {
        return currentAuctionUrl;
    }

    static Activity getTopActivity() {
        return get().topActivity;
    }

}
