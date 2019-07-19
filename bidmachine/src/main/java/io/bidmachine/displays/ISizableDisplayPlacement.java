package io.bidmachine.displays;

import android.content.Context;
import android.graphics.Point;
import io.bidmachine.unified.UnifiedAdRequestParams;

public interface ISizableDisplayPlacement<UnifiedAdRequestParamsType extends UnifiedAdRequestParams> {

    Point getSize(Context context, UnifiedAdRequestParamsType adRequestParams);

}
