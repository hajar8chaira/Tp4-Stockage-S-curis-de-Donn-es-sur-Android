package com.example.securestorageapp.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SupportFactory;
import java.security.SecureRandom;

@Database(entities = {User.class, Note.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "secure_app_db";
    private static AppDatabase instance;

    public abstract UserDao userDao();
    public abstract NoteDao noteDao();

    public static synchronized AppDatabase getInstance(Context context, char[] passphrase) {
        if (instance == null) {
            SQLiteDatabase.loadLibs(context);
            byte[] passphraseBytes = SQLiteDatabase.getBytes(passphrase);
            SupportFactory factory = new SupportFactory(passphraseBytes);
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, DATABASE_NAME)
                    .openHelperFactory(factory)
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }

    public static char[] generateRandomPassphrase(int length) {
        String allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()";
        SecureRandom random = new SecureRandom();
        char[] passphrase = new char[length];
        for (int i = 0; i < length; i++) {
            passphrase[i] = allowedChars.charAt(random.nextInt(allowedChars.length()));
        }
        return passphrase;
    }
}
