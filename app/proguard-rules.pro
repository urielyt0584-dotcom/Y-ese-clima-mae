# ProGuard & R8 configuration for ¿Y Ese Clima, Mae?

# Moshi rules
-dontwarn com.squareup.moshi.**
-keep class com.squareup.moshi.** { *; }
-keep interface com.squareup.moshi.** { *; }

# Retain generated Moshi adapters
-keep class *JsonAdapter { *; }
-keep class **$$JsonObjectAdapter { *; }
-keepclasseswithmembers class * {
    @com.squareup.moshi.Json <fields>;
}
-keep @com.squareup.moshi.JsonClass class * { *; }
-keepclassmembers @com.squareup.moshi.JsonClass class * {
    <init>(...);
}

# Keep all domain and network data models
-keep class com.example.data.model.** { *; }
-keepclassmembers class com.example.data.model.** { *; }

# Retrofit rules
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# OkHttp & Okio
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# Room Database rules
-keep class androidx.room.** { *; }
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }
-keep class com.example.data.local.** { *; }
-keepclassmembers class com.example.data.local.** { *; }

# Glance / AppWidget
-keep class com.example.widget.** { *; }
-keepclassmembers class com.example.widget.** { *; }

# Keep line numbers for crash reporting
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Firebase & Credential Manager
-dontwarn com.google.firebase.**
-keep class com.google.firebase.** { *; }
-keep class androidx.credentials.** { *; }
-keep class com.google.android.libraries.identity.googleid.** { *; }
