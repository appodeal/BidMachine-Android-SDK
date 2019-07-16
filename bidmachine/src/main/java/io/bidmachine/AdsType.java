package io.bidmachine;

import android.content.Context;
import android.graphics.Point;
import android.support.annotation.NonNull;
import com.explorestack.protobuf.adcom.Ad;
import com.explorestack.protobuf.openrtb.Response;
import com.google.protobuf.Message;
import io.bidmachine.adapters.OrtbAdapter;
import io.bidmachine.adapters.mraid.MraidAdapter;
import io.bidmachine.adapters.nast.NastAdapter;
import io.bidmachine.adapters.vast.VastAdapter;
import io.bidmachine.banner.BannerRequest;
import io.bidmachine.banner.BannerSize;
import io.bidmachine.core.Logger;
import io.bidmachine.displays.*;
import io.bidmachine.models.AdObject;
import io.bidmachine.models.AdObjectParams;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public enum AdsType {

    Banner(new ApiRequest.ApiAuctionDataBinder(),
            new String[]{AdapterRegistry.Mraid},
            new PlacementBuilder[]{new DisplayPlacementBuilder<BannerRequest>(false) {
                @Override
                public Point getSize(Context context, BannerRequest bannerRequest) {
                    BannerSize bannerSize = bannerRequest.getSize();
                    return new Point(bannerSize.width, bannerSize.height);
                }

                @Override
                public AdObjectParams createAdObjectParams(@NonNull Context context,
                                                           @NonNull BannerRequest adRequest,
                                                           @NonNull Response.Seatbid seatbid,
                                                           @NonNull Response.Seatbid.Bid bid,
                                                           @NonNull Ad ad) {
                    //TODO пересмотреть место проверки т.к. валидность проверяется в AdObjectParams.isValid()
                    AdObjectParams params = super.createAdObjectParams(context, adRequest, seatbid, bid, ad);
                    if (params.getWidth() > 0 && params.getHeight() > 0) {
                        return params;
                    }
                    return null;
                }
            }}),

    Interstitial(new ApiRequest.ApiAuctionDataBinder(),
            new String[]{
                    AdapterRegistry.Mraid,
                    AdapterRegistry.Vast},
            new PlacementBuilder[]{
                    new DisplayPlacementBuilder(true),
                    new VideoPlacementBuilder(true)}),

    Rewarded(new ApiRequest.ApiAuctionDataBinder(),
            new String[]{
                    AdapterRegistry.Mraid,
                    AdapterRegistry.Vast},
            new PlacementBuilder[]{new DisplayPlacementBuilder(true),
                    new VideoPlacementBuilder(false)}),

    Native(new ApiRequest.ApiAuctionDataBinder(),
            new String[]{AdapterRegistry.Nast},
            new PlacementBuilder[]{new NativePlacementBuilder()});

    private final ApiRequest.ApiAuctionDataBinder binder;
    private final PlacementBuilder[] placementBuilders;
    private final Map<String, OrtbAdapter> adapters = new HashMap<>();

    AdsType(@NonNull ApiRequest.ApiAuctionDataBinder binder,
            @NonNull String[] adaptersNames,
            @NonNull PlacementBuilder[] placementBuilders) {
        this.binder = binder;
        for (String adapterName : adaptersNames) {
            adapters.put(adapterName, AdapterRegistry.findAdapter(adapterName));
        }
        this.placementBuilders = placementBuilders;
    }

    OrtbAdapter findAdapter(@NonNull Context context, @NonNull Ad ad) {
        switch (this) {
            case Native: {
                return findAdapter(context, AdapterRegistry.Nast);
            }
            default: {
                if (ad.hasDisplay()) {
                    return findAdapter(context, AdapterRegistry.Mraid);
                } else if (ad.hasVideo()) {
                    return findAdapter(context, AdapterRegistry.Vast);
                }
                return null;
            }
        }
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
            if (builder.isMatch(ad)) {
                return builder.createAdObjectParams(context, adRequest, seatbid, bid, ad);
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    OrtbAdapter findAdapter(Context context, String key) {
        OrtbAdapter adapter = adapters.get(key);
        if (adapter != null) {
            try {
                adapter.initialize(context);
            } catch (Throwable throwable) {
                Logger.log(throwable);
                adapter = null;
            }
        }
        return adapter;
    }

    @SuppressWarnings("unchecked")
    void collectDisplayPlacements(Context context, AdRequest adRequest, ArrayList<Message.Builder> outList) {
        for (OrtbAdapter adapter : adapters.values()) {
            for (PlacementBuilder builder : placementBuilders) {
                if (adRequest.isPlacementBuilderMatch(builder)) {
                    outList.add(builder.buildPlacement(context, adRequest, adapter));
                }
            }
        }
    }

    AdObject createAdObject(@NonNull OrtbAdapter adapter,
                            @NonNull AdObjectParams adObjectParams) {
        switch (this) {
            case Native:
                return adapter.createNativeAdObject((NativeAdObjectParams) adObjectParams);
            case Banner:
                return adapter.createBannerAdObject((DisplayAdObjectParams) adObjectParams);
            case Interstitial:
                return adapter.createInterstitialAdObject((FullScreenAdObjectParams) adObjectParams);
            case Rewarded:
                return adapter.createRewardedAdObject((FullScreenAdObjectParams) adObjectParams);
        }
        throw new IllegalArgumentException();
    }

    private static class AdapterRegistry {

        static final String Mraid = "mraid";
        static final String Vast = "vast";
        static final String Nast = "nast";

        private static final HashMap<String, OrtbAdapter> cache = new HashMap<>();

        static {
            cache.put(Mraid, new MraidAdapter());
            cache.put(Vast, new VastAdapter());
            cache.put(Nast, new NastAdapter());
        }

        static OrtbAdapter findAdapter(String key) {
            return cache.get(key);
        }

    }

}


