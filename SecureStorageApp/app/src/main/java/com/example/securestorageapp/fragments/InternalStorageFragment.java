package com.example.securestorageapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.securestorageapp.R;
import com.example.securestorageapp.storage.InternalStorageManager;

public class InternalStorageFragment extends Fragment {

    private EditText etFilename;
    private EditText etContent;
    private TextView tvFileContent;
    private InternalStorageManager storageManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_internal_storage, container, false);

        storageManager = new InternalStorageManager(requireContext());
        etFilename = view.findViewById(R.id.etFilename);
        etContent = view.findViewById(R.id.etContent);
        tvFileContent = view.findViewById(R.id.tvFileContent);

        Button btnSave = view.findViewById(R.id.btnSave);
        Button btnLoad = view.findViewById(R.id.btnLoad);
        Button btnDelete = view.findViewById(R.id.btnDelete);
        Button btnList = view.findViewById(R.id.btnList);

        etFilename.setText("note_interne.txt");

        btnSave.setOnClickListener(v -> saveFile());
        btnLoad.setOnClickListener(v -> loadFile());
        btnDelete.setOnClickListener(v -> deleteFile());
        btnList.setOnClickListener(v -> listFiles());

        return view;
    }

    private void saveFile() {
        String filename = etFilename.getText().toString();
        String content = etContent.getText().toString();
        if (filename.isEmpty() || content.isEmpty()) {
            Toast.makeText(requireContext(), "Champs vides", Toast.LENGTH_SHORT).show();
            return;
        }
        boolean success = storageManager.writeToInternalStorage(filename, content);
        if (success) {
            Toast.makeText(requireContext(), "Enregistre avec succes", Toast.LENGTH_SHORT).show();
            etContent.setText("");
        }
    }

    private void loadFile() {
        String filename = etFilename.getText().toString();
        if (filename.isEmpty()) return;
        String content = storageManager.readFromInternalStorage(filename);
        if (content != null) {
            tvFileContent.setText(content);
        } else {
            tvFileContent.setText("Aucun fichier trouve");
        }
    }

    private void deleteFile() {
        String filename = etFilename.getText().toString();
        if (filename.isEmpty()) return;
        boolean success = storageManager.deleteFromInternalStorage(filename);
        if (success) {
            Toast.makeText(requireContext(), "Fichier supprime", Toast.LENGTH_SHORT).show();
            tvFileContent.setText("");
        }
    }

    private void listFiles() {
        String[] files = storageManager.listInternalStorageFiles();
        StringBuilder sb = new StringBuilder();
        for (String file : files) {
            sb.append("- ").append(file).append("\n");
        }
        tvFileContent.setText(sb.toString().isEmpty() ? "Dossier vide" : sb.toString());
    }
}
