## Overview

This folder contains mediation adapter used to mediate `Smaato`.

## Integration

[<img src="https://img.shields.io/badge/Min%20SDK%20version-1.3.3-brightgreen">](https://github.com/bidmachine/BidMachine-Android-SDK)
[<img src="https://img.shields.io/badge/Network%20Adapter%20version-1.3.3.2-brightgreen">](https://artifactory.bidmachine.io/bidmachine/io/bidmachine/ads.networks.smaato/1.3.3.1/)
[<img src="https://img.shields.io/badge/Network%20version-21.1.4-blue">](https://developers.smaato.com/nextgen-sdk-android-integration/)

Add next dependency to you `build.gradle`:

```groovy
dependencies {
    // ... other dependencies
    implementation 'io.bidmachine:ads.networks.smaato:1.3.3.1'
}
```

Configure `Smaato` network:

```java
BidMachine.registerNetworks(
               new SmaatoConfig("YOUR_PUBLISHER_ID")
                       .withMediationConfig(AdsFormat.Banner_320x50, "YOUR_AD_SPACE_ID")
                       .withMediationConfig(AdsFormat.Interstitial, "YOUR_AD_SPACE_ID")
                       .withMediationConfig(AdsFormat.Rewarded, "YOUR_AD_SPACE_ID"));
```
