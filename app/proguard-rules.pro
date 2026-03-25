# Keep line numbers for crash reports
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Gson serialization - keep data classes used for JSON
-keep class com.hntech.pickora.data.repository.HistoryEntry { *; }
-keep class com.hntech.pickora.data.repository.SavedList { *; }
-keepattributes Signature
-keepattributes *Annotation*

# Keep Koin
-keep class org.koin.core.** { *; }
-keep class org.koin.java.** { *; }

# Google Play Services / AdMob
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**
-keep class com.google.ads.** { *; }

# Compose
-dontwarn androidx.compose.**
