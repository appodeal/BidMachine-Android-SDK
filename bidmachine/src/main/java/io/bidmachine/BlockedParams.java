package io.bidmachine;

import java.util.ArrayList;

import io.bidmachine.models.IBlockedParams;
import io.bidmachine.models.RequestParams;
import io.bidmachine.models.RequestParamsRestrictions;
import io.bidmachine.protobuf.adcom.Context;

import static io.bidmachine.core.Utils.resolveList;

public final class BlockedParams extends RequestParams implements IBlockedParams<BlockedParams> {

    private ArrayList<String> blockedCategories;
    private ArrayList<String> blockedDomains;
    private ArrayList<String> blockedApplications;

    void build(android.content.Context context,
               Context.Restrictions.Builder builder,
               BlockedParams defaults,
               RequestParamsRestrictions restrictions) {
        builder.addAllBcat(resolveList(blockedCategories, defaults != null ? defaults.blockedCategories : null));
        builder.addAllBadv(resolveList(blockedDomains, defaults != null ? defaults.blockedDomains : null));
        builder.addAllBapp(resolveList(blockedApplications, defaults != null ? defaults.blockedApplications : null));
    }

    @Override
    public BlockedParams addBlockedAdvertiserIABCategory(String category) {
        if (blockedCategories == null) {
            blockedCategories = new ArrayList<>();
        }
        blockedCategories.add(category);
        return this;
    }

    @Override
    public BlockedParams addBlockedAdvertiserDomain(String domain) {
        if (blockedDomains == null) {
            blockedDomains = new ArrayList<>();
        }
        blockedDomains.add(domain);
        return this;
    }

    @Override
    public BlockedParams addBlockedApplication(String bundleOrPackage) {
        if (blockedApplications == null) {
            blockedApplications = new ArrayList<>();
        }
        blockedApplications.add(bundleOrPackage);
        return this;
    }

}
