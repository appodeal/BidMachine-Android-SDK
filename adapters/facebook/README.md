## Overview

This folder contains mediation adapter used to mediate `Facebook`.

## Integration

Add next dependency to you `build.gradle`:

```groovy
dependencies {
    // ... other dependencies
    implementation 'io.bidmachine:ads.networks.facebook:1.3.0.1'
}
```

Configure `Facebook` network:

```java
BidMachine.registerNetworks(
                new FacebookConfig("YOUR_APP_ID")
                        .withMediationConfig(AdsFormat.Banner, "YOUR_PLACEMENT_ID")
                        .withMediationConfig(AdsFormat.Banner_300x250, "YOUR_PLACEMENT_ID")
                        .withMediationConfig(AdsFormat.InterstitialStatic, "YOUR_PLACEMENT_ID")
                        .withMediationConfig(AdsFormat.RewardedVideo, "YOUR_PLACEMENT_ID"));
```
