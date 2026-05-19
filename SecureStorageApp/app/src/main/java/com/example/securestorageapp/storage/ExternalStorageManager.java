package com.example.securestorageapp.storage;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ExternalStorageManager {
    private static final String TAG = "ExternalStorageManager";
    private final Context context;

    public ExternalStorageManager(Context context) {
        this.context = context;
    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

    public boolean writeToExternalStorage(String filename, String content, String type) {
        if (!isExternalStorageWritable()) return false;

        File dir = context.getExternalFilesDir(type);
        if (dir == null) return false;

        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                Log.e(TAG, "Impossible de creer le repertoire : " + dir.getAbsolutePath());
                return false;
            }
        }

        File file = new File(dir, filename);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(content.getBytes(StandardCharsets.UTF_8));
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Erreur ecriture externe", e);
            return false;
        }
    }

    public String readFromExternalStorage(String filename, String type) {
        if (!isExternalStorageReadable()) return null;

        File dir = context.getExternalFilesDir(type);
        if (dir == null) return null;

        File file = new File(dir, filename);
        if (!file.exists()) {
            Log.e(TAG, "Fichier non trouve: " + file.getAbsolutePath());
            return null;
        }

        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] bytes = new byte[(int) file.length()];
            fis.read(bytes);
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            Log.e(TAG, "Erreur lecture externe", e);
            return null;
        }
    }

    public boolean deleteFromExternalStorage(String filename, String type) {
        File dir = context.getExternalFilesDir(type);
        if (dir == null) return false;
        File file = new File(dir, filename);
        if (file.exists()) {
            return file.delete();
        }
        return false;
    }

    public String[] listExternalStorageFiles(String type) {
        File dir = context.getExternalFilesDir(type);
        if (dir == null) return new String[0];

        File[] files = dir.listFiles();
        if (files == null) return new String[0];

        List<String> list = new ArrayList<>();
        for (File f : files) {
            list.add(f.getName());
        }
        return list.toArray(new String[0]);
    }
}
