## Overview

This folder contains mediation adapter used to mediate `AdColony`.

## Integration

Add next dependency to you `build.gradle`:

```groovy
dependencies {
    // ... other dependencies
    implementation 'io.bidmachine:ads.networks.adcolony:1.3.0.1'
}
```

Configure `AdColony` network:

```java
BidMachine.registerNetworks(
                new AdColonyConfig("YOUR_APP_ID")
                        .withMediationConfig(AdsFormat.InterstitialVideo, "YOUR_ZONE_ID")
                        .withMediationConfig(AdsFormat.RewardedVideo, "YOUR_ZONE_ID"));
```
