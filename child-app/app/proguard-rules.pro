# SafeGuard Child - ProGuard Rules

# Keep Retrofit interfaces
-keep,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Keep Gson serialization models
-keep class com.childprotection.child.network.** { *; }

# Keep data classes
-keep class com.childprotection.child.data.** { *; }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**

# Retrofit
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Firebase
-keep class com.google.firebase.** { *; }
