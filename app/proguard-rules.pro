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

# Please add these rules to your existing keep rules in order to suppress warnings.
# This is generated automatically by the Android Gradle plugin.
-dontwarn javax.annotation.processing.AbstractProcessor
-dontwarn javax.annotation.processing.SupportedAnnotationTypes
-dontwarn javax.lang.model.SourceVersion
-dontwarn javax.lang.model.element.Element
-dontwarn javax.lang.model.element.ElementKind
-dontwarn javax.lang.model.element.Modifier
-dontwarn javax.lang.model.type.TypeMirror
-dontwarn javax.lang.model.type.TypeVisitor
-dontwarn javax.lang.model.util.SimpleTypeVisitor8


############  Our Model classes ###########
-keep class com.makinul.background.remover.data.model.** { *; }
-keep class com.makinul.background.remover.data.network.** { *; }
-keep class com.makinul.background.remover.data.Data.** { *; }
-keep class com.makinul.background.remover.data.Event.** { *; }
-keep class com.makinul.background.remover.data.Resource.** { *; }


############ Media Pipe ###########

# Keep public members of our public interfaces. This also prevents the
# obfuscation of the corresponding methods in classes implementing them,
# such as implementations of PacketCallback#process.
-keep public interface com.google.mediapipe.framework.* {
  public *;
}

# This method is invoked by native code.
-keep public class com.google.mediapipe.framework.Packet {
  public static *** create(***);
  public long getNativeHandle();
  public void release();
}

# This method is invoked by native code.
-keep public class com.google.mediapipe.framework.PacketCreator {
  *** releaseWithSyncToken(...);
}

# This method is invoked by native code.
-keep public class com.google.mediapipe.framework.MediaPipeException {
  <init>(int, byte[]);
}

# Required to use PacketCreator#createProto
-keep class com.google.mediapipe.framework.ProtoUtil$SerializedMessage { *; }

# custom rules
#-keep class com.google.mediapipe.tasks.vision.poselandmarker.** { *; }
-keep public class com.google.mediapipe.** { *; }
-keep public class com.google.mediapipe.framework.Graph { *; }
-keep public class com.google.common.** { *; }
-keep public interface com.google.common.** { *; }

-keep class com.google.mediapipe.solutioncore.** {*;}
-keep class com.google.protobuf.** {*;}
-keepclassmembers class * extends com.google.protobuf.GeneratedMessageLite {*;}

-dontwarn com.google.mediapipe.proto.CalculatorProfileProto$CalculatorProfile
-dontwarn com.google.mediapipe.proto.GraphTemplateProto$CalculatorGraphTemplate
