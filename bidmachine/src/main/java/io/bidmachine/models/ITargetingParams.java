package io.bidmachine.models;

import android.location.Location;

import io.bidmachine.utils.Gender;

public interface ITargetingParams<SelfType> extends IBlockedParams<SelfType> {

    /**
     * Set Vendor-specific target user Id
     *
     * @param userId Vendor-specific ID for the user
     * @return Self instance
     */
    SelfType setUserId(String userId);

    /**
     * Set target user gender
     *
     * @param gender Gender, one of: Female, Male, Omitted {@link Gender}
     * @return Self instance
     */
    SelfType setGender(Gender gender);

    /**
     * Set target user birthday year in 4-digit integer (e.g - 1990) format
     *
     * @param birthdayYear Year of birth as a 4-digit integer (e.g - 1990)
     * @return Self instance
     */
    SelfType setBirthdayYear(Integer birthdayYear);

    /**
     * Set array of keywords, interests, or intent (Comma separated if you use xml)
     *
     * @param keywords Array of keywords
     * @return Self instance
     */
    SelfType setKeywords(String... keywords);

    /**
     * Set location of the user's home base (i.e., not necessarily their current location)
     *
     * @param location Location of the user's home base (i.e., not necessarily their current location)
     * @return Self instance
     */
    SelfType setDeviceLocation(Location location);

    /**
     * Set Country of the user's home base (i.e., not necessarily their current location)
     *
     * @param country An uppercase ISO 3166 2-letter code, or a UN M.49 3-digit code.
     * @return Self instance
     */
    SelfType setCountry(String country);

    /**
     * Set city of the user's home base (i.e., not necessarily their current location)
     *
     * @param city User's city
     * @return Self instance
     */
    SelfType setCity(String city);

    /**
     * Set ZIP of the user's home base (i.e., not necessarily their current location)
     *
     * @param zip User's ZIP
     * @return Self instance
     */
    SelfType setZip(String zip);

    /**
     * Set App store URL for an installed app; for <a href="https://cdn2.hubspot.net/hubfs/2848641/TrustworthyAccountabilityGroup_May2017/Docs/Summary-of-Changes-in-IQG-2.1.pdf?t=1504724070693">IQG 2.1</a> compliance.
     *
     * @param url App store url
     * @return Self instance
     */
    SelfType setStoreUrl(String url);

    /**
     * Set is it free of paid app version
     *
     * @param paid {@code true} if it's paid app
     * @return Self instance
     */
    SelfType setPaid(Boolean paid);

}
