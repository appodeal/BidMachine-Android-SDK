package io.bidmachine.models;

public interface DataRestrictions {

    boolean canSendGeoPosition();

    boolean canSendUserInfo();

    boolean canSendDeviceInfo();

    boolean canSendIfa();

    boolean isUserInGdprScope();

    boolean isUserHasConsent();

    boolean isUserGdprProtected();

    boolean isUserAgeRestricted();
}
