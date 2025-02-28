package com.example.damka;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainMenuActivity extends AppCompatActivity implements View.OnClickListener {

    private Button newGameButton, loginLogoutButton, scoreboardButton;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        // Initialize buttons
        newGameButton = findViewById(R.id.newGameButton);
        newGameButton.setOnClickListener(this);
        scoreboardButton = findViewById(R.id.scoreboardButton);
        scoreboardButton.setOnClickListener(this);
        loginLogoutButton = findViewById(R.id.loginLogoutButton);
        loginLogoutButton.setOnClickListener(this);

        if (currentUser != null) {
            loginLogoutButton.setText("Logout");
        }
    }

    @Override
    public void onClick(View v) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (v == newGameButton) {
            if (loginLogoutButton.getText().equals("Login")) {
                Toast.makeText(this, "Unable to start a game. Log in first.", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(this, ConnectToGameActivity.class);
                startActivity(intent);
            }
        }
        if (v == scoreboardButton) {
            Intent intent = new Intent(this, ScoreboardActivity.class);
            startActivity(intent);
        }
        if (v == loginLogoutButton) {
            if (loginLogoutButton.getText() != "Logout") {
                // User is not logged in; navigate to LoginActivity
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
            } else {
                // User is logged in; log them out
                mAuth.signOut();
                loginLogoutButton.setText("Login");
                Toast.makeText(MainMenuActivity.this, "Logged out successfully.", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
