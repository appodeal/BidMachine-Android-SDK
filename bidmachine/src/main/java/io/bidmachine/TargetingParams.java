package io.bidmachine;

import android.location.Location;
import android.support.annotation.NonNull;
import com.explorestack.protobuf.adcom.Context;
import io.bidmachine.core.Utils;
import io.bidmachine.models.ITargetingParams;
import io.bidmachine.models.RequestParams;
import io.bidmachine.models.DataRestrictions;
import io.bidmachine.utils.Gender;

import static io.bidmachine.core.Utils.oneOf;

public final class TargetingParams extends RequestParams implements ITargetingParams<TargetingParams> {

    private String userId;
    private Gender gender;
    private Integer birthdayYear;
    private String[] keywords;
    private String country;
    private String city;
    private String zip;
    private Location deviceLocation;
    private String storeUrl;
    private Boolean isPaid;

    private BlockedParams blockedParams;

    public Location getDeviceLocation() {
        return deviceLocation;
    }

    BlockedParams getBlockedParams() {
        return blockedParams;
    }

    void build(android.content.Context context,
               Context.App.Builder builder,
               @NonNull TargetingParams defaults,
               DataRestrictions restrictions) {
        final String storeUrl = oneOf(this.storeUrl, defaults.storeUrl);
        if (storeUrl != null) {
            builder.setStoreurl(storeUrl);
        }
        final Boolean isPaid = oneOf(this.isPaid, defaults.isPaid);
        builder.setPaid(isPaid != null && isPaid);

        final String packageName = context.getPackageName();
        if (packageName != null) {
            builder.setBundle(packageName);
        }

        String appVersion = Utils.getAppVersion(context);
        if (appVersion != null) {
            builder.setVer(appVersion);
        }
        String appName = Utils.getAppName(context);
        if (appName != null) {
            builder.setName(appName);
        }
    }

    void build(android.content.Context context,
               Context.User.Builder builder,
               @NonNull TargetingParams defaults,
               DataRestrictions restrictions) {
        //User id
        final String userId = oneOf(this.userId, defaults.userId);
        if (userId != null) {
            builder.setId(userId);
        }
        //Birthday year
        final Integer birthdayYear = oneOf(this.birthdayYear, defaults.birthdayYear);
        if (birthdayYear != null) {
            builder.setYob(birthdayYear);
        }
        //Gender
        final Gender gender = oneOf(this.gender, defaults.gender);
        if (gender != null) {
            builder.setGender(gender.getOrtbValue());
        }
        //Keywords
        final String[] keywords = oneOf(this.keywords, defaults.keywords);
        if (keywords != null && keywords.length > 0) {
            final StringBuilder keywordsBuilder = new StringBuilder();
            for (String keyword : keywords) {
                if (keywordsBuilder.length() > 0) keywordsBuilder.append(",");
                keywordsBuilder.append(keyword);
            }
            builder.setKeywords(keywordsBuilder.toString());
        }
        //Geo
        final Context.Geo.Builder geoBuilder = Context.Geo.newBuilder();
        build(context, geoBuilder, defaults, restrictions);
        OrtbUtils.locationToGeo(geoBuilder, null, false);
        builder.setGeo(geoBuilder);
    }

    void build(android.content.Context context,
               Context.Geo.Builder builder,
               @NonNull TargetingParams defaults,
               DataRestrictions restrictions) {
        final String country = oneOf(this.country, defaults.country);
        if (country != null) {
            builder.setCountry(country);
        }
        final String city = oneOf(this.city, defaults.city);
        if (city != null) {
            builder.setCity(city);
        }
        final String zip = oneOf(this.zip, defaults.zip);
        if (zip != null) {
            builder.setZip(zip);
        }
    }

    @Override
    public TargetingParams setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    @Override
    public TargetingParams setGender(Gender gender) {
        this.gender = gender;
        return this;
    }

    @Override
    public TargetingParams setBirthdayYear(Integer birthdayYear) {
        if (birthdayYear != null) {
            Utils.assertYear(birthdayYear);
        }
        this.birthdayYear = birthdayYear;
        return this;
    }

    @Override
    public TargetingParams setKeywords(String... keywords) {
        this.keywords = keywords;
        return this;
    }

    @Override
    public TargetingParams setCountry(String country) {
        this.country = country;
        return this;
    }

    @Override
    public TargetingParams setCity(String city) {
        this.city = city;
        return this;
    }

    @Override
    public TargetingParams setZip(String zip) {
        this.zip = zip;
        return this;
    }

    @Override
    public TargetingParams setStoreUrl(String url) {
        this.storeUrl = url;
        return this;
    }

    @Override
    public TargetingParams setPaid(Boolean paid) {
        this.isPaid = paid;
        return this;
    }

    @Override
    public TargetingParams setDeviceLocation(Location location) {
        this.deviceLocation = location;
        return this;
    }

    @Override
    public TargetingParams addBlockedApplication(String bundleOrPackage) {
        prepareBlockParams();
        blockedParams.addBlockedApplication(bundleOrPackage);
        return this;
    }

    @Override
    public TargetingParams addBlockedAdvertiserIABCategory(String category) {
        prepareBlockParams();
        blockedParams.addBlockedAdvertiserIABCategory(category);
        return this;
    }

    @Override
    public TargetingParams addBlockedAdvertiserDomain(String domain) {
        prepareBlockParams();
        blockedParams.addBlockedAdvertiserDomain(domain);
        return this;
    }

    private void prepareBlockParams() {
        if (blockedParams == null) {
            blockedParams = new BlockedParams();
        }
    }

}
