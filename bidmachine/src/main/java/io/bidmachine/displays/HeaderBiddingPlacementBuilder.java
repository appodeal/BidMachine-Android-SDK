package io.bidmachine.displays;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.explorestack.protobuf.Any;
import com.explorestack.protobuf.InvalidProtocolBufferException;
import com.explorestack.protobuf.Message;
import com.explorestack.protobuf.adcom.Ad;
import com.explorestack.protobuf.openrtb.Response;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import io.bidmachine.AdContentType;
import io.bidmachine.AdsType;
import io.bidmachine.BidMachineEvents;
import io.bidmachine.ContextProvider;
import io.bidmachine.HeaderBiddingAdRequestParams;
import io.bidmachine.HeaderBiddingAdapter;
import io.bidmachine.HeaderBiddingCollectParamsCallback;
import io.bidmachine.NetworkAdapter;
import io.bidmachine.NetworkConfig;
import io.bidmachine.TrackEventInfo;
import io.bidmachine.TrackEventType;
import io.bidmachine.TrackingObject;
import io.bidmachine.core.Logger;
import io.bidmachine.models.AdObjectParams;
import io.bidmachine.protobuf.headerbidding.HeaderBiddingAd;
import io.bidmachine.protobuf.headerbidding.HeaderBiddingPlacement;
import io.bidmachine.unified.UnifiedAdRequestParams;
import io.bidmachine.utils.BMError;

class HeaderBiddingPlacementBuilder<UnifiedAdRequestParamsType extends UnifiedAdRequestParams> {

    private static final long HEADER_BIDDING_PREPARE_TIMEOUT_SEC = 10;

    Message.Builder createPlacement(@NonNull ContextProvider contextProvider,
                                    @NonNull UnifiedAdRequestParamsType adRequestParams,
                                    @NonNull AdsType adsType,
                                    @NonNull AdContentType adContentType,
                                    @NonNull Collection<NetworkConfig> networkConfigs) {
        List<AdUnitPreloadTask> preloadTasks = new ArrayList<>();
        for (NetworkConfig networkConfig : networkConfigs) {
            NetworkAdapter adapter = networkConfig.obtainNetworkAdapter();
            if (adapter instanceof HeaderBiddingAdapter) {
                Map<String, String> mediationConfig =
                        networkConfig.peekMediationConfig(adsType, adRequestParams, adContentType);
                if (mediationConfig != null) {
                    preloadTasks.add(
                            new AdUnitPreloadTask<>(
                                    contextProvider,
                                    (HeaderBiddingAdapter) adapter,
                                    adsType,
                                    adContentType,
                                    adRequestParams,
                                    mediationConfig));
                }
            }
        }
        if (!preloadTasks.isEmpty()) {
            TrackingObject trackingObject = new TrackingObject() {
                private String key = UUID.randomUUID().toString();

                @Override
                public Object getTrackingKey() {
                    return key;
                }
            };
            BidMachineEvents.eventStart(
                    trackingObject,
                    TrackEventType.HeaderBiddingNetworksPrepare,
                    adsType);
            try {
                CountDownLatch syncLock = new CountDownLatch(preloadTasks.size());
                for (AdUnitPreloadTask task : preloadTasks) {
                    task.execute(syncLock);
                }
                try {
                    syncLock.await(HEADER_BIDDING_PREPARE_TIMEOUT_SEC, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Logger.log(e);
                }
                List<HeaderBiddingPlacement.AdUnit> adUnitList = null;
                for (AdUnitPreloadTask task : preloadTasks) {
                    HeaderBiddingPlacement.AdUnit adUnit = task.getAdUnit();
                    if (adUnit != null) {
                        if (adUnitList == null) {
                            adUnitList = new ArrayList<>();
                        }
                        adUnitList.add(adUnit);
                    } else if (!task.isFinished()) {
                        // In case when we reach this block after lock timeout - cancel tasks which are not finished yet
                        task.cancel();
                    }
                }
                if (adUnitList != null && !adUnitList.isEmpty()) {
                    HeaderBiddingPlacement.Builder placementBuilder = HeaderBiddingPlacement.newBuilder();
                    placementBuilder.addAllAdUnits(adUnitList);
                    return placementBuilder;
                }
            } finally {
                BidMachineEvents.eventFinish(
                        trackingObject,
                        TrackEventType.HeaderBiddingNetworksPrepare,
                        adsType,
                        null);
            }
        }
        return null;
    }

    AdObjectParams createAdObjectParams(@NonNull ContextProvider contextProvider,
                                        @NonNull UnifiedAdRequestParamsType adRequestParams,
                                        @NonNull Response.Seatbid seatbid,
                                        @NonNull Response.Seatbid.Bid bid,
                                        @NonNull Ad ad) {
        HeaderBiddingAd headerBiddingAd = null;
        if (ad.hasDisplay()) {
            if (ad.getDisplay().hasBanner()) {
                headerBiddingAd = obtainHeaderBiddingAd(ad.getDisplay().getBanner().getExtList());
            }
            if (headerBiddingAd == null && ad.getDisplay().hasNative()) {
                headerBiddingAd = obtainHeaderBiddingAd(ad.getDisplay().getNative().getExtList());
            }
        }
        if (headerBiddingAd == null && ad.hasVideo()) {
            headerBiddingAd = obtainHeaderBiddingAd(ad.getVideo().getExtList());
        }
        return headerBiddingAd != null
                ? new HeaderBiddingAdObjectParams(seatbid, bid, ad, headerBiddingAd)
                : null;
    }

    @Nullable
    private HeaderBiddingAd obtainHeaderBiddingAd(@NonNull List<Any> extensions) {
        for (Any extension : extensions) {
            if (extension.is(HeaderBiddingAd.class)) {
                try {
                    return extension.unpack(HeaderBiddingAd.class);
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    private static final class AdUnitPreloadTask<UnifiedAdRequestParamsType extends UnifiedAdRequestParams>
            implements Runnable, HeaderBiddingAdRequestParams, HeaderBiddingCollectParamsCallback {

        private static Executor executor = Executors.newFixedThreadPool(
                Runtime.getRuntime().availableProcessors() * 2);

        @NonNull
        private ContextProvider contextProvider;
        @NonNull
        private HeaderBiddingAdapter adapter;
        @NonNull
        private AdsType adsType;
        @NonNull
        private AdContentType adContentType;
        @NonNull
        private UnifiedAdRequestParamsType adRequestParams;
        @NonNull
        private Map<String, String> mediationConfig;

        private CountDownLatch syncLock;
        private HeaderBiddingPlacement.AdUnit adUnit;

        private boolean isFinished = false;

        private final TrackingObject trackingObject = new TrackingObject() {
            private String key = UUID.randomUUID().toString();

            @Override
            public Object getTrackingKey() {
                return key;
            }
        };

        AdUnitPreloadTask(@NonNull ContextProvider contextProvider,
                          @NonNull HeaderBiddingAdapter adapter,
                          @NonNull AdsType adsType,
                          @NonNull AdContentType adContentType,
                          @NonNull UnifiedAdRequestParamsType adRequestParams,
                          @NonNull Map<String, String> mediationConfig) {
            this.contextProvider = contextProvider;
            this.adapter = adapter;
            this.adsType = adsType;
            this.adContentType = adContentType;
            this.adRequestParams = adRequestParams;
            this.mediationConfig = mediationConfig;
        }

        @Override
        @NonNull
        public AdsType getAdsType() {
            return adsType;
        }

        @Override
        @NonNull
        public AdContentType getAdContentType() {
            return adContentType;
        }

        @Override
        public void run() {
            adapter.collectHeaderBiddingParams(contextProvider,
                                               adRequestParams,
                                               this,
                                               this,
                                               mediationConfig);
        }

        @Override
        public void onCollectFinished(@Nullable Map<String, String> params) {
            if (isFinished) {
                return;
            }
            HeaderBiddingPlacement.AdUnit.Builder builder = HeaderBiddingPlacement.AdUnit.newBuilder();
            builder.setBidder(adapter.getKey());
            builder.setBidderSdkver(adapter.getVersion());
            builder.putAllClientParams(params);
            adUnit = builder.build();
            Logger.log(String.format("%s: %s: Header bidding collect finished",
                                     adapter.getKey(),
                                     adsType));
            finish();
            BidMachineEvents.eventFinish(
                    trackingObject,
                    TrackEventType.HeaderBiddingNetworkPrepare,
                    adsType,
                    null);
        }

        @Override
        public void onCollectFail(@Nullable BMError error) {
            if (isFinished) {
                return;
            }
            if (error != null) {
                Logger.log(String.format("%s: Header bidding collect fail: %s",
                                         adapter.getKey(),
                                         error.getMessage()));
            }
            finish();
            BidMachineEvents.eventFinish(
                    trackingObject,
                    TrackEventType.HeaderBiddingNetworkPrepare,
                    adsType,
                    error);
        }

        void execute(@NonNull CountDownLatch syncLock) {
            BidMachineEvents.eventStart(
                    trackingObject,
                    TrackEventType.HeaderBiddingNetworkPrepare,
                    new TrackEventInfo()
                            .withParameter("HB_NETWORK", adapter.getKey())
                            .withParameter("BM_AD_TYPE", adsType.getName()),
                    adsType);
            this.syncLock = syncLock;
            executor.execute(this);
        }

        void cancel() {
            if (isFinished) {
                return;
            }
            Logger.log(String.format("%s: Header bidding collect fail: timeout", adapter.getKey()));
            finish();
        }

        HeaderBiddingPlacement.AdUnit getAdUnit() {
            return adUnit;
        }

        boolean isFinished() {
            return isFinished;
        }

        private void finish() {
            isFinished = true;
            syncLock.countDown();
        }
    }

}
