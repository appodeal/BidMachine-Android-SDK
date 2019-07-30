package io.bidmachine.displays;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.explorestack.protobuf.adcom.Ad;
import com.explorestack.protobuf.openrtb.Response;
import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import io.bidmachine.*;
import io.bidmachine.core.Logger;
import io.bidmachine.models.AdObjectParams;
import io.bidmachine.protobuf.headerbidding.HeaderBiddingAd;
import io.bidmachine.protobuf.headerbidding.HeaderBiddingPlacement;
import io.bidmachine.unified.UnifiedAdRequestParams;
import io.bidmachine.utils.BMError;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

class HeaderBiddingPlacementBuilder<UnifiedAdRequestParamsType extends UnifiedAdRequestParams> {

    Message.Builder createPlacement(@NonNull ContextProvider contextProvider,
                                    @NonNull UnifiedAdRequestParamsType adRequestParams,
                                    @NonNull final AdsType adsType,
                                    @NonNull final AdContentType contentType,
                                    @NonNull Collection<NetworkConfig> networkConfigs) {
        List<AdUnitPreloadTask> preloadTasks = new ArrayList<>();
        for (NetworkConfig networkConfig : networkConfigs) {
            NetworkAdapter adapter = networkConfig.getAdapter();
            if (adapter instanceof HeaderBiddingAdapter) {
                Map<String, String> mediationConfig = networkConfig.peekMediationConfig(adsType, contentType);
                if (mediationConfig != null) {
                    preloadTasks.add(new AdUnitPreloadTask<>(
                            contextProvider,
                            (HeaderBiddingAdapter) adapter,
                            adsType,
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
            try {
                BidMachineEvents.eventStart(
                        trackingObject,
                        TrackEventType.HeaderBiddingNetworksPrepare,
                        adsType);
                CountDownLatch syncLock = new CountDownLatch(preloadTasks.size());
                for (AdUnitPreloadTask task : preloadTasks) {
                    task.execute(syncLock);
                }
                try {
                    syncLock.await();
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
        return headerBiddingAd != null ? new HeaderBiddingAdObjectParams(seatbid, bid, ad, headerBiddingAd) : null;
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
            implements Runnable, HeaderBiddingCollectParamsCallback {

        private static Executor executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);

        @NonNull
        private ContextProvider contextProvider;
        @NonNull
        private HeaderBiddingAdapter adapter;
        @NonNull
        private AdsType adsType;
        @NonNull
        private UnifiedAdRequestParamsType adRequestParams;
        @NonNull
        private Map<String, String> mediationConfig;

        private CountDownLatch syncLock;
        private HeaderBiddingPlacement.AdUnit adUnit;

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
                          @NonNull UnifiedAdRequestParamsType adRequestParams,
                          @NonNull Map<String, String> mediationConfig) {
            this.contextProvider = contextProvider;
            this.adapter = adapter;
            this.adsType = adsType;
            this.adRequestParams = adRequestParams;
            this.mediationConfig = mediationConfig;
        }

        @Override
        public void run() {
            adapter.collectHeaderBiddingParams(contextProvider, adRequestParams, this, mediationConfig);
        }

        @Override
        public void onCollectFinished(@Nullable Map<String, String> params) {
            HeaderBiddingPlacement.AdUnit.Builder builder = HeaderBiddingPlacement.AdUnit.newBuilder();
            builder.setBidder(adapter.getKey());
            builder.setBidderSdkver(adapter.getVersion());
            builder.putAllClientParams(params);
            adUnit = builder.build();
            syncLock.countDown();
            BidMachineEvents.eventFinish(
                    trackingObject,
                    TrackEventType.HeaderBiddingNetworkPrepare,
                    adsType,
                    null);
        }

        @Override
        public void onCollectFail(@Nullable BMError error) {
            if (error != null) {
                Logger.log("Header bidding collect fail: " + error.getMessage());
            }
            syncLock.countDown();
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
                            .withParameter("BM_AD_TYPE", adsType.ordinal()),
                    adsType);
            this.syncLock = syncLock;
            executor.execute(this);
        }

        HeaderBiddingPlacement.AdUnit getAdUnit() {
            return adUnit;
        }
    }

}
