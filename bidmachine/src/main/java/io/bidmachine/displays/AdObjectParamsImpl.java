package io.bidmachine.displays;

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.bidmachine.TrackEventType;
import io.bidmachine.models.AdObjectParams;
import io.bidmachine.protobuf.AdExtension;
import io.bidmachine.protobuf.InvalidProtocolBufferException;
import io.bidmachine.protobuf.adcom.Ad;
import io.bidmachine.protobuf.openrtb.Response;

import static io.bidmachine.Utils.getOrDefault;

abstract class AdObjectParamsImpl implements AdObjectParams {

    private static final long DEF_VIEWABILITY_TIME_THRESHOLD = 1;
    private static final float DEF_VIEWABILITY_PIXEL_THRESHOLD = 1;

    private String creativeId;
    private String creativeAdm;
    private int width;
    private int height;
    private long viewabilityTimeThresholdSec = DEF_VIEWABILITY_TIME_THRESHOLD;
    private float viewabilityPixelThreshold = DEF_VIEWABILITY_PIXEL_THRESHOLD;
    private float viewabilityDurationThreshold;

    private boolean canPreload = true;

    private Map<TrackEventType, List<String>> trackUrls = new EnumMap<>(TrackEventType.class);

    AdObjectParamsImpl(@NonNull Response.Seatbid seatbid, @NonNull Response.Seatbid.Bid bid, @NonNull Ad ad) {
        creativeId = ad.getId();
        if (ad.getExtCount() > 0) {
            if (ad.getExt(0).is(AdExtension.class)) {
                try {
                    AdExtension extension = ad.getExt(0).unpack(AdExtension.class);
                    prepareExtensions(seatbid, bid, extension);
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @CallSuper
    protected void prepareExtensions(@NonNull Response.Seatbid seatbid,
                                     @NonNull Response.Seatbid.Bid bid,
                                     @NonNull AdExtension extension) {
        canPreload = extension.getPreload();
        viewabilityTimeThresholdSec = getOrDefault(extension.getViewabilityTimeThreshold(),
                AdExtension.getDefaultInstance().getViewabilityTimeThreshold(),
                DEF_VIEWABILITY_TIME_THRESHOLD);
        viewabilityPixelThreshold = getOrDefault(extension.getViewabilityPixelThreshold(),
                AdExtension.getDefaultInstance().getViewabilityPixelThreshold(),
                DEF_VIEWABILITY_PIXEL_THRESHOLD); //possibly should be multiplied by 100;
        viewabilityDurationThreshold = getOrDefault(extension.getViewabilityDurationThreshold(),
                AdExtension.getDefaultInstance().getViewabilityDurationThreshold(),
                0);

        prepareEvents(extension.getEventList());
    }

    protected void prepareEvents(@Nullable List<Ad.Event> events) {
        if (events == null || events.size() == 0) {
            return;
        }
        for (Ad.Event event : events) {
            TrackEventType eventType = TrackEventType.fromNumber(event.getTypeValue());
            if (eventType == null) continue;
            addEvent(eventType, event.getUrl());
        }
    }

    protected void addEvent(@NonNull TrackEventType eventType, String url) {
        List<String> urlList = trackUrls.get(eventType);
        if (urlList == null) {
            urlList = new ArrayList<>(1);
            trackUrls.put(eventType, urlList);
        }
        urlList.add(url);
    }

    public long getViewabilityTimeThresholdMs() {
        return TimeUnit.SECONDS.toMillis(viewabilityTimeThresholdSec);
    }

    public long getViewabilityTimeThresholdSec() {
        return viewabilityTimeThresholdSec;
    }

    public float getViewabilityPixelThreshold() {
        return viewabilityPixelThreshold;
    }

    public float getViewabilityDurationThreshold() {
        return viewabilityDurationThreshold;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    @Override
    public int getWidth() {
        return width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public String getCreativeId() {
        return creativeId;
    }

    void setCreativeAdm(String creativeAdm) {
        this.creativeAdm = creativeAdm;
    }

    @Override
    public String getCreativeAdm() {
        return creativeAdm;
    }

    @Nullable
    @Override
    public List<String> getTrackUrls(@NonNull TrackEventType eventType) {
        return trackUrls.get(eventType);
    }

    @Override
    public boolean isValid() {
        return !TextUtils.isEmpty(getCreativeAdm());
    }

    @Override
    public boolean canPreload() {
        return canPreload;
    }

}
