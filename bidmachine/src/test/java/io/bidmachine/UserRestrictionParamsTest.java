package io.bidmachine;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.explorestack.protobuf.adcom.Context;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class UserRestrictionParamsTest {

    private static SharedPreferences defaultSharedPreferences;

    @Before
    public void setUp() throws Exception {
        android.content.Context context = RuntimeEnvironment.application;
        defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        defaultSharedPreferences.edit().clear().apply();
    }

    @Test
    public void buildUser_bidMachineAndSharedPrefProvidedConsentString_returnBidMachineConsentString() {
        defaultSharedPreferences.edit()
                .putString("IABConsent_ConsentString", "iab_consent_string")
                .apply();
        BidMachine.setConsentConfig(true, "bid_machine_consent_string");
        BidMachineImpl.get()
                .getUserRestrictionParams()
                .fillIABGDPRConsentString(defaultSharedPreferences);

        Context.User.Builder builder = Context.User.newBuilder();
        assertEquals("", builder.getConsent());
        BidMachineImpl.get()
                .getUserRestrictionParams()
                .build(builder);
        assertEquals("bid_machine_consent_string", builder.getConsent());
    }

    @Test
    public void buildUser_bidMachineProvidedAndSharedPrefNotProvidedConsentString_returnBidMachineConsentString() {
        BidMachine.setConsentConfig(true, "bid_machine_consent_string");
        BidMachineImpl.get()
                .getUserRestrictionParams()
                .fillIABGDPRConsentString(defaultSharedPreferences);

        Context.User.Builder builder = Context.User.newBuilder();
        assertEquals("", builder.getConsent());
        BidMachineImpl.get()
                .getUserRestrictionParams()
                .build(builder);
        assertEquals("bid_machine_consent_string", builder.getConsent());
    }

    @Test
    public void buildUser_bidMachineNotProvidedAndSharedPrefProvidedConsentString_returnIABConsentString() {
        defaultSharedPreferences.edit()
                .putString("IABConsent_ConsentString", "iab_consent_string")
                .apply();
        BidMachine.setConsentConfig(true, null);
        BidMachineImpl.get()
                .getUserRestrictionParams()
                .fillIABGDPRConsentString(defaultSharedPreferences);

        Context.User.Builder builder = Context.User.newBuilder();
        assertEquals("", builder.getConsent());
        BidMachineImpl.get()
                .getUserRestrictionParams()
                .build(builder);
        assertEquals("iab_consent_string", builder.getConsent());
    }

    @Test
    public void buildUser_bidMachineAndSharedPrefNotProvidedConsentString_returnDefaultConsentString() {
        BidMachine.setConsentConfig(true, null);
        BidMachineImpl.get()
                .getUserRestrictionParams()
                .fillIABGDPRConsentString(defaultSharedPreferences);

        Context.User.Builder builder = Context.User.newBuilder();
        assertEquals("", builder.getConsent());
        BidMachineImpl.get()
                .getUserRestrictionParams()
                .build(builder);
        assertEquals("", builder.getConsent());
    }

    @Test
    public void buildRegs_bidMachineAndSharedPrefProvidedSubjectToGDPR_returnBidMachineSubjectToGDPR() {
        defaultSharedPreferences.edit()
                .putString("IABConsent_SubjectToGDPR", "0")
                .apply();
        BidMachine.setSubjectToGDPR(true);
        BidMachineImpl.get()
                .getUserRestrictionParams()
                .fillIABSubjectToGDPR(defaultSharedPreferences);

        Context.Regs.Builder builder = Context.Regs.newBuilder();
        assertFalse(builder.getGdpr());
        BidMachineImpl.get()
                .getUserRestrictionParams()
                .build(builder);
        assertTrue(builder.getGdpr());
    }

    @Test
    public void buildRegs_bidMachineProvidedAndSharedPrefNotProvidedSubjectToGDPR_returnBidMachineSubjectToGDPR() {
        BidMachine.setSubjectToGDPR(true);
        BidMachineImpl.get()
                .getUserRestrictionParams()
                .fillIABSubjectToGDPR(defaultSharedPreferences);

        Context.Regs.Builder builder = Context.Regs.newBuilder();
        assertFalse(builder.getGdpr());
        BidMachineImpl.get()
                .getUserRestrictionParams()
                .build(builder);
        assertTrue(builder.getGdpr());
    }

    @Test
    public void buildRegs_bidMachineNotProvidedAndSharedPrefProvidedSubjectToGDPR_returnIABSubjectToGDPR() {
        defaultSharedPreferences.edit()
                .putString("IABConsent_SubjectToGDPR", "1")
                .apply();
        BidMachine.setSubjectToGDPR(null);
        BidMachineImpl.get()
                .getUserRestrictionParams()
                .fillIABSubjectToGDPR(defaultSharedPreferences);

        Context.Regs.Builder builder = Context.Regs.newBuilder();
        assertFalse(builder.getGdpr());
        BidMachineImpl.get()
                .getUserRestrictionParams()
                .build(builder);
        assertTrue(builder.getGdpr());
    }

    @Test
    public void buildRegs_bidMachineAndSharedPrefNotProvidedSubjectToGDPR_returnDefaultSubjectToGDPR() {
        BidMachine.setSubjectToGDPR(null);
        BidMachineImpl.get()
                .getUserRestrictionParams()
                .fillIABSubjectToGDPR(defaultSharedPreferences);

        Context.Regs.Builder builder = Context.Regs.newBuilder();
        assertFalse(builder.getGdpr());
        BidMachineImpl.get()
                .getUserRestrictionParams()
                .build(builder);
        assertFalse(builder.getGdpr());
    }

}