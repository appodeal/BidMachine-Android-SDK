# BidMachine Proguard config

# Keep public classes and methods.
-keep public class io.bidmachine.**
-keepclassmembers class io.bidmachine.** {*;}
-keepattributes EnclosingMethod, InnerClasses, Signature, JavascriptInterface
-keep class com.google.protobuf.** {*;}
-keep class io.bidmachine.protobuf.**
-keep class com.explorestack.protobuf.**
-keepclassmembers class com.explorestack.protobuf.** {*;}

# Support for Android Advertiser ID.
-keep class com.google.android.gms.common.GooglePlayServicesUtil {*;}
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient {*;}
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient$Info {*;}