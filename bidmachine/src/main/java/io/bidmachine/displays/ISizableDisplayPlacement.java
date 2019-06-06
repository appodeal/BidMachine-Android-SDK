package io.bidmachine.displays;

import android.content.Context;
import android.graphics.Point;

import io.bidmachine.AdRequest;

public interface ISizableDisplayPlacement<AdRequestType extends AdRequest> {

    Point getSize(Context context, AdRequestType adRequestType);

}
