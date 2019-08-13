package io.bidmachine.displays;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.explorestack.protobuf.adcom.Ad;
import com.explorestack.protobuf.openrtb.Response;
import com.google.protobuf.Message;
import io.bidmachine.AdContentType;
import io.bidmachine.AdsType;
import io.bidmachine.ContextProvider;
import io.bidmachine.NetworkConfig;
import io.bidmachine.models.AdObjectParams;
import io.bidmachine.unified.UnifiedAdRequestParams;

import java.util.Collection;

public abstract class PlacementBuilder<UnifiedAdRequestParamsType extends UnifiedAdRequestParams> {

    private AdContentType contentType;
    @Nullable
    private HeaderBiddingPlacementBuilder<UnifiedAdRequestParamsType> headerBiddingPlacementBuilder;

    PlacementBuilder(AdContentType contentType, boolean supportHeaderBidding) {
        this.contentType = contentType;
        if (supportHeaderBidding) {
            headerBiddingPlacementBuilder = new HeaderBiddingPlacementBuilder<>();
        }
    }

    public AdContentType getAdContentType() {
        return contentType;
    }

    public abstract void createPlacement(@NonNull ContextProvider contextProvider,
                                         @NonNull UnifiedAdRequestParamsType adRequestParams,
                                         @NonNull AdsType adsType,
                                         @NonNull Collection<NetworkConfig> networkConfigs,
                                         @NonNull PlacementCreateCallback callback);

    public abstract AdObjectParams createAdObjectParams(@NonNull ContextProvider contextProvider,
                                                        @NonNull UnifiedAdRequestParamsType adRequest,
                                                        @NonNull Response.Seatbid seatbid,
                                                        @NonNull Response.Seatbid.Bid bid,
                                                        @NonNull Ad ad);

    Message.Builder createHeaderBiddingPlacement(@NonNull ContextProvider contextProvider,
                                                 @NonNull UnifiedAdRequestParamsType adRequestParams,
                                                 @NonNull AdsType adsType,
                                                 @NonNull Collection<NetworkConfig> networkConfigs) {
        return headerBiddingPlacementBuilder != null
                ? headerBiddingPlacementBuilder.createPlacement(contextProvider,
                                                                adRequestParams,
                                                                adsType,
                                                                getAdContentType(),
                                                                networkConfigs)
                : null;
    }

    AdObjectParams createHeaderBiddingAdObjectParams(@NonNull ContextProvider contextProvider,
                                                     @NonNull UnifiedAdRequestParamsType adRequest,
                                                     @NonNull Response.Seatbid seatbid,
                                                     @NonNull Response.Seatbid.Bid bid,
                                                     @NonNull Ad ad) {
        return headerBiddingPlacementBuilder != null
                ? headerBiddingPlacementBuilder.createAdObjectParams(contextProvider, adRequest, seatbid, bid, ad)
                : null;
    }

    public interface PlacementCreateCallback {

        void onCreated(@Nullable Message.Builder placement);

    }

}
