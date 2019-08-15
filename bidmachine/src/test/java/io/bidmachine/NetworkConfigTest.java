package io.bidmachine;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import io.bidmachine.banner.BannerSize;
import io.bidmachine.unified.UnifiedBannerAdRequestParams;
import io.bidmachine.unified.UnifiedFullscreenAdRequestParams;
import io.bidmachine.unified.UnifiedNativeAdRequestParams;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@RunWith(RobolectricTestRunner.class)
public class NetworkConfigTest {

    @Test
    public void constructor_params() {
        HashMap<String, String> params = new HashMap<>();
        params.put("TestKey", "TestValue");
        NetworkConfig networkConfig = new TestNetworkConfig(params);
        assertEquals("TestValue", networkConfig.getNetworkConfigParams().obtainNetworkParams().get("TestKey"));
    }

    @Test
    public void constructor_mediationParamsFromNetworkParams() {
        HashMap<String, String> params = new HashMap<>();
        params.put("TestKey", "TestValue");
        NetworkConfig networkConfig = new TestNetworkConfig(params);
        networkConfig.withMediationConfig(AdsFormat.Banner, new HashMap<String, String>());
        assertEquals("TestValue",
                     networkConfig.getNetworkConfigParams()
                                  .obtainNetworkMediationConfigs(AdsFormat.Banner)
                                  .get(AdsFormat.Banner)
                                  .get("TestKey"));
    }

    @Test
    public void constructor_mediationParamsNetworkParamsOverriding() {
        HashMap<String, String> params = new HashMap<>();
        params.put("TestKey", "TestValue");
        NetworkConfig networkConfig = new TestNetworkConfig(params);
        networkConfig.withMediationConfig(AdsFormat.Banner, new HashMap<String, String>() {{
            put("TestKey", "TestValue2");
        }});
        assertEquals("TestValue2",
                     networkConfig.getNetworkConfigParams()
                                  .obtainNetworkMediationConfigs(AdsFormat.Banner)
                                  .get(AdsFormat.Banner)
                                  .get("TestKey"));
    }

    @Test
    public void constructor_mediationParamsBaseMediationParamsOverriding() {
        NetworkConfig networkConfig = new TestNetworkConfig(null);
        networkConfig.withBaseMediationConfig(new HashMap<String, String>() {{
            put("TestKey", "TestValue");
        }});
        networkConfig.withMediationConfig(AdsFormat.Banner, new HashMap<String, String>() {{
            put("TestKey", "TestValue2");
        }});
        assertEquals("TestValue2",
                     networkConfig.getNetworkConfigParams()
                                  .obtainNetworkMediationConfigs(AdsFormat.Banner)
                                  .get(AdsFormat.Banner)
                                  .get("TestKey"));
    }

    @Test
    public void constructor_mediationParamsBaseMediationParamsNetworkParamsOverriding() {
        HashMap<String, String> params = new HashMap<>();
        params.put("TestKey", "TestValue");
        NetworkConfig networkConfig = new TestNetworkConfig(params);
        networkConfig.withBaseMediationConfig(new HashMap<String, String>() {{
            put("TestKey", "TestValue2");
        }});
        networkConfig.withMediationConfig(AdsFormat.Banner, new HashMap<String, String>() {{
            put("TestKey", "TestValue3");
        }});
        assertEquals("TestValue3",
                     networkConfig.getNetworkConfigParams()
                                  .obtainNetworkMediationConfigs(AdsFormat.Banner)
                                  .get(AdsFormat.Banner)
                                  .get("TestKey"));
    }

    @Test
    public void baseMediationParamsMerging() {
        NetworkConfig networkConfig = new TestNetworkConfig(null);
        networkConfig.withBaseMediationConfig(new HashMap<String, String>() {{
            put("TestKey1", "TestValue1");
        }});
        networkConfig.withMediationConfig(AdsFormat.Banner, new HashMap<String, String>() {{
            put("TestKey2", "TestValue2");
        }});
        Map<String, String> mediationParams = networkConfig.getNetworkConfigParams()
                                                           .obtainNetworkMediationConfigs(AdsFormat.Banner)
                                                           .get(AdsFormat.Banner);
        assertEquals("TestValue1", mediationParams.get("TestKey1"));
        assertEquals("TestValue2", mediationParams.get("TestKey2"));
    }

    @Test
    public void mediationConfigsContainsOnlyRequiredTypes() {
        NetworkConfig networkConfig = new TestNetworkConfig(null);
        networkConfig.withMediationConfig(AdsFormat.Banner_320x50, new HashMap<String, String>() {{
            put("TestKey", "TestValueBanner");
        }});
        networkConfig.withMediationConfig(AdsFormat.InterstitialStatic, new HashMap<String, String>() {{
            put("TestKey", "TestValueInterstitialStatic");
        }});
        networkConfig.withMediationConfig(AdsFormat.RewardedStatic, new HashMap<String, String>() {{
            put("TestKey", "TestValueRewardedStatic");
        }});
        EnumMap<AdsFormat, Map<String, String>> typedConfigs =
                networkConfig.getNetworkConfigParams().obtainNetworkMediationConfigs(AdsFormat.values());
        assertEquals(3, typedConfigs.size());
        assertEquals("TestValueBanner", typedConfigs.get(AdsFormat.Banner_320x50).get("TestKey"));
        assertEquals("TestValueInterstitialStatic", typedConfigs.get(AdsFormat.InterstitialStatic).get("TestKey"));
        assertEquals("TestValueRewardedStatic", typedConfigs.get(AdsFormat.RewardedStatic).get("TestKey"));
    }

    @Test
    public void mediationConfigsMergingDuringPeek() {
        NetworkConfig networkConfig = new TestNetworkConfig(null);
        networkConfig.withMediationConfig(AdsFormat.Banner, new HashMap<String, String>() {{
            put("TestKey", "TestValueBanner");
        }});
        networkConfig.withMediationConfig(AdsFormat.Interstitial, new HashMap<String, String>() {{
            put("TestKey", "TestValueInterstitialStatic");
        }});
        networkConfig.withMediationConfig(AdsFormat.Rewarded, new HashMap<String, String>() {{
            put("TestKey", "TestValueRewardedStatic");
        }});
        networkConfig.withMediationConfig(AdsFormat.Native, new HashMap<String, String>() {{
            put("TestKey", "TestValueNative");
        }});
        UnifiedBannerAdRequestParams bannerRequestParams = mock(UnifiedBannerAdRequestParams.class);
        doReturn(BannerSize.Size_320x50).when(bannerRequestParams).getBannerSize();
        assertEquals("TestValueBanner",
                     networkConfig.peekMediationConfig(AdsType.Banner, bannerRequestParams, AdContentType.All)
                                  .get("TestKey"));
        doReturn(BannerSize.Size_300x250).when(bannerRequestParams).getBannerSize();
        assertEquals("TestValueBanner",
                     networkConfig.peekMediationConfig(AdsType.Banner, bannerRequestParams, AdContentType.All)
                                  .get("TestKey"));
        doReturn(BannerSize.Size_728x90).when(bannerRequestParams).getBannerSize();
        assertEquals("TestValueBanner",
                     networkConfig.peekMediationConfig(AdsType.Banner, bannerRequestParams, AdContentType.All)
                                  .get("TestKey"));
        UnifiedFullscreenAdRequestParams fullscreenAdRequestParams = mock(UnifiedFullscreenAdRequestParams.class);
        assertEquals("TestValueInterstitialStatic",
                     networkConfig.peekMediationConfig(AdsType.Interstitial,
                                                       fullscreenAdRequestParams,
                                                       AdContentType.All)
                                  .get("TestKey"));
        assertEquals("TestValueInterstitialStatic",
                     networkConfig.peekMediationConfig(AdsType.Interstitial,
                                                       fullscreenAdRequestParams,
                                                       AdContentType.Video)
                                  .get("TestKey"));
        assertEquals("TestValueInterstitialStatic",
                     networkConfig.peekMediationConfig(AdsType.Interstitial,
                                                       fullscreenAdRequestParams,
                                                       AdContentType.Static)
                                  .get("TestKey"));
        assertEquals("TestValueRewardedStatic",
                     networkConfig.peekMediationConfig(AdsType.Rewarded, fullscreenAdRequestParams, AdContentType.All)
                                  .get("TestKey"));
        assertEquals("TestValueRewardedStatic",
                     networkConfig.peekMediationConfig(AdsType.Rewarded, fullscreenAdRequestParams, AdContentType.Video)
                                  .get("TestKey"));
        assertEquals("TestValueRewardedStatic",
                     networkConfig.peekMediationConfig(AdsType.Rewarded,
                                                       fullscreenAdRequestParams,
                                                       AdContentType.Static)
                                  .get("TestKey"));
        UnifiedNativeAdRequestParams nativeAdRequestParams = mock(UnifiedNativeAdRequestParams.class);
        assertEquals("TestValueNative",
                     networkConfig.peekMediationConfig(AdsType.Native, nativeAdRequestParams, AdContentType.All)
                                  .get("TestKey"));
    }

    @Test
    public void mediationConfigsNotMergingDuringPeekForSpecifiedFormats() {
        NetworkConfig networkConfig = new TestNetworkConfig(null);
        networkConfig.withMediationConfig(AdsFormat.Banner_320x50, new HashMap<String, String>() {{
            put("TestKey", "TestValueBanner320");
        }});
        networkConfig.withMediationConfig(AdsFormat.Banner_300x250, new HashMap<String, String>() {{
            put("TestKey", "TestValueBanner300");
        }});
        networkConfig.withMediationConfig(AdsFormat.Banner_728x90, new HashMap<String, String>() {{
            put("TestKey", "TestValueBanner728");
        }});
        networkConfig.withMediationConfig(AdsFormat.InterstitialVideo, new HashMap<String, String>() {{
            put("TestKey", "TestValueInterstitialVideo");
        }});
        networkConfig.withMediationConfig(AdsFormat.InterstitialStatic, new HashMap<String, String>() {{
            put("TestKey", "TestValueInterstitialStatic");
        }});
        networkConfig.withMediationConfig(AdsFormat.RewardedVideo, new HashMap<String, String>() {{
            put("TestKey", "TestValueRewardedVideo");
        }});
        networkConfig.withMediationConfig(AdsFormat.RewardedStatic, new HashMap<String, String>() {{
            put("TestKey", "TestValueRewardedStatic");
        }});
        UnifiedBannerAdRequestParams bannerRequestParams = mock(UnifiedBannerAdRequestParams.class);
        doReturn(BannerSize.Size_320x50).when(bannerRequestParams).getBannerSize();
        assertEquals("TestValueBanner320",
                     networkConfig.peekMediationConfig(AdsType.Banner, bannerRequestParams, AdContentType.All)
                                  .get("TestKey"));
        doReturn(BannerSize.Size_300x250).when(bannerRequestParams).getBannerSize();
        assertEquals("TestValueBanner300",
                     networkConfig.peekMediationConfig(AdsType.Banner, bannerRequestParams, AdContentType.All)
                                  .get("TestKey"));
        doReturn(BannerSize.Size_728x90).when(bannerRequestParams).getBannerSize();
        assertEquals("TestValueBanner728",
                     networkConfig.peekMediationConfig(AdsType.Banner, bannerRequestParams, AdContentType.All)
                                  .get("TestKey"));
        UnifiedFullscreenAdRequestParams fullscreenAdRequestParams = mock(UnifiedFullscreenAdRequestParams.class);
        assertNull(networkConfig.peekMediationConfig(AdsType.Interstitial,
                                                     fullscreenAdRequestParams,
                                                     AdContentType.All));
        assertEquals("TestValueInterstitialVideo",
                     networkConfig.peekMediationConfig(AdsType.Interstitial,
                                                       fullscreenAdRequestParams,
                                                       AdContentType.Video)
                                  .get("TestKey"));
        assertEquals("TestValueInterstitialStatic",
                     networkConfig.peekMediationConfig(AdsType.Interstitial,
                                                       fullscreenAdRequestParams,
                                                       AdContentType.Static)
                                  .get("TestKey"));
        assertNull(networkConfig.peekMediationConfig(AdsType.Rewarded, fullscreenAdRequestParams, AdContentType.All));
        assertEquals("TestValueRewardedVideo",
                     networkConfig.peekMediationConfig(AdsType.Rewarded, fullscreenAdRequestParams, AdContentType.Video)
                                  .get("TestKey"));
        assertEquals("TestValueRewardedStatic",
                     networkConfig.peekMediationConfig(AdsType.Rewarded,
                                                       fullscreenAdRequestParams,
                                                       AdContentType.Static)
                                  .get("TestKey"));
    }

    class TestNetworkConfig extends NetworkConfig {

        TestNetworkConfig(@Nullable Map<String, String> networkParams) {
            super(networkParams);
        }

        @NonNull
        @Override
        protected NetworkAdapter createNetworkAdapter() {
            return new TestNetworkAdapter();
        }
    }

    class TestNetworkAdapter extends NetworkAdapter {

        TestNetworkAdapter() {
            super("TestAdapter", "1", "1", AdsType.values());
        }
    }

}
