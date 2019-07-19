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

    NetworkConfig obtainNetworkConfig(@NonNull Context context, @NonNull Ad ad) {
        NetworkConfig networkConfig = obtainHeaderBiddingAdNetworkConfig(context, ad);
        if (networkConfig == null) {
            if (this == AdsType.Native) {
                networkConfig = obtainNetworkConfig(context, AdapterRegistry.Nast);
            } else if (ad.hasDisplay()) {
                networkConfig = obtainNetworkConfig(context, AdapterRegistry.Mraid);
            } else if (ad.hasVideo()) {
                networkConfig = obtainNetworkConfig(context, AdapterRegistry.Vast);
            }
        }
        return networkConfig;
    }

    @Nullable
    private NetworkConfig obtainHeaderBiddingAdNetworkConfig(@NonNull Context context, @NonNull Ad ad) {
        NetworkConfig result = null;
        if (ad.hasDisplay()) {
            result = obtainHeaderBiddingAdNetworkConfig(context, ad.getDisplay().getExtList());
        }
        if (result == null && ad.hasVideo()) {
            result = obtainHeaderBiddingAdNetworkConfig(context, ad.getVideo().getExtList());
        }
        return result;
    }

    @Nullable
    private NetworkConfig obtainHeaderBiddingAdNetworkConfig(@NonNull Context context, @NonNull List<Any> extensions) {
        for (Any extension : extensions) {
            if (extension.is(HeaderBiddingAd.class)) {
                try {
                    HeaderBiddingAd headerBiddingAd = extension.unpack(HeaderBiddingAd.class);
                    return obtainNetworkConfig(context, headerBiddingAd.getBidder());
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    private NetworkConfig obtainNetworkConfig(@NonNull Context context, @NonNull String networkName) {
        NetworkConfig networkConfig = AdapterRegistry.getConfig(networkName);
        if (networkConfig != null) {
            try {
                networkConfig.getAdapter().initialize(context, networkConfig.getNetworkConfig());
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
        AdapterRegistry.registerAdapter(new NetworkConfig(new MraidAdapter()));
        AdapterRegistry.registerAdapter(new NetworkConfig(new VastAdapter()));
        AdapterRegistry.registerAdapter(new NetworkConfig(new NastAdapter()));
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

        static void registerAdapter(NetworkConfig networkConfig) {
            BidMachineAdapter adapter = networkConfig.getAdapter();
            if (!cache.containsKey(adapter.getKey())) {
                cache.put(adapter.getKey(), networkConfig);
            }
            for (AdsType type : networkConfig.getSupportedAdsTypes()) {
                type.networkConfigs.put(adapter.getKey(), networkConfig);
            }
        }

        static void setLoggingEnabled(boolean enabled) {
            for (Map.Entry<String, NetworkConfig> entry : cache.entrySet()) {
                entry.getValue().getAdapter().setLogging(enabled);
            }
        }
    }

}


