package io.bidmachine.models;

public interface IBlockedParams<SelfType> {

    /**
     * Add category of content categories using IDs to block list
     *
     * @param category Block list category ID of content
     * @return Self instance
     */
    SelfType addBlockedAdvertiserIABCategory(String category);

    /**
     * Add advertiser domain (e.g., “example.com”) to block list
     *
     * @param domain Advertiser domain (e.g., “example.com”) which will be added to block list
     * @return Self instance
     */
    SelfType addBlockedAdvertiserDomain(String domain);

    /**
     * Add app for which ads are disallowed to block list
     *
     * @param bundleOrPackage App bundle or package for which ads are disallowed to block list
     * @return Self instance
     */
    SelfType addBlockedApplication(String bundleOrPackage);

}
