package com.example.securestorageapp.security;

import android.util.Log;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class KeyDerivationManager {
    private static final String TAG = "KeyDerivationManager";

    public static byte[] deriveKeyPbkdf2(char[] password, byte[] salt, int iterations, int keyLength) {
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLength);
            SecretKey key = factory.generateSecret(spec);
            return key.getEncoded();
        } catch (GeneralSecurityException e) {
            Log.e(TAG, "Erreur PBKDF2: " + e.getMessage());
            return null;
        }
    }

    public static byte[] generateSalt(int length) {
        byte[] salt = new byte[length];
        new SecureRandom().nextBytes(salt);
        return salt;
    }

    public static SecretKey createAesKey(byte[] keyBytes) {
        return new SecretKeySpec(keyBytes, "AES");
    }

    public static SecretKey deriveAesKey(char[] password, byte[] salt) {
        byte[] keyBytes = deriveKeyPbkdf2(password, salt, 10000, 256);
        if (keyBytes != null) {
            return createAesKey(keyBytes);
        }
        return null;
    }
}
