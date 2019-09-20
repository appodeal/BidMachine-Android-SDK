## Overview

This folder contains mediation adapter used to mediate `Tapjoy`.

## Integration

[<img src="https://img.shields.io/badge/Min%20SDK%20version-1.3.3-brightgreen">](https://github.com/bidmachine/BidMachine-Android-SDK)
[<img src="https://img.shields.io/badge/Network%20Adapter%20version-1.3.3.2-brightgreen">](https://artifactory.bidmachine.io/bidmachine/io/bidmachine/ads.networks.my_target/1.3.3.2/)
[<img src="https://img.shields.io/badge/Network%20version-12.3.1-blue">](https://dev.tapjoy.com/sdk-integration/android/)

Add next dependency to you `build.gradle`:

```groovy
dependencies {
    // ... other dependencies
    implementation 'io.bidmachine:ads.networks.tapjoy:1.3.3.2'
}
```

Configure `Tapjoy` network:

```java
BidMachine.registerNetworks(
               new TapJoyConfig("YOUR_SDK_KEY")
                       .withMediationConfig(AdsFormat.InterstitialVideo, "YOUR_PLACEMENT_NAME")
                       .withMediationConfig(AdsFormat.RewardedVideo, "YOUR_PLACEMENT_NAME"));
```

## What's new in this version

Please view the [changelog](CHANGELOG.md) for details.
