package com.example.securestorageapp.database;

import android.content.Context;
import android.util.Log;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class DatabaseManager {
    private static final String TAG = "DatabaseManager";
    private static final String PASSPHRASE_PREF_NAME = "secure_db_passphrase";
    private static final String PASSPHRASE_KEY = "db_passphrase";
    private static final int PASSPHRASE_LENGTH = 32;

    private final Context context;
    private final Executor executor;
    private AppDatabase database;
    private char[] passphrase;

    public DatabaseManager(Context context) {
        this.context = context;
        this.executor = Executors.newSingleThreadExecutor();
        initializeDatabase();
    }

    private void initializeDatabase() {
        try {
            passphrase = getOrCreatePassphrase();
            database = AppDatabase.getInstance(context, passphrase);
            Log.d(TAG, "Base de donnees initialisee");
        } catch (Exception e) {
            Log.e(TAG, "Erreur initialisation BDD: " + e.getMessage());
        }
    }

    private char[] getOrCreatePassphrase() throws GeneralSecurityException, IOException {
        MasterKey masterKey = new MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build();

        EncryptedSharedPreferences encryptedPrefs = (EncryptedSharedPreferences) EncryptedSharedPreferences.create(
                context,
                PASSPHRASE_PREF_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        );

        String storedPassphrase = encryptedPrefs.getString(PASSPHRASE_KEY, null);
        if (storedPassphrase == null) {
            char[] newPassphrase = AppDatabase.generateRandomPassphrase(PASSPHRASE_LENGTH);
            storedPassphrase = new String(newPassphrase);
            encryptedPrefs.edit().putString(PASSPHRASE_KEY, storedPassphrase).apply();
            return newPassphrase;
        } else {
            return storedPassphrase.toCharArray();
        }
    }

    public void insertUser(User user, DatabaseCallback<Long> callback) {
        executor.execute(() -> {
            try {
                long userId = database.userDao().insert(user);
                callback.onSuccess(userId);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }

    public void getUserByUsername(String username, DatabaseCallback<User> callback) {
        executor.execute(() -> {
            try {
                User user = database.userDao().getUserByUsername(username);
                callback.onSuccess(user);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }

    public void getAllUsers(DatabaseCallback<List<User>> callback) {
        executor.execute(() -> {
            try {
                List<User> users = database.userDao().getAllUsers();
                callback.onSuccess(users);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }

    public void insertNote(Note note, DatabaseCallback<Long> callback) {
        executor.execute(() -> {
            try {
                long noteId = database.noteDao().insert(note);
                callback.onSuccess(noteId);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }

    public void getNotesByUserId(int userId, DatabaseCallback<List<Note>> callback) {
        executor.execute(() -> {
            try {
                List<Note> notes = database.noteDao().getNotesByUserId(userId);
                callback.onSuccess(notes);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }

    public interface DatabaseCallback<T> {
        void onSuccess(T result);
        void onError(Exception e);
    }
}
