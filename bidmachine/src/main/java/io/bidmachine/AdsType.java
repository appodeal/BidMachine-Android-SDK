package io.bidmachine;

import android.graphics.Point;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.explorestack.protobuf.adcom.Ad;
import com.explorestack.protobuf.openrtb.Response;
import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import io.bidmachine.adapters.mraid.MraidAdapter;
import io.bidmachine.adapters.nast.NastAdapter;
import io.bidmachine.adapters.vast.VastAdapter;
import io.bidmachine.banner.BannerSize;
import io.bidmachine.core.Logger;
import io.bidmachine.displays.DisplayPlacementBuilder;
import io.bidmachine.displays.NativePlacementBuilder;
import io.bidmachine.displays.PlacementBuilder;
import io.bidmachine.displays.VideoPlacementBuilder;
import io.bidmachine.models.AdObjectParams;
import io.bidmachine.protobuf.headerbidding.HeaderBiddingAd;
import io.bidmachine.unified.UnifiedAdRequestParams;
import io.bidmachine.unified.UnifiedBannerAdRequestParams;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public enum AdsType {

    Banner(new ApiRequest.ApiAuctionDataBinder(),
            new PlacementBuilder[]{
                    new DisplayPlacementBuilder<UnifiedBannerAdRequestParams>(false, true) {
                        @Override
                        public Point getSize(ContextProvider contextProvider, UnifiedBannerAdRequestParams bannerRequest) {
                            BannerSize bannerSize = bannerRequest.getBannerSize();
                            return new Point(bannerSize.width, bannerSize.height);
                        }
                    }}),
    Interstitial(new ApiRequest.ApiAuctionDataBinder(),
            new PlacementBuilder[]{
                    new DisplayPlacementBuilder(true, true),
                    new VideoPlacementBuilder(true, true)}),
    Rewarded(new ApiRequest.ApiAuctionDataBinder(),
            new PlacementBuilder[]{
                    new DisplayPlacementBuilder(true, true),
                    new VideoPlacementBuilder(false, true)}),
    Native(new ApiRequest.ApiAuctionDataBinder(),
            new PlacementBuilder[]{
                    new NativePlacementBuilder(false)});

    private final ApiRequest.ApiAuctionDataBinder binder;
    private final PlacementBuilder[] placementBuilders;
    private final Map<String, NetworkConfig> networkConfigs = new HashMap<>();
    private final Executor placementCreateExecutor = Executors.newFixedThreadPool(Math.max(8, Runtime.getRuntime().availableProcessors() * 4));

    AdsType(@NonNull ApiRequest.ApiAuctionDataBinder binder,
            @NonNull PlacementBuilder[] placementBuilders) {
        this.binder = binder;
        this.placementBuilders = placementBuilders;
    }

    NetworkConfig obtainNetworkConfig(@NonNull ContextProvider contextProvider,
                                      @NonNull UnifiedAdRequestParams adRequestParams,
                                      @NonNull Ad ad) {
        NetworkConfig networkConfig = obtainHeaderBiddingAdNetworkConfig(contextProvider, adRequestParams, ad);
        if (networkConfig == null) {
            if (this == AdsType.Native) {
                networkConfig = obtainNetworkConfig(contextProvider, adRequestParams, NetworkRegistry.Nast);
            } else if (ad.hasDisplay()) {
                networkConfig = obtainNetworkConfig(contextProvider, adRequestParams, NetworkRegistry.Mraid);
            } else if (ad.hasVideo()) {
                networkConfig = obtainNetworkConfig(contextProvider, adRequestParams, NetworkRegistry.Vast);
            }
        }
        return networkConfig;
    }

    @Nullable
    private NetworkConfig obtainHeaderBiddingAdNetworkConfig(@NonNull ContextProvider contextProvider,
                                                             @NonNull UnifiedAdRequestParams adRequestParams,
                                                             @NonNull Ad ad) {
        List<Any> extensions = null;
        if (ad.hasDisplay()) {
            Ad.Display display = ad.getDisplay();
            if (display.hasBanner()) {
                extensions = display.getBanner().getExtList();
            } else if (display.hasNative()) {
                extensions = display.getNative().getExtList();
            }
        }
        if ((extensions == null || extensions.isEmpty()) && ad.hasVideo()) {
            extensions = ad.getVideo().getExtList();
        }
        if (extensions != null) {
            return obtainHeaderBiddingAdNetworkConfig(contextProvider, adRequestParams, extensions);
        }
        return null;
    }

    @Nullable
    private NetworkConfig obtainHeaderBiddingAdNetworkConfig(@NonNull ContextProvider contextProvider,
                                                             @NonNull UnifiedAdRequestParams adRequestParams,
                                                             @NonNull List<Any> extensions) {
        for (Any extension : extensions) {
            if (extension.is(HeaderBiddingAd.class)) {
                try {
                    HeaderBiddingAd headerBiddingAd = extension.unpack(HeaderBiddingAd.class);
                    return obtainNetworkConfig(contextProvider, adRequestParams, headerBiddingAd.getBidder());
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    private NetworkConfig obtainNetworkConfig(@NonNull ContextProvider contextProvider,
                                              @NonNull UnifiedAdRequestParams adRequestParams,
                                              @NonNull String networkName) {
        NetworkConfig networkConfig = NetworkRegistry.getConfig(networkName);
        if (networkConfig != null) {
            try {
                networkConfig.getAdapter().initialize(contextProvider, adRequestParams, networkConfig.getNetworkConfig());
            } catch (Throwable throwable) {
                Logger.log(throwable);
                networkConfig = null;
            }
        }
        return networkConfig;
    }

    ApiRequest.ApiAuctionDataBinder getBinder() {
        return binder;
    }

    @SuppressWarnings("unchecked")
    AdObjectParams createAdObjectParams(@NonNull ContextProvider contextProvider,
                                        @NonNull Response.Seatbid seatbid,
                                        @NonNull Response.Seatbid.Bid bid,
                                        @NonNull Ad ad,
                                        @Deprecated AdRequest adRequest) {
        for (PlacementBuilder builder : placementBuilders) {
            AdObjectParams params = builder.createAdObjectParams(
                    contextProvider, adRequest.getUnifiedRequestParams(), seatbid, bid, ad);
            if (params != null) {
                return params;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    void collectDisplayPlacements(final ContextProvider contextProvider,
                                  final AdRequest adRequest,
                                  final ArrayList<Message.Builder> outList) {
        final CountDownLatch syncLock = new CountDownLatch(placementBuilders.length);
        for (final PlacementBuilder placementBuilder : placementBuilders) {
            if (adRequest.isPlacementBuilderMatch(placementBuilder)) {
                placementCreateExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        placementBuilder.createPlacement(
                                contextProvider,
                                adRequest.getUnifiedRequestParams(),
                                AdsType.this,
                                networkConfigs.values(),
                                new PlacementBuilder.PlacementCreateCallback() {
                                    @Override
                                    public void onCreated(@Nullable Message.Builder placement) {
                                        if (placement != null) {
                                            synchronized (outList) {
                                                outList.add(placement);
                                            }
                                        }
                                        syncLock.countDown();
                                    }
                                });
                    }
                });
            } else {
                syncLock.countDown();
            }
        }
        try {
            syncLock.await();
        } catch (InterruptedException e) {
            Logger.log(e);
        }
    }

    static {
        NetworkRegistry.registerNetworks(
                new NetworkConfig(new MraidAdapter()) {
                },
                new NetworkConfig(new NastAdapter()) {
                },
                new NetworkConfig(new VastAdapter()) {
                });
    }

    static class NetworkRegistry {

        static final String Mraid = "mraid";
        static final String Vast = "vast";
        static final String Nast = "nast";

        private static final HashMap<String, NetworkConfig> cache = new HashMap<>();

        @Nullable
        static NetworkConfig getConfig(String key) {
            return cache.get(key);
        }

        static void registerNetworks(NetworkConfig... networkConfigs) {
            for (NetworkConfig config : networkConfigs) {
                if (!cache.containsKey(config.getKey())) {
                    cache.put(config.getKey(), config);
                }
                for (AdsType type : config.getSupportedAdsTypes()) {
                    type.networkConfigs.put(config.getKey(), config);
                }
            }
        }

        static void setLoggingEnabled(boolean enabled) {
            for (Map.Entry<String, NetworkConfig> entry : cache.entrySet()) {
                entry.getValue().getAdapter().setLogging(enabled);
            }
        }
    }

}


