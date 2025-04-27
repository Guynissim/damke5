package com.example.damka;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ConnectToGameActivity extends AppCompatActivity implements View.OnClickListener {

    private Button createGameButton, joinGameButton;
    private GameSessionManager gameSessionManager;
    private AuthManager authManager;
    private FireStoreManager firestoreManager;
    private ActivityResultLauncher<Intent> activityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_to_game);

        createGameButton = findViewById(R.id.createGameButton);
        createGameButton.setOnClickListener(this);
        joinGameButton = findViewById(R.id.joinGameButton);
        joinGameButton.setOnClickListener(this);

        gameSessionManager = new GameSessionManager();
        authManager = new AuthManager();
        firestoreManager = new FireStoreManager();


        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK) {
                            Intent data = result.getData();
                            boolean isWin = data.getBooleanExtra("winnerSide", false);
                            Log.d("activityResultLauncher","winnerSide:" + isWin);
                            displayLastResult(isWin);
                        }
                    }
                });
    }


    private void displayLastResult(boolean isWin) {
        String message = isWin ? "You Won!!!" : " You Lost...";
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onClick(View v) {
        if (v == createGameButton)
            createGame();
        if (v == joinGameButton)
            joinGame();
    }

    private void createGame() {
        String currentPlayerId = authManager.getCurrentUserId();
        String gameId = UUID.randomUUID().toString(); // Unique game ID
        Map<String, Object> gameData = new HashMap<>();
        gameData.put("gameId", gameId);
        gameData.put("createdAt", System.currentTimeMillis());

        firestoreManager.addGameToWaitingList(gameId, gameData, task -> {
            if (task.isSuccessful()) {
                int playerSide = 1;
                Log.d("DEBUG", "Successfully added game: " + gameId);
                gameSessionManager.createGameSession(gameId, currentPlayerId);
                startGameActivity(gameId, currentPlayerId, playerSide);
            } else {
                Log.e("DEBUG", "Failed to create game: " + task.getException().getMessage());
            }
        });
    }

    private void joinGame() {
        String currentPlayerId = authManager.getCurrentUserId();
        int playerSide = 2;
        firestoreManager.getWaitingGame(task -> {
            if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {

                String gameId = task.getResult().getDocuments().get(0).getId();
                Log.d("DEBUG", "Joining a game: " + gameId);

                gameSessionManager.joinGameSession(gameId, currentPlayerId);

                firestoreManager.removeGameFromWaitingList(gameId, task2 -> {
                    if (task2.isSuccessful()) {
                        Log.d("DEBUG", "Successfully removed waiting game: " + gameId);
                    }
                });
                startGameActivity(gameId, currentPlayerId, playerSide);
            } else {
                Log.d("DEBUG", "No games available.");
                Toast.makeText(this, "No games available. Try creating a game.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void startGameActivity(String gameId, String playerId, int playerSide) {
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra("gameId", gameId);
        intent.putExtra("playerId", playerId);
        intent.putExtra("playerSide", playerSide);
        activityResultLauncher.launch(intent);
    }
}