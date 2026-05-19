package com.example.securestorageapp.security;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public class SecurityAnalyzer {
    private static final String TAG = "SecurityAnalyzer";
    private final Context context;

    public SecurityAnalyzer(Context context) {
        this.context = context;
    }

    public static final java.util.Set<String> mitigatedRisks = new java.util.HashSet<>();

    public List<SecurityRisk> analyzeSecurityRisks() {
        List<SecurityRisk> risks = new ArrayList<>();
        if (!mitigatedRisks.contains("Sauvegardes activees")) {
            checkBackupSettings(risks);
        }
        if (!mitigatedRisks.contains("StrongBox indisponible")) {
            checkStrongBoxAvailability(risks);
        }
        if (!mitigatedRisks.contains("Mode Debug actif")) {
            checkDebuggable(risks);
        }
        if (!mitigatedRisks.contains("Execution sur Emulateur")) {
            checkEmulator(risks);
        }
        return risks;
    }

    private void checkBackupSettings(List<SecurityRisk> risks) {
        try {
            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), 0);
            boolean allowBackup = (appInfo.flags & ApplicationInfo.FLAG_ALLOW_BACKUP) != 0;
            if (allowBackup) {
                risks.add(new SecurityRisk(
                        "Sauvegardes activees",
                        "Les sauvegardes automatiques de donnees sont en service, ce qui est critique.",
                        "Definissez android:allowBackup=\"false\" dans le manifeste.",
                        "<application\n    android:allowBackup=\"false\"\n    android:fullBackupContent=\"false\"\n    ... >\n</application>",
                        SecurityRisk.Severity.HIGH
                ));
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Erreur verification backups: " + e.getMessage());
        }
    }

    private void checkStrongBoxAvailability(List<SecurityRisk> risks) {
        KeystoreManager keystoreManager = new KeystoreManager(context);
        boolean strongBoxAvailable = keystoreManager.isStrongBoxAvailable();
        if (!strongBoxAvailable) {
            risks.add(new SecurityRisk(
                    "StrongBox indisponible",
                    "Le module materiel StrongBox n'est pas installe sur cette plateforme.",
                    "La protection repose sur le TEE ou la derivation logicielle PBKDF2.",
                    "KeyGenParameterSpec spec = new KeyGenParameterSpec.Builder(\n        \"master_key\",\n        KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)\n        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)\n        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)\n        .setKeySize(256)\n        .setUserAuthenticationRequired(false)\n        .build();",
                    SecurityRisk.Severity.MEDIUM
            ));
        }
    }

    private void checkDebuggable(List<SecurityRisk> risks) {
        try {
            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), 0);
            boolean debuggable = (appInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
            if (debuggable) {
                risks.add(new SecurityRisk(
                        "Mode Debug actif",
                        "L'application est compilee avec les drapeaux de debogage systeme.",
                        "Desactivez le mode debug dans vos reglages de build gradle.",
                        "buildTypes {\n    release {\n        isMinifyEnabled = true\n        isShrinkResources = true\n        isDebuggable = false\n        proguardFiles(getDefaultProguardFile(\"proguard-android-optimize.txt\"), \"proguard-rules.pro\")\n    }\n}",
                        SecurityRisk.Severity.HIGH
                ));
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Erreur debug check: " + e.getMessage());
        }
    }

    private void checkEmulator(List<SecurityRisk> risks) {
        boolean isEmulator = Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk".equals(Build.PRODUCT);

        if (isEmulator) {
            risks.add(new SecurityRisk(
                    "Execution sur Emulateur",
                    "L'application s'execute au sein d'un environnement de test virtuel.",
                    "Privilegiez un terminal physique pour reduire les points d'attaque.",
                    "boolean isEmulator = Build.FINGERPRINT.startsWith(\"generic\")\n        || Build.MODEL.contains(\"Emulator\");\nif (isEmulator) {\n    throw new SecurityException(\"Execution interdite sur emulateur !\");\n}",
                    SecurityRisk.Severity.LOW
            ));
        }
    }

    public static class SecurityRisk {
        private final String title;
        private final String description;
        private final String recommendation;
        private final String codeSnippet;
        private final Severity severity;

        public SecurityRisk(String title, String description, String recommendation, String codeSnippet, Severity severity) {
            this.title = title;
            this.description = description;
            this.recommendation = recommendation;
            this.codeSnippet = codeSnippet;
            this.severity = severity;
        }

        public String getTitle() {
            return title;
        }

        public String getDescription() {
            return description;
        }

        public String getRecommendation() {
            return recommendation;
        }

        public String getCodeSnippet() {
            return codeSnippet;
        }

        public Severity getSeverity() {
            return severity;
        }

        public enum Severity {
            LOW, MEDIUM, HIGH
        }
    }
}
