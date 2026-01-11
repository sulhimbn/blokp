# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# ========================================================
# SECURITY HARDENING RULES
# ========================================================

# Remove all logging from release builds to prevent information leakage
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
    public static int e(...);
}

# Remove debugging code from release builds
-assumenosideeffects class com.example.iurankomplek.utils.LoggingUtils {
    public static void logNetworkSecurityWarning(...);
}

# Keep security-related classes and methods but obfuscate names
-keep,allowobfuscation class com.example.iurankomplek.utils.SecurityManager
-keep,allowobfuscation class com.example.iurankomplek.network.SecurityConfig

# Keep certificate pinning configuration
-keep class com.example.iurankomplek.network.SecurityConfig {
    public okhttp3.OkHttpClient getSecureOkHttpClient();
}

# ========================================================
# ENCRYPTION & CRYPTOGRAPHY
# ========================================================

# Keep cryptographic operations but obfuscate implementation details
-keep,allowobfuscation class * extends java.security.Signature
-keep,allowobfuscation class * extends javax.crypto.Cipher

# ========================================================
# NETWORK & API SECURITY
# ========================================================

# Keep certificate pinning (don't optimize away)
-keep class okhttp3.CertificatePinner {
    public *;
}

# Keep network interceptors for security headers
-keep class com.example.iurankomplek.network.SecurityConfig$** {
    public *;
}

# ========================================================
# PAYMENT SECURITY
# ========================================================

# Keep payment-related classes but obfuscate internal logic
-keep,allowobfuscation class com.example.iurankomplek.payment.** {
    public *;
}

# Keep transaction models for serialization
-keepclassmembers class com.example.iurankomplek.transaction.Transaction {
    public *;
}

# Keep payment status enums
-keep enum com.example.iurankomplek.payment.** { *; }

# ========================================================
# DATA VALIDATION
# ========================================================

# Keep validation utilities
-keep class com.example.iurankomplek.utils.DataValidator {
    public static *** sanitize*(...);
    public static *** isValid*(...);
    public static *** format*(...);
}

# ========================================================
# INPUT SANITIZATION
# ========================================================

# Keep input validation logic but obfuscate implementation
-keep,allowobfuscation class com.example.iurankomplek.utils.** {
    public static *** sanitize*(...);
    public static *** validate*(...);
}

# ========================================================
# RATE LIMITING (SECURITY-002)
# ========================================================

# Keep rate limiter logic but obfuscate implementation
-keep,allowobfuscation class com.example.iurankomplek.utils.RateLimiter {
    public *;
}

-keep,allowobfuscation class com.example.iurankomplek.utils.MultiLevelRateLimiter {
    public *;
}

# Keep rate limiter interceptor for network layer
-keep,allowobfuscation class com.example.iurankomplek.network.interceptor.RateLimiterInterceptor {
    public *;
}

# ========================================================
# KOTLIN & COROUTINES
# ========================================================

# Keep Kotlin coroutines for proper lifecycle management
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# ========================================================
# JSON SERIALIZATION
# ========================================================

# Keep kotlinx.serialization classes
-keepattributes *Annotation*
-keep class kotlinx.serialization.json.** { *; }
-keep @kotlinx.serialization.Serializable class * { *; }

# Keep JSON models with @Serializable annotation
-keep @kotlinx.serialization.Serializable class com.example.iurankomplek.** { *; }

# ========================================================
# OPTIMIZATION RULES
# ========================================================

# Aggressive optimization for release builds
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-verbose

# Optimize code but preserve essential functionality
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*

# Remove unused code
-dontwarn javax.xml.**