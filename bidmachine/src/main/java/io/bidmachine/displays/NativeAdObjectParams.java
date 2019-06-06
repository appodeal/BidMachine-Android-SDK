package io.bidmachine.displays;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import io.bidmachine.nativead.utils.NativePrivateData;
import io.bidmachine.protobuf.adcom.Ad;
import io.bidmachine.protobuf.openrtb.Response;
import io.bidmachine.TrackEventType;

public class NativeAdObjectParams extends DisplayAdObjectParams implements NativePrivateData {

    private String title;
    private String description;
    private String iconUrl;
    private String imageUrl;
    private String callToAction;
    private String sponsored;
    private String ageRestrictions;
    private String clickUrl;
    private String videoUrl;
    private String videoAdm;
    private float rating;

    NativeAdObjectParams(Response.Seatbid seatbid, Response.Seatbid.Bid bid, Ad ad) {
        super(seatbid, bid, ad);

        Ad.Display.Native ortbData = ad.getDisplay().getNative();
        Ad.Display.Native.LinkAsset linkAsset = ortbData.getLink();
        if (linkAsset != null && linkAsset != Ad.Display.Native.LinkAsset.getDefaultInstance()) {
            clickUrl = linkAsset.getUrl();
            for (int i = 0; i < linkAsset.getTrkrCount(); i++) {
                addEvent(TrackEventType.Click, linkAsset.getTrkr(i));
            }
        }
        for (Ad.Display.Native.Asset asset : ortbData.getAssetList()) {
            switch (asset.getId()) {
                case NativePlacementBuilder.TITLE_ASSET_ID: {
                    title = asset.getTitle().getText();
                    break;
                }
                case NativePlacementBuilder.ICON_ASSET_ID: {
                    iconUrl = asset.getImage().getUrl();
                    break;
                }
                case NativePlacementBuilder.IMAGE_ASSET_ID: {
                    imageUrl = asset.getImage().getUrl();
                    break;
                }
                case NativePlacementBuilder.DESC_ASSET_ID: {
                    description = asset.getData().getValue();
                    break;
                }
                case NativePlacementBuilder.CTA_ASSET_ID: {
                    callToAction = asset.getData().getValue();
                    break;
                }
                case NativePlacementBuilder.RATING_ASSET_ID: {
                    rating = Float.valueOf(asset.getData().getValue());
                    break;
                }
                case NativePlacementBuilder.SPONSORED_ASSET_ID: {
                    sponsored = asset.getData().getValue();
                    break;
                }
                case NativePlacementBuilder.VIDEO_ASSET_ID: {
                    if (!asset.getVideo().getCurl()
                            .equals(Ad.Display.Native.Asset.VideoAsset.getDefaultInstance().getCurl())) {
                        videoUrl = asset.getVideo().getCurl();
                    }
                    if (!asset.getVideo().getAdm()
                            .equals(Ad.Display.Native.Asset.VideoAsset.getDefaultInstance().getAdm())) {
                        videoAdm = asset.getVideo().getAdm();
                    }
                    break;
                }
            }
        }
    }

    @NonNull
    @Override
    public String getTitle() {
        return title;
    }

    @NonNull
    @Override
    public String getDescription() {
        return description;
    }

    @Nullable
    @Override
    public String getIconUrl() {
        return iconUrl;
    }

    @Nullable
    @Override
    public String getImageUrl() {
        return imageUrl;
    }

    @Nullable
    @Override
    public String getCallToAction() {
        return callToAction;
    }

    @Nullable
    @Override
    public String getSponsored() {
        return sponsored;
    }

    @Nullable
    @Override
    public String getAgeRestrictions() {
        return ageRestrictions;
    }

    @Nullable
    @Override
    public String getClickUrl() {
        return clickUrl;
    }

    @Nullable
    @Override
    public String getVideoUrl() {
        return videoUrl;
    }

    @Nullable
    @Override
    public String getVideoAdm() {
        return videoAdm;
    }

    @Override
    public float getRating() {
        return rating;
    }

    @Override
    public boolean isValid() {
        return !TextUtils.isEmpty(title);
    }
}
