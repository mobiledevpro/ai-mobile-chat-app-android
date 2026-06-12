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

# To keep Protobuf
-keepclassmembers class * extends androidx.datastore.preferences.protobuf.GeneratedMessageLite {
    <fields>;
}

# Remove all logs
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}

#Crashlytics
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception

-keepattributes SourceFile,LineNumberTable        # Keep file names and line numbers.
-keep public class * extends java.lang.Exception  # Optional: Keep custom exceptions.

-dontwarn com.itextpdf**
-keep class com.itextpdf** { *; }
-keep interface com.itextpdf** { *; }
-keep class org.bouncycastle.** { *; }

# It fixes the fail with compiling the buils: Missing class org.slf4j.impl.StaticLoggerBinder
-dontwarn org.slf4j.impl.StaticLoggerBinder

# It removes Compose Compiler tracing strings and descrease APK size
-assumenosideeffects public class androidx.compose.runtime.ComposerKt {
   boolean isTraceInProgress();
   void traceEventStart(int,int,int,java.lang.String);
   void traceEventStart(int,java.lang.String);
   void traceEventEnd();

}

# To work with iTextPDF
# Keep Xerces implementation classes used by JAXP / iText
-keep class org.apache.xerces.** { *; }
-dontwarn org.apache.xerces.**

# Keep service provider files (JAXP uses META-INF/services to discover implementations)
-keepdirectories META-INF/services

# Keep JAXP factory classes (prevent renaming issues)
-keep class javax.xml.parsers.** { *; }
-keep class org.xml.sax.** { *; }
-keep class org.w3c.dom.** { *; }