package com.example.securestorageapp.security;

import android.content.Context;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Log;
import androidx.security.crypto.MasterKey;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class KeystoreManager {
    private static final String TAG = "KeystoreManager";
    private static final String ANDROID_KEYSTORE = "AndroidKeyStore";
    private static final String STRONGBOX_KEY_ALIAS = "secure_strongbox_key";
    private final Context context;

    public KeystoreManager(Context context) {
        this.context = context;
    }

    public MasterKey createStrongBoxMasterKey() throws GeneralSecurityException, IOException {
        boolean isStrongBoxAvailable = isStrongBoxAvailable();
        MasterKey.Builder builder = new MasterKey.Builder(context, STRONGBOX_KEY_ALIAS)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .setUserAuthenticationRequired(false);

        if (isStrongBoxAvailable) {
            builder.setRequestStrongBoxBacked(true);
            Log.d(TAG, "MasterKey StrongBox creee");
        } else {
            Log.d(TAG, "StrongBox indisponible, MasterKey standard creee");
        }
        return builder.build();
    }

    public boolean isStrongBoxAvailable() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            return false;
        }

        try {
            KeyGenParameterSpec spec = new KeyGenParameterSpec.Builder(
                    "strongbox_test_key",
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .setIsStrongBoxBacked(true)
                    .build();

            KeyGenerator keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES,
                    ANDROID_KEYSTORE);
            keyGenerator.init(spec);
            keyGenerator.generateKey();

            KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
            keyStore.load(null);
            keyStore.deleteEntry("strongbox_test_key");

            return true;
        } catch (Exception e) {
            Log.d(TAG, "StrongBox non supporte");
            return false;
        }
    }

    public boolean generateAesKey(String alias) {
        try {
            KeyGenParameterSpec spec = new KeyGenParameterSpec.Builder(
                    alias,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build();

            KeyGenerator keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES,
                    ANDROID_KEYSTORE);
            keyGenerator.init(spec);
            keyGenerator.generateKey();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Erreur generation cle AES");
            return false;
        }
    }

    public SecretKey getKey(String alias) {
        try {
            KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
            keyStore.load(null);
            KeyStore.SecretKeyEntry entry = (KeyStore.SecretKeyEntry) keyStore.getEntry(alias, null);
            if (entry != null) {
                return entry.getSecretKey();
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public boolean deleteKey(String alias) {
        try {
            KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
            keyStore.load(null);
            keyStore.deleteEntry(alias);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
