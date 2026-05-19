package com.example.securestorageapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.securestorageapp.database.DatabaseManager;
import com.example.securestorageapp.database.User;
import com.example.securestorageapp.security.PasswordHasher;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername;
    private EditText etPassword;
    private Button btnLogin;
    private Button btnRegister;
    private DatabaseManager databaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        databaseManager = new DatabaseManager(this);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);

        btnLogin.setOnClickListener(v -> login());
        btnRegister.setOnClickListener(v -> register());
    }

    private void login() {
        String username = etUsername.getText().toString();
        String password = etPassword.getText().toString();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Champs requis", Toast.LENGTH_SHORT).show();
            return;
        }

        databaseManager.getUserByUsername(username, new DatabaseManager.DatabaseCallback<User>() {
            @Override
            public void onSuccess(User user) {
                runOnUiThread(() -> {
                    if (user != null) {
                        boolean passwordValid = PasswordHasher.verifyPassword(password, user.getPassword());
                        if (passwordValid) {
                            Toast.makeText(LoginActivity.this, "Connexion reussie", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.putExtra("USER_ID", user.getId());
                            intent.putExtra("USERNAME", user.getUsername());
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(LoginActivity.this, "Mot de passe incorrect", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Utilisateur non trouve", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(LoginActivity.this, "Erreur connexion BDD", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void register() {
        String username = etUsername.getText().toString();
        String password = etPassword.getText().toString();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Champs requis", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 8) {
            Toast.makeText(this, "Mot de passe trop court", Toast.LENGTH_SHORT).show();
            return;
        }

        String hashedPassword = PasswordHasher.hashPassword(password);
        User user = new User(username, username + "@example.com", hashedPassword);

        databaseManager.insertUser(user, new DatabaseManager.DatabaseCallback<Long>() {
            @Override
            public void onSuccess(Long userId) {
                runOnUiThread(() -> {
                    Toast.makeText(LoginActivity.this, "Inscription reussie", Toast.LENGTH_SHORT).show();
                    login();
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(LoginActivity.this, "Erreur inscription", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}
