## Overview

This folder contains mediation adapter used to mediate `Tapjoy`.

## Integration

Add next dependency to you `build.gradle`:

```groovy
dependencies {
    // ... other dependencies
    implementation 'io.bidmachine:ads.networks.tapjoy:1.3.0.1'
}
```

Configure `Tapjoy` network:

```java
BidMachine.registerNetworks(
               new TapJoyConfig("YOUR_SDK_KEY")
                       .withMediationConfig(AdsFormat.InterstitialVideo, "YOUR_PLACEMENT_NAME")
                       .withMediationConfig(AdsFormat.RewardedVideo, "YOUR_PLACEMENT_NAME"));
```
