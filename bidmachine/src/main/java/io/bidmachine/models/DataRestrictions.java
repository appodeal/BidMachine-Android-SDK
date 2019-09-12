package io.bidmachine.models;

import android.content.Context;
import android.support.annotation.NonNull;

public interface DataRestrictions {

    boolean canSendGeoPosition();

    boolean canSendUserInfo();

    boolean canSendDeviceInfo();

    boolean canSendIfa();

    boolean isUserInGdprScope();

    boolean isUserHasConsent();

    boolean isUserGdprProtected();

    boolean isUserAgeRestricted();

    String getHttpAgent(@NonNull Context context);

    String getIfa(@NonNull Context context);

    boolean isLimitAdTrackingEnabled();
}
