package io.bidmachine;

import android.content.Context;

import io.bidmachine.core.Utils;
import io.bidmachine.displays.FullScreenAdObjectParams;
import io.bidmachine.utils.BMError;

public abstract class FullScreenAd<
        SelfType extends FullScreenAd<SelfType, AdRequestType, AdObjectType, ListenerType>,
        AdRequestType extends AdRequest<AdRequestType>,
        AdObjectType extends FullScreenAdObject<SelfType>,
        ListenerType extends AdListener<SelfType>>
        extends OrtbAd<SelfType, AdRequestType, AdObjectType, FullScreenAdObjectParams, ListenerType>
        implements IFullScreenAd {

    public FullScreenAd(Context context) {
        super(context);
    }

    @Override
    public void show() {
        final AdObjectType loadedObject = getLoadedObject();
        if (!prepareShow() || loadedObject == null) return;
        if (!Utils.isNetworkAvailable(getContext())) {
            processCallback.processShowFail(BMError.Connection);
        } else {
            loadedObject.show();
        }
    }

    @Override
    public boolean canShow() {
        return super.canShow() && Utils.isNetworkAvailable(getContext());
    }

}
