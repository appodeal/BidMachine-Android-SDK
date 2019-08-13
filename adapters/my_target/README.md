## Overview

This folder contains mediation adapter used to mediate `myTarget`.

## Integration

Add next dependency to you `build.gradle`:

```groovy
dependencies {
    // ... other dependencies
    implementation 'io.bidmachine:ads.networks.my_target:1.3.0.1'
}
```

Configure `myTarget` network:

```java
BidMachine.registerNetworks(
               new MyTargetConfig()
                       .withMediationConfig(AdsFormat.Banner, "YOUR_SLOT_ID")
                       .withMediationConfig(AdsFormat.Banner_320x50, "YOUR_SLOT_ID")
                       .withMediationConfig(AdsFormat.Banner_300x250, "YOUR_SLOT_ID")
                       .withMediationConfig(AdsFormat.Banner_728x90, "YOUR_SLOT_ID")
                       .withMediationConfig(AdsFormat.InterstitialStatic, "YOUR_SLOT_ID")
                       .withMediationConfig(AdsFormat.RewardedVideo, "YOUR_SLOT_ID");
```
