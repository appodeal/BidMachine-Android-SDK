package io.bidmachine.displays;

import android.content.Context;
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

    Message.Builder createPlacement(@NonNull Context context,
                                    @NonNull UnifiedAdRequestParamsType adRequestParams,
                                    @NonNull AdsType adsType,
                                    @NonNull AdContentType contentType,
                                    @NonNull Collection<NetworkConfig> networkConfigs) {
        List<AdUnitPreloadTask> preloadTasks = new ArrayList<>();
        for (NetworkConfig networkConfig : networkConfigs) {
            BidMachineAdapter adapter = networkConfig.getAdapter();
            if (adapter instanceof HeaderBiddingAdapter) {
                Map<String, Object> mediationConfig = networkConfig.peekMediationConfig(adsType, contentType);
                if (mediationConfig != null) {
                    preloadTasks.add(
                            new AdUnitPreloadTask<>(
                                    context, (HeaderBiddingAdapter) adapter, adRequestParams, mediationConfig));
                }
            }
        }
        if (!preloadTasks.isEmpty()) {
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
        }
        return null;
    }

    AdObjectParams createAdObjectParams(@NonNull Context context,
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
        private Context context;
        @NonNull
        private HeaderBiddingAdapter adapter;
        @NonNull
        private UnifiedAdRequestParamsType adRequestParams;
        @NonNull
        private Map<String, Object> mediationConfig;

        private CountDownLatch syncLock;
        private HeaderBiddingPlacement.AdUnit adUnit;

        AdUnitPreloadTask(@NonNull Context context,
                          @NonNull HeaderBiddingAdapter adapter,
                          @NonNull UnifiedAdRequestParamsType adRequestParams,
                          @NonNull Map<String, Object> mediationConfig) {
            this.context = context;
            this.adapter = adapter;
            this.adRequestParams = adRequestParams;
            this.mediationConfig = mediationConfig;
        }

        @Override
        public void run() {
            adapter.collectHeaderBiddingParams(context, adRequestParams, this, mediationConfig);
        }

        @Override
        public void onCollectFinished(@Nullable HashMap<String, String> params) {
            HeaderBiddingPlacement.AdUnit.Builder builder = HeaderBiddingPlacement.AdUnit.newBuilder();
            builder.setBidder(adapter.getKey());
            builder.setBidderSdkver(adapter.getVersion());
            builder.putAllClientParams(params);
            adUnit = builder.build();
            syncLock.countDown();
        }

        @Override
        public void onCollectFail(@Nullable BMError error) {
            if (error != null) {
                Logger.log("Header bidding collect fail: " + error.getMessage());
            }
            syncLock.countDown();
        }

        void execute(@NonNull CountDownLatch syncLock) {
            this.syncLock = syncLock;
            executor.execute(this);
        }

        HeaderBiddingPlacement.AdUnit getAdUnit() {
            return adUnit;
        }
    }

}
