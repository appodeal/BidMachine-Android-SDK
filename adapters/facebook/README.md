## Overview

This folder contains mediation adapter used to mediate `Facebook`.

## Integration

[<img src="https://img.shields.io/badge/Min%20SDK%20version-1.3.0-brightgreen">](https://github.com/bidmachine/BidMachine-Android-SDK)
[<img src="https://img.shields.io/badge/Network%20Adapter%20version-1.3.0.1-brightgreen">](https://artifactory.bidmachine.io/bidmachine/io/bidmachine/ads.networks.facebook/1.3.0.1/)
[<img src="https://img.shields.io/badge/Network%20version-5.4.1-blue">](https://developers.facebook.com/docs/android/)

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
