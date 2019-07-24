package io.bidmachine;

import android.content.Context;
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

public enum AdsType {

    Banner(new ApiRequest.ApiAuctionDataBinder(),
            new PlacementBuilder[]{
                    new DisplayPlacementBuilder<UnifiedBannerAdRequestParams>(false, true) {
                        @Override
                        public Point getSize(Context context, UnifiedBannerAdRequestParams bannerRequest) {
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

    AdsType(@NonNull ApiRequest.ApiAuctionDataBinder binder,
            @NonNull PlacementBuilder[] placementBuilders) {
        this.binder = binder;
        this.placementBuilders = placementBuilders;
    }

    NetworkConfig obtainNetworkConfig(@NonNull Context context,
                                      @NonNull Ad ad,
                                      @NonNull UnifiedAdRequestParams adRequestParams) {
        NetworkConfig networkConfig = obtainHeaderBiddingAdNetworkConfig(context, ad, adRequestParams);
        if (networkConfig == null) {
            if (this == AdsType.Native) {
                networkConfig = obtainNetworkConfig(context, AdapterRegistry.Nast, adRequestParams);
            } else if (ad.hasDisplay()) {
                networkConfig = obtainNetworkConfig(context, AdapterRegistry.Mraid, adRequestParams);
            } else if (ad.hasVideo()) {
                networkConfig = obtainNetworkConfig(context, AdapterRegistry.Vast, adRequestParams);
            }
        }
        return networkConfig;
    }

    @Nullable
    private NetworkConfig obtainHeaderBiddingAdNetworkConfig(@NonNull Context context,
                                                             @NonNull Ad ad,
                                                             @NonNull UnifiedAdRequestParams adRequestParams) {
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
            return obtainHeaderBiddingAdNetworkConfig(context, adRequestParams, extensions);
        }
        return null;
    }

    @Nullable
    private NetworkConfig obtainHeaderBiddingAdNetworkConfig(@NonNull Context context,
                                                             @NonNull UnifiedAdRequestParams adRequestParams,
                                                             @NonNull List<Any> extensions) {
        for (Any extension : extensions) {
            if (extension.is(HeaderBiddingAd.class)) {
                try {
                    HeaderBiddingAd headerBiddingAd = extension.unpack(HeaderBiddingAd.class);
                    return obtainNetworkConfig(context, headerBiddingAd.getBidder(), adRequestParams);
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    private NetworkConfig obtainNetworkConfig(@NonNull Context context,
                                              @NonNull String networkName,
                                              @NonNull UnifiedAdRequestParams adRequestParams) {
        NetworkConfig networkConfig = AdapterRegistry.getConfig(networkName);
        if (networkConfig != null) {
            try {
                networkConfig.getAdapter().initialize(context, adRequestParams, networkConfig.getNetworkConfig());
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
    AdObjectParams createAdObjectParams(@NonNull Context context,
                                        @NonNull Response.Seatbid seatbid,
                                        @NonNull Response.Seatbid.Bid bid,
                                        @NonNull Ad ad,
                                        @Deprecated AdRequest adRequest) {
        for (PlacementBuilder builder : placementBuilders) {
            AdObjectParams params = builder.createAdObjectParams(
                    context, adRequest.getUnifiedRequestParams(), seatbid, bid, ad);
            if (params != null) {
                return params;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    void collectDisplayPlacements(Context context, AdRequest adRequest, ArrayList<Message.Builder> outList) {
        for (PlacementBuilder placementBuilder : placementBuilders) {
            if (adRequest.isPlacementBuilderMatch(placementBuilder)) {
                Message.Builder buildResult = placementBuilder.createPlacement(
                        context, adRequest.getUnifiedRequestParams(), this, networkConfigs.values());
                if (buildResult != null) {
                    outList.add(buildResult);
                }
            }
        }
    }

    static {
        AdapterRegistry.registerNetworks(
                new NetworkConfig(new MraidAdapter()) {
                },
                new NetworkConfig(new NastAdapter()) {
                },
                new NetworkConfig(new VastAdapter()) {
                });
    }

    static class AdapterRegistry {

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
                BidMachineAdapter adapter = config.getAdapter();
                if (!cache.containsKey(adapter.getKey())) {
                    cache.put(adapter.getKey(), config);
                }
                for (AdsType type : config.getSupportedAdsTypes()) {
                    type.networkConfigs.put(adapter.getKey(), config);
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


