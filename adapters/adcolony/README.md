## Overview

This folder contains mediation adapter used to mediate `AdColony`.

## Integration

[<img src="https://img.shields.io/badge/Min%20SDK%20version-1.3.3-brightgreen">](https://github.com/bidmachine/BidMachine-Android-SDK)
[<img src="https://img.shields.io/badge/Network%20Adapter%20version-1.3.3.2-brightgreen">](https://artifactory.bidmachine.io/bidmachine/io/bidmachine/ads.networks.adcolony/1.3.3.2/)
[<img src="https://img.shields.io/badge/Network%20version-3.3.11-blue">](https://github.com/AdColony/AdColony-Android-SDK-3)

Add next dependency to you `build.gradle`:

```groovy
dependencies {
    // ... other dependencies
    implementation 'io.bidmachine:ads.networks.adcolony:1.3.3.2'
}
```

Configure `AdColony` network:

```java
BidMachine.registerNetworks(
                new AdColonyConfig("YOUR_APP_ID")
                        .withMediationConfig(AdsFormat.InterstitialVideo, "YOUR_ZONE_ID")
                        .withMediationConfig(AdsFormat.RewardedVideo, "YOUR_ZONE_ID"));
```

## What's new in this version

Please view the [changelog](CHANGELOG.md) for details.