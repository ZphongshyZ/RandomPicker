# Keep line numbers for crash reports
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Gson serialization - keep data classes used for JSON
-keep class com.hntech.pickora.data.repository.HistoryEntry { *; }
-keep class com.hntech.pickora.data.repository.SavedList { *; }

# Keep Koin
-keep class org.koin.** { *; }

# Keep Compose
-dontwarn androidx.compose.**
