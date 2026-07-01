# ============================================================
# ProGuard Rules for NeuroApplication (Optimised)
# ============================================================

# ---- GENERAL SETTINGS ----
-dontoptimize
-dontshrink

# Keep essential metadata for Reflection, Hibernate, and Log4j
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod,SourceFile,LineNumberTable

# ---- ENTRY POINT ----
-keepclasseswithmembers public class com.neuro.application.NeuroApplication {
    public static void main(java.lang.String[]);
}

# ---- THIRD-PARTY LIBRARIES SAFETY ----
# Instead of keeping "not com.neuro", we tell ProGuard to ignore warnings
# from dependencies and allow it to safely process your code.
-dontwarn org.hibernate.**
-dontwarn jakarta.persistence.**
-dontwarn com.mysql.**
-dontwarn org.apache.pdfbox.**
-dontwarn org.apache.logging.log4j.**

# ---- HIBERNATE ENTITIES (MUST KEEP UNTOUCHED) ----
# Keeps all classes inside the entity package intact so JPA mappings don't break
-keep class com.neuro.entity.** { *; }

# ---- DATABASE & CONFIG (MUST KEEP UNTOUCHED) ----
# Keeps your database setup, configuration files, and HibernateUtil safe
-keep class com.neuro.db.** { *; }
-keep class com.neuro.config.** { *; }
-keep class com.neuro.util.HibernateUtil { *; }

# ---- UI LOCALIZATION (MUST KEEP UNTOUCHED) ----
# Keeps your text translation keys from breaking at runtime
-keep class com.neuro.ui.i18n.Messages { *; }

# ============================================================
# ---- DEEP OBFUSCATION RULES (WHAT WILL BE SCRAMBLED) ----
# ============================================================

# We completely REMOVED the "keep" rules for: repo, model, exceptions, app, session.
# These backend packages will now be heavily obfuscated along with your UI!

# FORCE OBFUSCATION ON THE UI PACKAGE
# This allows ProGuard to scramble your Swing/JavaFX views and controller logic
-keep,allowobfuscation class com.neuro.ui.** { *; }

# Keep UI component fields and methods intact if they override standard Java UI methods,
# but allow ProGuard to aggressively scramble their class names and internal logic.
-keepclassmembers class * extends javax.swing.JFrame { *; }
-keepclassmembers class * extends javax.swing.JPanel { *; }
-keepclassmembers class * extends javax.swing.JDialog { *; }
