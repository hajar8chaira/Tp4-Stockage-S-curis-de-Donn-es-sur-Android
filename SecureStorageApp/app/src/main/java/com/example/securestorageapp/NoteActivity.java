package com.example.securestorageapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.securestorageapp.adapters.NoteAdapter;
import com.example.securestorageapp.database.DatabaseManager;
import com.example.securestorageapp.database.Note;
import com.example.securestorageapp.security.EncryptedFileManager;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NoteActivity extends AppCompatActivity {

    private EditText etNoteTitle;
    private EditText etNoteContent;
    private Button btnSaveNote;
    private RecyclerView rvNotes;
    private TextView tvUsername;
    private DatabaseManager databaseManager;
    private EncryptedFileManager encryptedFileManager;
    private NoteAdapter noteAdapter;
    private List<Note> notesList;
    private int userId;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        userId = getIntent().getIntExtra("USER_ID", -1);
        username = getIntent().getStringExtra("USERNAME");

        if (userId == -1 || username == null) {
            Toast.makeText(this, "Utilisateur non identifie", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        databaseManager = new DatabaseManager(this);
        encryptedFileManager = new EncryptedFileManager(this);

        etNoteTitle = findViewById(R.id.etNoteTitle);
        etNoteContent = findViewById(R.id.etNoteContent);
        btnSaveNote = findViewById(R.id.btnSaveNote);
        rvNotes = findViewById(R.id.rvNotes);
        tvUsername = findViewById(R.id.tvUsername);

        notesList = new ArrayList<>();
        noteAdapter = new NoteAdapter(notesList);
        rvNotes.setLayoutManager(new LinearLayoutManager(this));
        rvNotes.setAdapter(noteAdapter);

        tvUsername.setText("Notes de : " + username);
        btnSaveNote.setOnClickListener(v -> saveNote());

        loadNotes();
    }

    private void saveNote() {
        String title = etNoteTitle.getText().toString();
        String content = etNoteContent.getText().toString();

        if (title.isEmpty() || content.isEmpty()) {
            Toast.makeText(this, "Champs vides", Toast.LENGTH_SHORT).show();
            return;
        }

        Note note = new Note(userId, title, content);
        databaseManager.insertNote(note, new DatabaseManager.DatabaseCallback<Long>() {
            @Override
            public void onSuccess(Long noteId) {
                runOnUiThread(() -> {
                    logAction("Note creee: " + title);
                    note.setId(noteId.intValue());
                    notesList.add(0, note);
                    noteAdapter.notifyItemInserted(0);
                    etNoteTitle.setText("");
                    etNoteContent.setText("");
                    Toast.makeText(NoteActivity.this, "Note enregistree avec succes", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(NoteActivity.this, "Erreur enregistrement note", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void loadNotes() {
        databaseManager.getNotesByUserId(userId, new DatabaseManager.DatabaseCallback<List<Note>>() {
            @Override
            public void onSuccess(List<Note> notes) {
                runOnUiThread(() -> {
                    notesList.clear();
                    notesList.addAll(notes);
                    noteAdapter.notifyDataSetChanged();
                    logAction("Notes chargees: " + notes.size() + " notes");
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(NoteActivity.this, "Erreur chargement notes", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void logAction(String action) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String timestamp = sdf.format(new Date());
        String logMessage = timestamp + " - " + username + " - " + action + "\n";
        String logFilename = "user_" + userId + "_log.txt";

        String existingLog = encryptedFileManager.readFromEncryptedFile(logFilename);
        if (existingLog == null) {
            existingLog = "";
        }

        String newLog = logMessage + existingLog;
        encryptedFileManager.writeToEncryptedFile(logFilename, newLog);
    }
}
