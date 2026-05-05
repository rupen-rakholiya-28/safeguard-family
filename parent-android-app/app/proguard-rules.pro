# SafeGuard Parent - ProGuard Rules

-keep,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

-keep class com.childprotection.parent.network.** { *; }
-keep class com.childprotection.parent.data.** { *; }

-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }

-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

-keep class com.google.firebase.** { *; }
