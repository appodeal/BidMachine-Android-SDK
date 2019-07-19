package io.bidmachine;

import android.support.annotation.NonNull;
import io.bidmachine.displays.PlacementBuilder;
import io.bidmachine.models.RequestBuilder;
import io.bidmachine.unified.UnifiedFullscreenAdRequestParams;

public abstract class FullScreenAdRequest<SelfType extends FullScreenAdRequest>
        extends AdRequest<SelfType, UnifiedFullscreenAdRequestParams> {

    AdContentType adContentType = AdContentType.All;

    protected AdContentType getAdContentType() {
        return adContentType;
    }

    @Override
    boolean isPlacementBuilderMatch(PlacementBuilder placementBuilder) {
        return (adContentType == AdContentType.All || adContentType == placementBuilder.getAdContentType())
                && super.isPlacementBuilderMatch(placementBuilder);
    }

    @Override
    public UnifiedFullscreenAdRequestParams getUnifiedRequestParams() {
        return new FullscreenUnifiedAdRequestParams();
    }

    protected abstract static class FullScreenRequestBuilder<
            SelfType extends RequestBuilder,
            ReturnType extends FullScreenAdRequest>
            extends AdRequestBuilderImpl<SelfType, ReturnType> {

        @SuppressWarnings("unchecked")
        protected SelfType setAdContentType(@NonNull AdContentType adContentType) {
            prepareRequest();
            params.adContentType = adContentType;
            return (SelfType) this;
        }
    }

    private class FullscreenUnifiedAdRequestParams
            extends BaseUnifiedRequestParams
            implements UnifiedFullscreenAdRequestParams {
        @Override
        public boolean isContentTypeMatch(@NonNull AdContentType adContentType) {
            return FullScreenAdRequest.this.adContentType == AdContentType.All
                    || FullScreenAdRequest.this.adContentType == adContentType;
        }
    }

}
