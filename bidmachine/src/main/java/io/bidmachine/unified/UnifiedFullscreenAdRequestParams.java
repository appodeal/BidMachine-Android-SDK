package io.bidmachine.unified;

import android.support.annotation.NonNull;
import io.bidmachine.AdContentType;

public interface UnifiedFullscreenAdRequestParams extends UnifiedAdRequestParams {
    boolean isContentTypeMatch(@NonNull AdContentType adContentType);
}
