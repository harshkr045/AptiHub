# Add project specific ProGuard rules here.
# You can find more samples at https://r8.googlesource.com/r8/+/main/compatibility-faq.md

# Keep this to avoid some warnings that may occur with older versions of Guava.
-dontwarn com.google.common.util.concurrent.internal.InternalFutureFailureAccess
-dontwarn com.google.common.util.concurrent.internal.InternalFutures

# Add this rule to prevent R8 from removing classes required by Firestore.
-keep public class com.google.firebase.firestore.** { *; }

# For Kotlin, keep the data classes and their properties.
-keepnames class com.harshkr.aptihub.Question
-keepclassmembers class com.harshkr.aptihub.Question {
    public <init>();
    <fields>;
}

# The following rules are recommended for Firebase Auth
-keep class com.google.firebase.auth.** { *; }
-keep class com.google.android.gms.internal.firebase-auth.** { *; }

# The following rule is needed when using the Google Sign In
-keep class com.google.android.gms.auth.api.signin.internal.* { *; }
-keep class com.google.android.gms.auth.api.signin.* { *; }

# Keep annotation classes
-keepclassmembers class javax.annotation.Nullable {
    *;
}
-keepclassmembers class javax.annotation.Nonnull {
    *;
}
