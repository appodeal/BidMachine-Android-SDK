package io.bidmachine;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import com.explorestack.protobuf.adcom.Context;
import io.bidmachine.models.DataRestrictions;
import io.bidmachine.models.IUserRestrictionsParams;
import io.bidmachine.models.RequestParams;

import static io.bidmachine.core.Utils.oneOf;

final class UserRestrictionParams
        extends RequestParams<UserRestrictionParams>
        implements IUserRestrictionsParams<UserRestrictionParams>, DataRestrictions {

    static final String IAB_CONSENT_STRING = "IABConsent_ConsentString";
    static final String IAB_SUBJECT_TO_GDPR = "IABConsent_SubjectToGDPR";

    private String gdprConsentString;
    private Boolean subjectToGDPR;
    private Boolean hasConsent;
    private Boolean hasCoppa;

    private String iabGDPRConsentString;
    private Boolean iabSubjectToGDPR;

    @Override
    public void merge(@NonNull UserRestrictionParams instance) {
        gdprConsentString = oneOf(gdprConsentString, instance.gdprConsentString);
        subjectToGDPR = oneOf(subjectToGDPR, instance.subjectToGDPR);
        hasConsent = oneOf(hasConsent, instance.hasConsent);
        hasCoppa = oneOf(hasCoppa, instance.hasCoppa);
    }

    void build(@NonNull Context.Regs.Builder builder) {
        builder.setGdpr(subjectToGDPR());
        builder.setCoppa(hasCoppa != null && hasCoppa);
    }

    void build(@NonNull Context.User.Builder builder) {
        String gdprConsentString = gdprConsentString();
        if (gdprConsentString != null) {
            builder.setConsent(gdprConsentString);
        }
    }

    @Override
    public UserRestrictionParams setConsentConfig(boolean hasConsent, String consentString) {
        this.gdprConsentString = consentString;
        this.hasConsent = hasConsent;
        return this;
    }

    @Override
    public UserRestrictionParams setSubjectToGDPR(Boolean subject) {
        subjectToGDPR = subject;
        return this;
    }

    @Override
    public UserRestrictionParams setCoppa(Boolean coppa) {
        hasCoppa = coppa;
        return this;
    }

    void fillIABGDPRConsentString(SharedPreferences sharedPreferences) {
        iabGDPRConsentString = sharedPreferences.getString(
                IAB_CONSENT_STRING,
                null);
    }

    void fillIABSubjectToGDPR(SharedPreferences sharedPreferences) {
        String iabConsentSubjectToGDPR = sharedPreferences.getString(
                IAB_SUBJECT_TO_GDPR,
                null);
        iabSubjectToGDPR = iabConsentSubjectToGDPR != null
                ? iabConsentSubjectToGDPR.equals("1")
                : null;
    }

    private String gdprConsentString() {
        return oneOf(gdprConsentString, iabGDPRConsentString);
    }

    private boolean subjectToGDPR() {
        Boolean oneOfSubjectToGDPR = oneOf(subjectToGDPR, iabSubjectToGDPR);
        return oneOfSubjectToGDPR != null && oneOfSubjectToGDPR;
    }

    private boolean hasConsent() {
        return hasConsent != null && hasConsent;
    }

    private boolean hasCoppa() {
        return hasCoppa != null && hasCoppa;
    }

    @Override
    public boolean canSendGeoPosition() {
        return !hasCoppa() && !isUserGdprProtected();
    }

    @Override
    public boolean canSendUserInfo() {
        return !hasCoppa() && !isUserGdprProtected();
    }

    @Override
    public boolean canSendDeviceInfo() {
        return !hasCoppa();
    }

    @Override
    public boolean canSendIfa() {
        return !isUserGdprProtected();
    }

    @Override
    public boolean isUserInGdprScope() {
        return subjectToGDPR();
    }

    @Override
    public boolean isUserHasConsent() {
        return hasConsent();
    }

    @Override
    public boolean isUserGdprProtected() {
        return subjectToGDPR() && !hasConsent();
    }

    @Override
    public boolean isUserAgeRestricted() {
        return hasCoppa();
    }

}
