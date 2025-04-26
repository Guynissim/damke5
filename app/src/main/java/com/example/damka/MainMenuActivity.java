package com.example.damka;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainMenuActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView userNameTextView;
    private Button newGameButton, loginLogoutButton, scoreboardButton;
    private String userId;
    private FirebaseAuth mAuth;
    private AuthManager authManager;
    private FirebaseUser currentUser;
    private FireStoreManager fireStoreManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        fireStoreManager = new FireStoreManager();
        mAuth = FirebaseAuth.getInstance();
        authManager = new AuthManager();
        currentUser = mAuth.getCurrentUser();

        // Initialize buttons and TextView
        userNameTextView = findViewById(R.id.userNameTextView);
        newGameButton = findViewById(R.id.newGameButton);
        newGameButton.setOnClickListener(this);
        scoreboardButton = findViewById(R.id.scoreboardButton);
        scoreboardButton.setOnClickListener(this);
        loginLogoutButton = findViewById(R.id.loginLogoutButton);
        loginLogoutButton.setOnClickListener(this);

        if (currentUser != null) {
            loginLogoutButton.setText("Logout");
            userId = authManager.getCurrentUserId();
            Log.d("userId", "userId: " + userId);
            loadUserDetails();
        }
    }


    @Override
    public void onClick(View v) {
        if (v == newGameButton) {
            if (loginLogoutButton.getText().equals("Login")) {
                Toast.makeText(this, "You must log in first.", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(this, ConnectToGameActivity.class);
                startActivity(intent);
            }
        }
        if (v == scoreboardButton) {
            if (loginLogoutButton.getText().equals("Login")) {
                Toast.makeText(this, "You must log in first.", Toast.LENGTH_SHORT).show();
            } else{
                Intent intent = new Intent(this, ScoreboardActivity.class);
                startActivity(intent);
            }
        }
        if (v == loginLogoutButton) {
            if (loginLogoutButton.getText() != "Logout") {
                // User is not logged in; navigate to LoginActivity
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
            } else {
                mAuth.signOut();
                loginLogoutButton.setText("Login");
                userNameTextView.setText("Name: ");
                Toast.makeText(MainMenuActivity.this, "Logged out successfully.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadUserDetails() {
        fireStoreManager.getUserProfile(userId, task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                String userName = task.getResult().getString("username");
                Log.d("userName", "userName: " + userName);
                userNameTextView.setText("Name:  " + userName);

            } else {
                Log.e("DEBUG", "Failed to get player info.");
            }
        });
    }

}
