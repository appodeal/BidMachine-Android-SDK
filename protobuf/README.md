# protobuf-android
Module for easy implement [Protocol Buffers]((https://developers.google.com/protocol-buffers/)) in Android.

### How to use:

Apply proto.gradle file to project (or module), where you have your proto files:

```
apply from: "../protobuf/proto.gradle"
```

Add library:

```
implementation fileTree(dir: '../protobuf/libs', include: ['*.jar'])
```

### Custom properties:

**protoJavaPackageFrom(String) / protoJavaPackageTo(String)** - params for rename package provided by 'option java_package'

```
gradle.ext {
    protoJarPackage = "com.appodeal.protobuf"
    protoJavaPackageFrom = "com.appodeal.protobuf"
    protoJavaPackageTo = "com.appodeal.protobuf"
}
...
apply from: "../protobuf/build.gradle"
```