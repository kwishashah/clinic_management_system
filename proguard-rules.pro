# ============================================================
# ProGuard Rules for NeuroApplication (dashboard-raw.jar)
# ============================================================

# ---- GENERAL SETTINGS ----

-dontoptimize
-dontshrink

-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses,EnclosingMethod

# ---- ENTRY POINT ----

-keepclasseswithmembers public class com.neuro.application.NeuroApplication {
    public static void main(java.lang.String[]);
}

# ---- THIRD-PARTY LIBRARIES: Keep ALL untouched ----

-keep class !com.neuro.** { *; }

# ---- HIBERNATE ENTITIES ----

-keep class com.neuro.entity.** { *; }

# ---- REPOSITORIES ----

-keep class com.neuro.repo.** { *; }

# ---- MODEL CLASSES ----

-keep class com.neuro.model.** { *; }

# ---- DATABASE & CONFIG ----

-keep class com.neuro.db.** { *; }
-keep class com.neuro.config.** { *; }

# ---- HIBERNATE UTIL ----

-keep class com.neuro.util.HibernateUtil { *; }

# ---- EXCEPTIONS ----

-keep class com.neuro.exceptions.** { *; }

# ---- APP CONTEXT & SESSION ----

-keep class com.neuro.app.** { *; }
-keep class com.neuro.session.** { *; }

# ---- INTERNATIONALIZATION ----

-keep class com.neuro.ui.i18n.Messages { *; }

# ---- SWING SERIALIZATION SAFETY ----

-keepclassmembers class com.neuro.ui.** implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readReplace();
}

# ---- META-INF/services (SPI) ----

-keepdirectories META-INF/services

# ---- RESOURCE FILES ----

-adaptresourcefilenames **.properties,**.xml,**.yml,**.yaml,**.cfg
-adaptresourcefilecontents **.properties,**.xml

# ---- OBFUSCATION SETTINGS ----

-flattenpackagehierarchy 'a'
-repackageclasses 'a'
-allowaccessmodification
-overloadaggressively

# ---- SUPPRESS WARNINGS ----

-dontwarn **
-dontnote **
