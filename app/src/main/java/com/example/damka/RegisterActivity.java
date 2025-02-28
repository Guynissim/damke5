package com.example.damka;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText emailInput, passwordInput, confirmPasswordInput, registerUserNameInput;
    private Button registerButton;
    private TextView loginLink;
    private FirebaseAuth mAuth;
    private AuthManager authManager;
    private FireStoreManager firestoreManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mAuth = FirebaseAuth.getInstance();
        authManager = new AuthManager();
        firestoreManager = new FireStoreManager();

        registerUserNameInput = findViewById(R.id.registerUserNameInput);
        emailInput = findViewById(R.id.registerEmailInput);
        passwordInput = findViewById(R.id.registerPasswordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        registerButton = findViewById(R.id.registerButton);
        registerButton.setOnClickListener(this);
        loginLink = findViewById(R.id.loginLink);
        loginLink.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == loginLink)// Navigate to LoginActivity
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
        if (v == registerButton)
            registerUser();
    }

    private void registerUser() {
        String username = registerUserNameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            registerUserNameInput.setError("Username is required");
            return;
        }

        if (TextUtils.isEmpty(email)) {
            emailInput.setError("Email is required");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordInput.setError("Password is required");
            return;
        }
        if (password.length() < 6) {
            passwordInput.setError("Password should be at least 6 characters ");
            return;
        }

        if (!password.equals(confirmPassword)) {
            confirmPasswordInput.setError("Passwords do not match");
            return;
        }
        authManager.registerUser(email, password, username, task -> {
            if (task.isSuccessful()) {
                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                firestoreManager.saveUserProfile(userId, username, email, 0, 0, saveTask -> {
                    if (saveTask.isSuccessful()) {
                        Toast.makeText(RegisterActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(RegisterActivity.this, MainMenuActivity.class));
                        finish();
                    } else {
                        Toast.makeText(RegisterActivity.this, "Failed to save user profile.", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                String errorMessage = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                Toast.makeText(RegisterActivity.this, "Registration failed: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }
}