package io.bidmachine;

import android.support.annotation.NonNull;

import com.explorestack.protobuf.adcom.Context;
import com.explorestack.protobuf.openrtb.Request;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import io.bidmachine.models.DataRestrictions;
import io.bidmachine.unified.UnifiedAdRequestParams;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class AdRequestTest {

    private android.content.Context context;
    private AdRequest adRequest;

    @Before
    public void setUp() throws Exception {
        context = RuntimeEnvironment.application;
        adRequest = new TestAdRequest(AdsType.Interstitial);
        BidMachine.setConsentConfig(true, null);
        BidMachine.setSubjectToGDPR(null);
        BidMachine.initialize(context, "1");
    }

    @Test
    public void build_userRestrictionParamsNotSet_useDefaultValues() throws Exception {
        Request request = (Request) adRequest.build(context, adRequest.getType());
        Context requestContext = request.getContext().unpack(Context.class);
        assertEquals(
                Context.User.getDefaultInstance().getConsent(),
                requestContext.getUser().getConsent());
        assertFalse(requestContext.getRegs().getGdpr());
    }

    @Test
    public void build_userRestrictionParamsIsSet_useValuesFromUserRestrictionParams() throws Exception {
        UserRestrictionParams userRestrictionParams = new UserRestrictionParams();
        userRestrictionParams.setConsentConfig(
                true,
                "private_consent_string");
        userRestrictionParams.setSubjectToGDPR(true);
        adRequest.userRestrictionParams = userRestrictionParams;
        Request request = (Request) adRequest.build(context, adRequest.getType());
        Context requestContext = request.getContext().unpack(Context.class);
        assertEquals(
                "private_consent_string",
                requestContext.getUser().getConsent());
        assertTrue(requestContext.getRegs().getGdpr());
    }

    @Test
    public void build_userSetParamsFromPublicApi_useValuesFromPublicApi() throws Exception {
        BidMachine.setConsentConfig(false, "public_consent_string");
        BidMachine.setSubjectToGDPR(true);
        Request request = (Request) adRequest.build(context, adRequest.getType());
        Context requestContext = request.getContext().unpack(Context.class);
        assertEquals(
                "public_consent_string",
                requestContext.getUser().getConsent());
        assertTrue(requestContext.getRegs().getGdpr());
    }
    @Test
    public void build_userRestrictionParamsIsSetAndUserSetParamsFromApi_useValuesFromUserRestrictionParams() throws Exception {
        BidMachine.setConsentConfig(false, "public_consent_string");
        BidMachine.setSubjectToGDPR(true);
        UserRestrictionParams userRestrictionParams = new UserRestrictionParams();
        userRestrictionParams.setConsentConfig(
                true,
                "private_consent_string");
        userRestrictionParams.setSubjectToGDPR(false);
        adRequest.userRestrictionParams = userRestrictionParams;
        Request request = (Request) adRequest.build(context, adRequest.getType());
        Context requestContext = request.getContext().unpack(Context.class);
        assertEquals(
                "private_consent_string",
                requestContext.getUser().getConsent());
        assertFalse(requestContext.getRegs().getGdpr());
    }

    private static class TestAdRequest extends AdRequest {

        TestAdRequest(@NonNull AdsType adsType) {
            super(adsType);
        }

        @NonNull
        @Override
        protected UnifiedAdRequestParams createUnifiedAdRequestParams(@NonNull TargetingParams targetingParams, @NonNull DataRestrictions dataRestrictions) {
            return new BaseUnifiedAdRequestParams(targetingParams, dataRestrictions);
        }

    }

}