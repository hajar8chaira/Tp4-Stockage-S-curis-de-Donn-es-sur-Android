package com.example.securestorageapp.storage;

import android.content.Context;
import android.util.Log;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class InternalStorageManager {
    private static final String TAG = "InternalStorageManager";
    private final Context context;

    public InternalStorageManager(Context context) {
        this.context = context;
    }

    public boolean writeToInternalStorage(String filename, String content) {
        try (FileOutputStream fos = context.openFileOutput(filename, Context.MODE_PRIVATE)) {
            fos.write(content.getBytes());
            Log.d(TAG, "Fichier ecrit: " + filename);
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Erreur ecriture: " + e.getMessage());
            return false;
        }
    }

    public String readFromInternalStorage(String filename) {
        try (FileInputStream fis = context.openFileInput(filename);
             InputStreamReader isr = new InputStreamReader(fis);
             BufferedReader br = new BufferedReader(isr)) {

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            Log.d(TAG, "Fichier lu: " + filename);
            return sb.toString();
        } catch (IOException e) {
            Log.e(TAG, "Erreur lecture: " + e.getMessage());
            return null;
        }
    }

    public boolean deleteFromInternalStorage(String filename) {
        boolean result = context.deleteFile(filename);
        if (result) {
            Log.d(TAG, "Fichier supprime: " + filename);
        } else {
            Log.e(TAG, "Erreur suppression: " + filename);
        }
        return result;
    }

    public String[] listInternalStorageFiles() {
        return context.fileList();
    }
}
