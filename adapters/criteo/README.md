## Overview

This folder contains mediation adapter used to mediate `Criteo`.

## Integration

[<img src="https://img.shields.io/badge/Min%20SDK%20version-1.3.3-brightgreen">](https://github.com/bidmachine/BidMachine-Android-SDK)
[<img src="https://img.shields.io/badge/Network%20Adapter%20version-1.3.0.1-brightgreen">](https://artifactory.bidmachine.io/bidmachine/io/bidmachine/ads.networks.my_target/1.3.0.1/)

Configure `Criteo` network:

```java
BidMachine.registerNetworks(
               new CriteoConfig("YOUR_SENDER_ID"));
```
