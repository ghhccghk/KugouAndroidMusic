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

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-keep class com.ghhccghk.musicplay.data.** { *; }
-keep class com.ghhccghk.musicplay.util.NodeBridge {
    *;
}
-keep class com.hchen.superlyricapi.** { *;}

# 避免 kotlin.reflect 内部反射崩溃
-keepattributes *Annotation*


# 避免 META-INF/services 文件冲突
-keepnames class * implements java.util.ServiceLoader

# 保留 Kotlin metadata，避免反射失败
-keepclassmembers class ** {
    @kotlin.Metadata *;
}
-keep class kotlin.Metadata

# JNI
-keep class org.nift4.gramophone.hificore.NativeTrack {
    onAudioDeviceUpdate(...);
    onUnderrun(...);
    onMarker(...);
    onNewPos(...);
    onStreamEnd(...);
    onNewIAudioTrack(...);
    onNewTimestamp(...);
    onLoopEnd(...);
    onBufferEnd(...);
    onMoreData(...);
    onCanWriteMoreData(...);
}
# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
# -renamesourcefileattribute SourceFile

-dontobfuscate
# reflection by androidx via theme attr viewInflaterClass
-keep class com.ghhccghk.musicplay.ui.components.** { *; }
-keep class com.ghhccghk.musicplay.ui.widgets.** { *; }
# reflection by lyric getter xposed
-keep class androidx.media3.common.util.Util {
    public static void setForegroundServiceNotification(...);
}
