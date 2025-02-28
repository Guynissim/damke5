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
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText emailInput, passwordInput;
    private Button loginButton;
    private TextView registerLink;
    private FirebaseAuth mAuth;
    AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
        authManager = new AuthManager();

        emailInput = findViewById(R.id.loginEmailInput);
        passwordInput = findViewById(R.id.loginPasswordInput);
        loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(this);
        registerLink = findViewById(R.id.registerLink);
        registerLink.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == registerLink)// Navigate to RegisterActivity
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        if (v == loginButton)
            loginUser();
    }

    private void loginUser() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            emailInput.setError("Email is required");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordInput.setError("Password is required");
            return;
        }
        authManager.loginUser(email, password, task -> {
            if (task.isSuccessful()) {
                // Sign-in success
                FirebaseUser user = mAuth.getCurrentUser();
                Toast.makeText(LoginActivity.this, "Welcome, " + user.getEmail(), Toast.LENGTH_SHORT).show();
                startActivity(new Intent(LoginActivity.this, MainMenuActivity.class));
                finish();
            } else {
                // If sign-in fails
                Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}