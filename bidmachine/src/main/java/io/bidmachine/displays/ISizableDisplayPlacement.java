package io.bidmachine.displays;

import android.graphics.Point;
import io.bidmachine.unified.UnifiedAdRequestParams;
import io.bidmachine.ContextProvider;

interface ISizableDisplayPlacement<UnifiedAdRequestParamsType extends UnifiedAdRequestParams> {

    Point getSize(ContextProvider contextProvider, UnifiedAdRequestParamsType adRequestParams);

}
