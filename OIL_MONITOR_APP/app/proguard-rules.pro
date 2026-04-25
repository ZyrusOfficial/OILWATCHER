# Add project specific ProGuard rules here.

# Keep Hilt generated code
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# Keep Firebase
-keep class com.google.firebase.** { *; }

# Keep ML Kit
-keep class com.google.mlkit.** { *; }

# Keep Gson
-keepattributes Signature
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Keep data classes for serialization
-keep class com.oilwatcher.monitor.domain.model.** { *; }

# Keep WebView JavaScript interface
-keepclassmembers class com.oilwatcher.monitor.presentation.webview.AndroidBridge {
    @android.webkit.JavascriptInterface <methods>;
}

# Keep R8/ProGuard from stripping interface information
-keepclassmembernames class * {
    @android.webkit.JavascriptInterface <methods>;
}
