# Proguard rules for Gallery & File Manager app
-keepattributes *Annotation*
-keepclassmembers class * {
    @androidx.room.* <methods>;
}
-keep class com.amitbharat.gallery.data.models.** { *; }
-dontwarn com.bumptech.glide.**
