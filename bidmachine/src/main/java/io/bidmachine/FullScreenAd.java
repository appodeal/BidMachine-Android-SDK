package io.bidmachine;

import android.content.Context;
import android.support.annotation.NonNull;
import io.bidmachine.core.Utils;
import io.bidmachine.models.AdObjectParams;
import io.bidmachine.utils.BMError;

public abstract class FullScreenAd<
        SelfType extends FullScreenAd<SelfType, AdRequestType, AdObjectType, ListenerType>,
        AdRequestType extends FullScreenAdRequest<AdRequestType>,
        AdObjectType extends FullScreenAdObject<AdRequestType>,
        ListenerType extends AdListener<SelfType>>
        extends BidMachineAd<SelfType, AdRequestType, AdObjectType, AdObjectParams, ListenerType>
        implements IFullScreenAd {

    public FullScreenAd(Context context) {
        super(context);
    }

    @Override
    public void show(@NonNull Context context) {
        final AdObjectType loadedObject = getLoadedObject();
        if (!prepareShow() || loadedObject == null) return;
        if (!Utils.isNetworkAvailable(context)) {
            processCallback.processShowFail(BMError.Connection);
        } else {
            loadedObject.show(context);
        }
    }

    @Override
    public boolean canShow() {
        Context context = getContext();
        return super.canShow() && context != null && Utils.isNetworkAvailable(context);
    }
}
