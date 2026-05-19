package com.example.securestorageapp.security;

import android.content.Context;
import android.util.Log;
import androidx.security.crypto.EncryptedFile;
import androidx.security.crypto.MasterKey;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;

public class EncryptedFileManager {
    private static final String TAG = "EncryptedFileManager";
    private final Context context;
    private MasterKey masterKey;

    public EncryptedFileManager(Context context) {
        this.context = context;
        initializeMasterKey();
    }

    private void initializeMasterKey() {
        try {
            masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .setUserAuthenticationRequired(false)
                    .build();
            Log.d(TAG, "MasterKey initialisee");
        } catch (GeneralSecurityException | IOException e) {
            Log.e(TAG, "Erreur MasterKey: " + e.getMessage());
        }
    }

    public boolean writeToEncryptedFile(String filename, String content) {
        if (masterKey == null) {
            return false;
        }

        try {
            File file = new File(context.getFilesDir(), filename);
            if (file.exists()) {
                file.delete();
            }

            EncryptedFile encryptedFile = new EncryptedFile.Builder(
                    context,
                    file,
                    masterKey,
                    EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
            ).build();

            try (OutputStream outputStream = encryptedFile.openFileOutput()) {
                outputStream.write(content.getBytes(StandardCharsets.UTF_8));
            }
            Log.d(TAG, "Fichier chiffre ecrit: " + filename);
            return true;
        } catch (GeneralSecurityException | IOException e) {
            Log.e(TAG, "Erreur ecriture chiffrement: " + e.getMessage());
            return false;
        }
    }

    public String readFromEncryptedFile(String filename) {
        if (masterKey == null) {
            return null;
        }

        try {
            File file = new File(context.getFilesDir(), filename);
            if (!file.exists()) {
                return null;
            }

            EncryptedFile encryptedFile = new EncryptedFile.Builder(
                    context,
                    file,
                    masterKey,
                    EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
            ).build();

            try (InputStream inputStream = encryptedFile.openFileInput();
                 ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {

                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    byteArrayOutputStream.write(buffer, 0, bytesRead);
                }
                return byteArrayOutputStream.toString(StandardCharsets.UTF_8.name());
            }
        } catch (GeneralSecurityException | IOException e) {
            Log.e(TAG, "Erreur lecture chiffrement: " + e.getMessage());
            return null;
        }
    }

    public boolean deleteEncryptedFile(String filename) {
        File file = new File(context.getFilesDir(), filename);
        return file.delete();
    }

    public String[] listFiles() {
        return context.fileList();
    }
}
