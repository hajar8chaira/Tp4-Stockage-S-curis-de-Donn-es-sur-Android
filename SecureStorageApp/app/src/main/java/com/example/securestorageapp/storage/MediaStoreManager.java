package com.example.securestorageapp.storage;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class MediaStoreManager {
    private static final String TAG = "MediaStoreManager";
    private final Context context;
    private final ContentResolver contentResolver;

    public MediaStoreManager(Context context) {
        this.context = context;
        this.contentResolver = context.getContentResolver();
    }

    public Uri saveImageToGallery(Bitmap bitmap, String displayName) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, displayName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.Images.Media.IS_PENDING, 1);
            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);
        }

        Uri uri = null;
        try {
            uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            if (uri != null) {
                try (OutputStream os = contentResolver.openOutputStream(uri)) {
                    if (os != null) {
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, os);
                    }
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    values.clear();
                    values.put(MediaStore.Images.Media.IS_PENDING, 0);
                    contentResolver.update(uri, values, null, null);
                }
                Log.d(TAG, "Image enregistree: " + uri);
            }
        } catch (IOException e) {
            Log.e(TAG, "Erreur enregistrement image: " + e.getMessage());
            if (uri != null) {
                contentResolver.delete(uri, null, null);
                uri = null;
            }
        }
        return uri;
    }

    public List<Uri> getAllImages() {
        List<Uri> imageUris = new ArrayList<>();
        String[] projection = {
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME
        };

        try (Cursor cursor = contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                MediaStore.Images.Media.DATE_ADDED + " DESC")) {

            if (cursor != null) {
                int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
                while (cursor.moveToNext()) {
                    long id = cursor.getLong(idColumn);
                    Uri contentUri = Uri.withAppendedPath(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            String.valueOf(id));
                    imageUris.add(contentUri);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur recuperation images: " + e.getMessage());
        }
        return imageUris;
    }

    public boolean deleteImage(Uri uri) {
        try {
            int deletedRows = contentResolver.delete(uri, null, null);
            return deletedRows > 0;
        } catch (Exception e) {
            Log.e(TAG, "Erreur suppression image: " + e.getMessage());
            return false;
        }
    }

    public static Intent createImagePickerIntent() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        return intent;
    }

    public Bitmap loadImageFromUri(Uri uri) {
        try {
            InputStream is = contentResolver.openInputStream(uri);
            if (is != null) {
                Bitmap bitmap = android.graphics.BitmapFactory.decodeStream(is);
                is.close();
                return bitmap;
            }
        } catch (IOException e) {
            Log.e(TAG, "Erreur chargement image: " + e.getMessage());
        }
        return null;
    }
}
