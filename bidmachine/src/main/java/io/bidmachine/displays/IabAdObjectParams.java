package io.bidmachine.displays;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import com.explorestack.protobuf.adcom.Ad;
import com.explorestack.protobuf.openrtb.Response;
import io.bidmachine.models.AdObjectParams;
import io.bidmachine.protobuf.AdExtension;
import io.bidmachine.unified.UnifiedMediationParams;

import java.util.HashMap;
import java.util.Map;

import static io.bidmachine.Utils.getOrDefault;
import static io.bidmachine.utils.IabUtils.*;

abstract class IabAdObjectParams
        extends AdObjectParams
        implements UnifiedMediationParams.MappedUnifiedMediationParams.DataProvider {

    private static final int DEF_SKIP_AFTER_TIME_SEC = 2;

    private Map<String, Object> params;
    private UnifiedMediationParams mediationParams = new UnifiedMediationParams.MappedUnifiedMediationParams(this);

    IabAdObjectParams(@NonNull Response.Seatbid seatbid,
                      @NonNull Response.Seatbid.Bid bid,
                      @NonNull Ad ad) {
        super(seatbid, bid, ad);
        getData().put(KEY_CREATIVE_ID, ad.getId());
    }

    @NonNull
    @Override
    public Map<String, Object> getData() {
        if (params == null) {
            params = new HashMap<>();
        }
        return params;
    }

    @Override
    protected void prepareExtensions(@NonNull Response.Seatbid seatbid,
                                     @NonNull Response.Seatbid.Bid bid,
                                     @NonNull AdExtension extension) {
        super.prepareExtensions(seatbid, bid, extension);
        getData().put(KEY_PRELOAD, extension.getPreload());
        getData().put(KEY_SKIP_AFTER_TIME_SEC, getOrDefault(extension.getSkipAfter(),
                AdExtension.getDefaultInstance().getSkipAfter(),
                DEF_SKIP_AFTER_TIME_SEC));
    }

    public void setWidth(int width) {
        getData().put(KEY_WIDTH, width);
    }

    public void setHeight(int height) {
        getData().put(KEY_HEIGHT, height);
    }

    void setCreativeAdm(String creativeAdm) {
        getData().put(KEY_CREATIVE_ADM, creativeAdm);
    }

    @Override
    public boolean isValid() {
        Object creativeAdm = params.get(KEY_CREATIVE_ADM);
        return creativeAdm instanceof String && !TextUtils.isEmpty((CharSequence) creativeAdm);
    }

    @NonNull
    @Override
    public UnifiedMediationParams toMediationParams() {
        return mediationParams;
    }
}
