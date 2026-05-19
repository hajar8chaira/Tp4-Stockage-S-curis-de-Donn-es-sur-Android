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
import com.example.securestorageapp.security.EncryptedFileManager;

public class EncryptedFilesFragment extends Fragment {

    private EditText etFilename;
    private EditText etContent;
    private TextView tvFileContent;
    private EncryptedFileManager encryptedFileManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_encrypted_files, container, false);

        encryptedFileManager = new EncryptedFileManager(requireContext());
        etFilename = view.findViewById(R.id.etFilename);
        etContent = view.findViewById(R.id.etContent);
        tvFileContent = view.findViewById(R.id.tvFileContent);

        Button btnSave = view.findViewById(R.id.btnSave);
        Button btnLoad = view.findViewById(R.id.btnLoad);
        Button btnDelete = view.findViewById(R.id.btnDelete);
        Button btnList = view.findViewById(R.id.btnList);

        etFilename.setText("fichier_secret.enc");

        btnSave.setOnClickListener(v -> encryptFile());
        btnLoad.setOnClickListener(v -> decryptFile());
        btnDelete.setOnClickListener(v -> deleteFile());
        btnList.setOnClickListener(v -> listFiles());

        return view;
    }

    private void encryptFile() {
        String filename = etFilename.getText().toString();
        String content = etContent.getText().toString();
        if (filename.isEmpty() || content.isEmpty()) {
            Toast.makeText(requireContext(), "Champs vides", Toast.LENGTH_SHORT).show();
            return;
        }
        boolean success = encryptedFileManager.writeToEncryptedFile(filename, content);
        if (success) {
            Toast.makeText(requireContext(), "Chiffre avec succes", Toast.LENGTH_SHORT).show();
            etContent.setText("");
        }
    }

    private void decryptFile() {
        String filename = etFilename.getText().toString();
        if (filename.isEmpty()) return;
        String content = encryptedFileManager.readFromEncryptedFile(filename);
        if (content != null) {
            tvFileContent.setText(content);
        } else {
            tvFileContent.setText("Impossible de lire ou fichier absent");
        }
    }

    private void deleteFile() {
        String filename = etFilename.getText().toString();
        if (filename.isEmpty()) return;
        boolean success = encryptedFileManager.deleteEncryptedFile(filename);
        if (success) {
            Toast.makeText(requireContext(), "Fichier chiffre supprime", Toast.LENGTH_SHORT).show();
            tvFileContent.setText("");
        }
    }

    private void listFiles() {
        String[] files = encryptedFileManager.listFiles();
        StringBuilder sb = new StringBuilder();
        for (String file : files) {
            if (file.endsWith(".enc") || file.endsWith(".txt")) {
                sb.append("- ").append(file).append("\n");
            }
        }
        tvFileContent.setText(sb.toString().isEmpty() ? "Aucun fichier" : sb.toString());
    }
}
