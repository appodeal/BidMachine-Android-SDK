package io.bidmachine.test.app;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import io.bidmachine.BuildConfig;
import io.bidmachine.*;
import io.bidmachine.adapters.my_target.MyTargetAdapter;
import io.bidmachine.banner.BannerSize;
import io.bidmachine.banner.BannerView;
import io.bidmachine.nativead.NativeAd;
import io.bidmachine.nativead.NativeAdContentLayout;
import io.bidmachine.nativead.NativeListener;
import io.bidmachine.nativead.NativeRequest;
import io.bidmachine.nativead.view.NativeIconView;
import io.bidmachine.nativead.view.NativeMediaView;
import io.bidmachine.test.app.params.*;
import io.bidmachine.test.app.utils.TestActivityWrapper;
import io.bidmachine.utils.BMError;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity {

    private static final String SAVED_StaticMode = "StaticMode";

    private ViewGroup bannerFrame;
    private LinearLayout nativeAdParent;
    private RadioGroup bannerSizesParent;
    private RadioGroup interstitialFormatParent;
    private TextView txtLocation;

    private RequestHelper requestHelper;
    private boolean isStaticMode;
    private boolean isResumed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            setFinalStatic(Class.forName("io.bidmachine.BuildConfig").getDeclaredField("DEBUG"),
                    true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        BidMachine.registerAdapter(
                new NetworkConfig(new MyTargetAdapter())
                        .withMediationConfig(AdsFormat.Banner, new HashMap<String, Object>() {{
                            put("slot_id", "437933");
                        }})
                        .withMediationConfig(AdsFormat.RewardedVideo, new HashMap<String, Object>() {{
                            put("slot_id", "482205");
                        }}));

        final SpannableStringBuilder appInfoBuilder = new SpannableStringBuilder();
        appInfoBuilder.append("Version: ");
        appendBold(appInfoBuilder, BidMachine.VERSION).append("   ");
        appInfoBuilder.append("Build: ");
        appendBold(appInfoBuilder, BuildConfig.VERSION_CODE);

        this.<TextView>findViewById(R.id.txtAppInfo).setText(appInfoBuilder);
        bannerFrame = findViewById(R.id.bannerFrame);
        nativeAdParent = findViewById(R.id.nativeAdParent);

        this.<SwitchCompat>findViewById(R.id.switchTestMode).setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        BidMachine.setTestMode(isChecked);
                    }
                });

        bannerSizesParent = findViewById(R.id.bannerSizesParent);
        for (BannerSize size : BannerSize.values()) {
            final RadioButton radioButton = new RadioButton(this);
            int id = ("rbtn" + size.name()).hashCode();
            radioButton.setId(id < 0 ? -id : id);
            radioButton.setText(size.name()
                    .replace("Size_", "")
                    .replace("_", " x "));
            radioButton.setTag(size);
            bannerSizesParent.addView(radioButton,
                    new RadioGroup.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f));
        }
        if (savedInstanceState == null) {
            bannerSizesParent.check(bannerSizesParent.getChildAt(0).getId());
        }

        interstitialFormatParent = findViewById(R.id.interstitialFormatParent);
        for (AdContentType format : AdContentType.values()) {
            final RadioButton radioButton = new RadioButton(this);
            int id = ("rbtn" + format.name()).hashCode();
            radioButton.setId(id < 0 ? -id : id);
            radioButton.setText(format.name());
            radioButton.setTag(format);
            interstitialFormatParent.addView(radioButton,
                    new RadioGroup.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f));
        }
        if (savedInstanceState == null) {
            interstitialFormatParent.check(interstitialFormatParent.getChildAt(0).getId());
        }

        this.<SwitchCompat>findViewById(R.id.switchStaticMode).setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        setStaticMode(isChecked);
                    }
                });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            final ArrayList<String> requiredPermissions = new ArrayList<String>() {{
                add(Manifest.permission.ACCESS_COARSE_LOCATION);
                add(Manifest.permission.ACCESS_FINE_LOCATION);
            }};
            final Iterator<String> requiredPermissionsIterator = requiredPermissions.iterator();
            while (requiredPermissionsIterator.hasNext()) {
                String permission = requiredPermissionsIterator.next();
                if (ContextCompat.checkSelfPermission(this, permission) ==
                        PackageManager.PERMISSION_GRANTED) {
                    requiredPermissionsIterator.remove();
                }
            }
            if (!requiredPermissions.isEmpty()) {
                ActivityCompat.requestPermissions(this, requiredPermissions.toArray(new String[0]), 1);
            }
        }

        txtLocation = findViewById(R.id.txtLocation);
        txtLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateCurrentLocation();
            }
        });

        getSupportFragmentManager().addOnBackStackChangedListener(
                new FragmentManager.OnBackStackChangedListener() {
                    @Override
                    public void onBackStackChanged() {
                        syncBackIcon();
                    }
                });
        syncBackIcon();

        if (savedInstanceState != null) {
            isStaticMode = savedInstanceState.getBoolean(SAVED_StaticMode);
        }
        setStaticMode(isStaticMode);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SAVED_StaticMode, isStaticMode);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isResumed = true;
        //Required for display toast only!!!
        requestHelper.setActivity(this);
        updateCurrentLocation();
    }

    @Override
    protected void onPause() {
        super.onPause();
        isResumed = false;
        //Required for display toast only!!!
        requestHelper.setActivity(null);
    }

    private void setStaticMode(boolean staticMode) {
        isStaticMode = staticMode;
        if (isStaticMode) {
            requestHelper = RequestHelper.obtainStaticInstance(this);
        } else {
            requestHelper = new RequestHelper(this);
        }
        syncRequestHelper(requestHelper);
    }

    private void syncRequestHelper(RequestHelper requestHelper) {
        if (isResumed) requestHelper.setActivity(this);
        bannerFrame.removeAllViews();
        final BannerView bannerView = requestHelper.getBannerView();
        if (bannerView.getParent() instanceof ViewGroup) {
            ((ViewGroup) bannerView.getParent()).removeView(bannerView);
        }
        bannerFrame.addView(requestHelper.getBannerView());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void syncBackIcon() {
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(
                    getSupportFragmentManager().getBackStackEntryCount() > 0);
        }
    }

    /*
    Global params
     */

    public void showTargetingParams(View view) {
        showParamsFragment(new TargetingParamsFragment(), "TargetingParamsFragment");
    }

    public void showUserRestrictionsParams(View view) {
        showParamsFragment(new UserRestrictionsParamsFragment(), "UserRestrictionsParamsFragment");
    }

    public void showExtraParams(View view) {
        showParamsFragment(new ExtraParamsFragment(), "ExtraParamsFragment");
    }

    private void showParamsFragment(Fragment fragment, String tag) {
        getSupportFragmentManager().beginTransaction()
                .add(R.id.parent, fragment, tag)
                .addToBackStack(tag)
                .commitAllowingStateLoss();
    }

    public void loadBanner(View view) {
        BannerSize bannerSize = (BannerSize) bannerSizesParent
                .findViewById(bannerSizesParent.getCheckedRadioButtonId()).getTag();
        requestHelper.loadBanner(bannerSize, false);
    }

    public void showBanner(View view) {
        requestHelper.showBanner();
    }

    public void requestBanner(View view) {
        BannerSize bannerSize = (BannerSize) bannerSizesParent
                .findViewById(bannerSizesParent.getCheckedRadioButtonId()).getTag();
        requestHelper.requestBanner(bannerSize);
    }

    public void showRequestedBanner(View view) {
        requestHelper.showPendingBanner();
    }

    public void hideBanner(View view) {
        requestHelper.hideBanner();
    }

    public void showBannerAutoLoad(View view) {
        startActivity(new Intent(this, BannerAutoLoadActivity.class));
    }

    public void loadInterstitial(View view) {
        final AdContentType contentType = (AdContentType) interstitialFormatParent
                .findViewById(interstitialFormatParent.getCheckedRadioButtonId()).getTag();
        requestHelper.loadInterstitial(contentType, false);
    }

    public void showInterstitial(View view) {
        requestHelper.showInterstitial();
    }

    public void requestInterstitial(View view) {
        final AdContentType contentType = (AdContentType) interstitialFormatParent
                .findViewById(interstitialFormatParent.getCheckedRadioButtonId()).getTag();
        requestHelper.requestInterstitial(contentType);
    }

    public void showRequestedInterstitial(View view) {
        requestHelper.showPendingInterstitial();
    }

    public void loadRewarded(View view) {
        requestHelper.loadRewarded(false);
    }

    public void showRewarded(View view) {
        requestHelper.showRewarded();
    }

    public void requestRewarded(View view) {
        requestHelper.requestRewarded();
    }

    public void showRequestedRewarded(View view) {
        requestHelper.showPendingRewarded();
    }

    /*
    Native
     */

    public void showNative(View view) {
        hideNative(null);
        final NativeRequest request = ParamsHelper.getInstance(this, ParamsHelper.AdsType.Native)
                .createParams(new NativeRequest.Builder().setMediaAssetTypes(MediaAssetType.All))
                .build();
        new NativeAd(this)
                .setListener(new NativeListener() {
                    @Override
                    public void onAdLoaded(@NonNull NativeAd ad) {
                        Utils.showToast(MainActivity.this, "onAdLoaded");
//                        inflateNativeAd(ad);
                        inflateNativeAdWithBind(ad);
                    }

                    @Override
                    public void onAdShown(@NonNull NativeAd ad) {
                        Utils.showToast(MainActivity.this, "onAdShown");
                    }

                    @Override
                    public void onAdImpression(@NonNull NativeAd ad) {
                        Utils.showToast(MainActivity.this, "onAdImpression");
                    }

                    @Override
                    public void onAdLoadFailed(@NonNull NativeAd ad, @NonNull BMError error) {
                        Utils.showToast(MainActivity.this, "onAdLoadFailed: " + error.getMessage());
                    }

                    @Override
                    public void onAdClicked(@NonNull NativeAd ad) {
                        Utils.showToast(MainActivity.this, "onAdClicked");
                    }

                    @Override
                    public void onAdExpired(@NonNull NativeAd ad) {
                        Utils.showToast(MainActivity.this, "onAdExpired");
                    }
                })
                .load(request);
    }

    private void inflateNativeAd(NativeAd ad) {
        if (ad == null) {
            return;
        }
        final NativeAdContentLayout nativeAdContentLayout =
                (NativeAdContentLayout) getLayoutInflater().inflate(
                        R.layout.include_native_ads, nativeAdParent, false);

        final TextView tvTitle = nativeAdContentLayout.findViewById(R.id.txtTitle);
        tvTitle.setText(ad.getTitle());
        nativeAdContentLayout.setTitleView(tvTitle);

        final TextView tvDescription = nativeAdContentLayout.findViewById(R.id.txtDescription);
        tvDescription.setText(ad.getDescription());
        nativeAdContentLayout.setDescriptionView(tvDescription);

        final RatingBar ratingBar = nativeAdContentLayout.findViewById(R.id.ratingBar);
        if (ad.getRating() == 0) {
            ratingBar.setVisibility(View.INVISIBLE);
        } else {
            ratingBar.setVisibility(View.VISIBLE);
            ratingBar.setRating(ad.getRating());
            ratingBar.setStepSize(0.1f);
        }
        nativeAdContentLayout.setRatingView(ratingBar);

        final Button ctaButton = nativeAdContentLayout.findViewById(R.id.btnCta);
        ctaButton.setText(ad.getCallToAction());
        nativeAdContentLayout.setCallToActionView(ctaButton);

        final NativeIconView icon = nativeAdContentLayout.findViewById(R.id.icon);
        nativeAdContentLayout.setIconView(icon);

        //TOOD: it's should be ViewGroup?
        final View providerView = ad.getProviderView(nativeAdParent.getContext());
        if (providerView != null) {
            if (providerView.getParent() != null && providerView.getParent() instanceof ViewGroup) {
                ((ViewGroup) providerView.getParent()).removeView(providerView);
            }
            FrameLayout providerViewContainer = nativeAdContentLayout.findViewById(R.id.providerView);
            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            providerViewContainer.addView(providerView, layoutParams);
        }
        nativeAdContentLayout.setProviderView(providerView);

        TextView tvAgeRestrictions = nativeAdContentLayout.findViewById(R.id.txtAgeRestriction);
        if (ad.getAgeRestrictions() != null) {
            tvAgeRestrictions.setText(ad.getAgeRestrictions());
            tvAgeRestrictions.setVisibility(View.VISIBLE);
        } else {
            tvAgeRestrictions.setVisibility(View.GONE);
        }

        NativeMediaView nativeMediaView = nativeAdContentLayout.findViewById(R.id.mediaView);
        nativeAdContentLayout.setMediaView(nativeMediaView);

        nativeAdContentLayout.registerViewForInteraction(ad);
        nativeAdContentLayout.setVisibility(View.VISIBLE);

        nativeAdParent.removeAllViews();
        nativeAdParent.addView(nativeAdContentLayout);
    }

    private void inflateNativeAdWithBind(NativeAd ad) {
        if (ad == null) {
            return;
        }
        final NativeAdContentLayout nativeAdContentLayout =
                (NativeAdContentLayout) getLayoutInflater().inflate(
                        R.layout.include_native_ads, nativeAdParent, false);
        final TextView tvAgeRestrictions = nativeAdContentLayout.findViewById(R.id.txtAgeRestriction);
        if (ad.getAgeRestrictions() != null) {
            tvAgeRestrictions.setText(ad.getAgeRestrictions());
            tvAgeRestrictions.setVisibility(View.VISIBLE);
        } else {
            tvAgeRestrictions.setVisibility(View.GONE);
        }
        nativeAdContentLayout.bind(ad);
        nativeAdContentLayout.registerViewForInteraction(ad);
        nativeAdContentLayout.setVisibility(View.VISIBLE);
        nativeAdParent.removeAllViews();
        nativeAdParent.addView(nativeAdContentLayout);
    }

    public void hideNative(View view) {
        int childCount = nativeAdParent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            NativeAdContentLayout child = (NativeAdContentLayout) nativeAdParent.getChildAt(i);
            child.unregisterViewForInteraction();
            child.destroy();
        }
        nativeAdParent.removeAllViews();
    }

    public void isRegisteredNative(View view) {
        boolean isRegistered = false;
        if (nativeAdParent.getChildCount() > 0) {
            isRegistered = ((NativeAdContentLayout) nativeAdParent.getChildAt(0)).isRegistered();
        }

        Utils.showToast(this, String.valueOf(isRegistered));
    }

    private SpannableStringBuilder appendBold(SpannableStringBuilder builder, Object text) {
        int startLength = builder.length();
        builder.append(String.valueOf(text));
        builder.setSpan(new StyleSpan(Typeface.BOLD), startLength, builder.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.setSpan(new ForegroundColorSpan(Color.BLACK), startLength, builder.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return builder;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!isStaticMode) requestHelper.destroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        updateCurrentLocation();
    }

    public void initSdk(View view) {
        BidMachine.initialize(new TestActivityWrapper(this), ParamsHelper.getInstance(this).getSellerId());
    }

    public void showAppParams(View view) {
        showParamsFragment(new AppParamsFragment(), "AppParamsFragment");
    }

    public void showBannerParams(View view) {
        showParamsFragment(AdsParamsFragment.create(ParamsHelper.AdsType.Banner), "BannerAdsFragment");
    }

    public void showInterstitialParams(View view) {
        showParamsFragment(AdsParamsFragment.create(ParamsHelper.AdsType.Interstitial), "InterstitialAdsFragment");
    }

    public void showRewardedParams(View view) {
        showParamsFragment(AdsParamsFragment.create(ParamsHelper.AdsType.Rewarded), "RewardedAdsFragment");
    }

    public void showNativeParams(View view) {
        showParamsFragment(AdsParamsFragment.create(ParamsHelper.AdsType.Native), "NativeAdsFragment");
    }

    @SuppressLint("SetTextI18n")
    private void updateCurrentLocation() {
        DecimalFormat df = new DecimalFormat("####0.000");
        Location location = Utils.getLocation(this);
        if (location != null) {
            txtLocation.setText(new StringBuilder()
                    .append(df.format(location.getLatitude()))
                    .append("\n")
                    .append(df.format(location.getLongitude())));
        } else {
            txtLocation.setText("Location not available");
        }
    }

    public void showBannerScrollable(View view) {
        startActivityForResult(new Intent(this, BannerScrollableActivity.class), 0);
    }

    private static void setFinalStatic(Field field, Object newValue) throws Exception {
        field.setAccessible(true);
        try {
            Field modifiersField = Field.class.getDeclaredField("slot");
            modifiersField.setAccessible(true);
            modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                Field modifiersField = Field.class.getDeclaredField("accessFlags");
                modifiersField.setAccessible(true);
                modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        field.set(null, newValue);
    }

}