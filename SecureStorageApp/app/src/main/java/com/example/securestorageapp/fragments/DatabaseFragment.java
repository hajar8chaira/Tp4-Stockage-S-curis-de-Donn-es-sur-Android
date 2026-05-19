package com.example.securestorageapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.securestorageapp.NoteActivity;
import com.example.securestorageapp.R;

public class DatabaseFragment extends Fragment {

    private int userId;
    private String username;

    public static DatabaseFragment newInstance(int userId, String username) {
        DatabaseFragment fragment = new DatabaseFragment();
        Bundle args = new Bundle();
        args.putInt("USER_ID", userId);
        args.putString("USERNAME", username);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userId = getArguments().getInt("USER_ID");
            username = getArguments().getString("USERNAME");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_database, container, false);
        Button btnOpenDatabase = view.findViewById(R.id.btnOpenDatabase);

        btnOpenDatabase.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), NoteActivity.class);
            intent.putExtra("USER_ID", userId);
            intent.putExtra("USERNAME", username);
            startActivity(intent);
        });

        return view;
    }
}
