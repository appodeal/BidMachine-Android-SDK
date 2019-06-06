package io.bidmachine;

import android.support.annotation.NonNull;

import io.bidmachine.models.IUserRestrictionsParams;
import io.bidmachine.models.RequestParams;
import io.bidmachine.models.RequestParamsRestrictions;
import io.bidmachine.protobuf.adcom.Context;
import io.bidmachine.protobuf.adcom.Context.Regs;

import static io.bidmachine.core.Utils.oneOf;

final class UserRestrictionParams extends RequestParams
        implements IUserRestrictionsParams<UserRestrictionParams> {

    private String gdprConsentString;
    private Boolean subjectToGDPR;
    private Boolean hasCoppa;
    private Boolean hasConsent;

    private Boolean hasCoppa() {
        return hasCoppa;
    }

    private Boolean hasConsent() {
        return hasConsent;
    }

    private Boolean subjectToGDPR() {
        return subjectToGDPR;
    }

    void build(@NonNull android.content.Context context,
               @NonNull Regs.Builder builder,
               @NonNull UserRestrictionParams defaults,
               @NonNull RequestParamsRestrictions restrictions) {
        builder.setGdpr(oneOf(subjectToGDPR, defaults.subjectToGDPR, false));
        builder.setCoppa(oneOf(hasCoppa, defaults.hasCoppa, false));
    }

    void build(@NonNull android.content.Context context,
               @NonNull Context.User.Builder builder,
               @NonNull UserRestrictionParams defaults,
               @NonNull RequestParamsRestrictions restrictions) {
        final String gdprConsentString = oneOf(this.gdprConsentString, defaults.gdprConsentString);
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

    static RequestParamsRestrictions createRestrictions(final UserRestrictionParams userRestrictionParams) {
        final boolean hasCoppa = oneOf(userRestrictionParams.hasCoppa(),
                BidMachineImpl.get().getUserRestrictionParams().hasCoppa(), false);

        final boolean subjectToGDPR = oneOf(userRestrictionParams.subjectToGDPR(),
                BidMachineImpl.get().getUserRestrictionParams().subjectToGDPR(), false);

        final boolean hasConsent = oneOf(userRestrictionParams.hasConsent(),
                BidMachineImpl.get().getUserRestrictionParams().hasConsent(), false);

        final boolean underGdprRestrictions = subjectToGDPR && !hasConsent;

        return new RequestParamsRestrictions() {
            @Override
            public boolean canSendGeoPosition() {
                return !hasCoppa && !underGdprRestrictions;
            }

            @Override
            public boolean canSendUserInfo() {
                return !hasCoppa && !underGdprRestrictions;
            }

            @Override
            public boolean canSendDeviceInfo() {
                return !hasCoppa;
            }

            @Override
            public boolean canSendIfa() {
                return !underGdprRestrictions;
            }
        };
    }

}
