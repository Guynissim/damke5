package com.example.damka;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class GameActivity extends AppCompatActivity {
    private TextView player1Text, player2Text, gameIdText;
    private String playerName1, playerName2;
    private GameSessionManager gameSessionManager;
    private BoardGame boardGame;
    private String gameId, playerId;
    private int isPlayer1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        // Retrieve game details from Intent
        Intent intent = getIntent();
        gameId = intent.getStringExtra("gameId");
        playerId = intent.getStringExtra("playerId");
        isPlayer1 = intent.getIntExtra("isPlayer1", 0); // 1 - Player1, 2 - Player2, 0 - Error

        gameSessionManager = new GameSessionManager();
        FireStoreManager firestoreManager = new FireStoreManager();

        if (isPlayer1 == 0)
            Log.d("Error GameActivity", "isPlayer1 = 0");

        // UI Elements
        gameIdText = findViewById(R.id.game_id_text);
        gameIdText.setText("Game ID: " + gameId);

        player1Text = findViewById(R.id.player1_name);
        player2Text = findViewById(R.id.player2_name);

        player2Text.setText("Player 2: Waiting for opponent...");

        // Fetch player name and update UI inside callback
        firestoreManager.getUserProfile(playerId, task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                if (isPlayer1 == 1) {
                    playerName1 = task.getResult().getString("username");
                    Log.d("DEBUG", "Username(Player1): " + playerName1);
                    player1Text.setText("Player 1: " + playerName1);
                } else {
                    playerName2 = task.getResult().getString("username");
                    Log.d("DEBUG", "Username(Player2): " + playerName2);
                    player2Text.setText("Player 2: " + playerName2);
                }
            } else {
                Log.e("DEBUG", "Failed to get player info.");
            }
        });
        // Fetch Board State First, Then Create BoardGame
        fetchBoardState();
    }

    private void fetchBoardState() {
        DatabaseReference gameRef = FirebaseDatabase.getInstance().getReference("GameSessions").child(gameId);

        gameRef.child("boardState").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<List<Long>> boardStateFromFB = (List<List<Long>>) task.getResult().getValue();
                int[][] boardState = boardStateFromFB != null ? convertListToArray(boardStateFromFB) : null;

                // Create BoardGame after getting boardState
                boardGame = new BoardGame(this, gameSessionManager, gameId, playerId,isPlayer1, boardState);

                FrameLayout boardContainer = findViewById(R.id.board_container);
                boardContainer.addView(boardGame);

                // Listen for Player 2 and Board Updates
                listenForPlayer2();
                listenForBoardState();
            } else {
                Log.e("GameActivity", "Failed to fetch board state: " + task.getException());
            }
        });
    }

    private void listenForPlayer2() {
        DatabaseReference gameRef = FirebaseDatabase.getInstance().getReference("GameSessions").child(gameId).child("player2");

        gameRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String player2Id = snapshot.getValue(String.class);
                if (player2Id != null) {
                    Log.d("DEBUG", "Player 2 has joined! Player 1's movement is enabled.");
                    // enable player1's movement
                } else {
                    Log.d("DEBUG", "Waiting for Player 2...");
                    // disable player1's movement
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("DEBUG", "Error listening for Player 2: " + error.getMessage());
            }
        });
    }

    private void listenForBoardState() {
        DatabaseReference gameRef = FirebaseDatabase.getInstance().getReference("GameSessions").child(gameId);
        gameRef.child("boardState").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<List<Long>> boardStateFromFB = (List<List<Long>>) snapshot.getValue();
                if (boardStateFromFB != null) {
                    int[][] boardState = convertListToArray(boardStateFromFB);
                    boardGame.updateBoardState(boardState);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("GameActivity", "Error in listenForBoardState()");
            }
        });
    }

    private int[][] convertListToArray(List<List<Long>> boardStateList) {
        if (boardStateList == null || boardStateList.isEmpty()) return null;
        int[][] board = new int[boardStateList.size()][boardStateList.get(0).size()];
        for (int i = 0; i < boardStateList.size(); i++) {
            for (int j = 0; j < boardStateList.get(i).size(); j++) {
                board[i][j] = boardStateList.get(i).get(j).intValue();
            }
        }
        return board;
    }
}
