package com.example.damka;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class GameActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView player1Text, player2Text, gameIdText;
    private Button QuitButton;
    private CustomDialog customDialog;
    private String player1Name, player2Name, gameId, playerId, player1Id, player2Id;
    private int playerSide;// 1 - Player1, 2 - Player2, 0 - Error
    private GameSessionManager gameSessionManager;
    private FireStoreManager firestoreManager;
    private BoardGame boardGame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        // Retrieve game details from Intent
        Intent intent = getIntent();
        gameId = intent.getStringExtra("gameId");
        playerId = intent.getStringExtra("playerId");
        playerSide = intent.getIntExtra("playerSide", 0); // 1 - Player1, 2 - Player2, 0 - Error

        gameSessionManager = new GameSessionManager();
        firestoreManager = new FireStoreManager();

        if (playerSide == 0)
            Log.d("Error GameActivity", "isPlayer1 = 0");

        // UI Elements
        gameIdText = findViewById(R.id.game_id_text);
        gameIdText.setText("Game ID: " + gameId);

        customDialog = new CustomDialog(this);

        player1Text = findViewById(R.id.player1_name);
        player2Text = findViewById(R.id.player2_name);
        QuitButton = findViewById(R.id.QuitButton);
        QuitButton.setOnClickListener(this);


        player1Text.setText("Player 1: Waiting for opponent...");
        player2Text.setText("Player 2: Waiting for opponent...");

        // Fetch player name and update UI (before starting the game)
        firestoreManager.getUserProfile(playerId, task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                if (playerSide == 1) {
                    player1Name = task.getResult().getString("username");
                    Log.d("DEBUG", "Username(Player1): " + player1Name);
                    player1Text.setText("Player 1: " + player1Name);
                }
                if (playerSide == 2) {
                    player2Name = task.getResult().getString("username");
                    Log.d("DEBUG", "Username(Player2): " + player2Name);
                    player2Text.setText("Player 2: " + player2Name);
                }
            } else {
                Log.e("DEBUG", "Failed to get player info.");
            }
        });

        fetchBoardState();
    }

    private void fetchBoardState() {
        DatabaseReference gameRef = FirebaseDatabase.getInstance().getReference("GameSessions").child(gameId);

        gameRef.child("boardState").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<List<Long>> boardStateFromFB = (List<List<Long>>) task.getResult().getValue();
                int[][] boardState = boardStateFromFB != null ? convertListToArray(boardStateFromFB) : null;

                // Create BoardGame after getting boardState
                boardGame = new BoardGame(this, gameSessionManager, gameId, playerId, playerSide, boardState);

                FrameLayout boardContainer = findViewById(R.id.board_container);
                boardContainer.addView(boardGame);

                // Listen for Player 2 and Board Updates
                listenForPlayer2();
                listenForBoardState();
                listenForWinnerSideChange();
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
                player2Id = (String) snapshot.getValue();
                if (player2Id != null) {
                    Log.d("DEBUG", "Player 2 has joined! Player 1's movement is enabled.");
                    if (playerSide == 2) { //Now both players' IDs are in player2's phone
                        getPlayer1Id(playerId -> {
                            if (playerId != null) {
                                player1Id = playerId;
                                Log.d("DEBUG", "player1Id: " + player1Id);
                                setOpponentName(player1Id); // Call this method after player1Id is available
                            } else {
                                Log.e("DEBUG", "Failed to get player1Id from Firebase.");
                            }
                        });
                    }
                    if (playerSide == 1) { // already has both IDs
                        setOpponentName(player2Id);
                    }

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
                    checkAvailableMoves();// Checks if current player cannot move - Loss
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("GameActivity", "Error in listenForBoardState()");
            }
        });
    }

    private void checkAvailableMoves() {
        DatabaseReference gamRef = FirebaseDatabase.getInstance().getReference("GameSessions").child(gameId);
        gamRef.child("turn").get().addOnSuccessListener(dataSnapshot -> {
            if (dataSnapshot.exists()) {
                int side;
                boolean turn = dataSnapshot.getValue(Boolean.class);
                boolean hasMoves = boardGame.hasAvailableMoves(turn);
                if (!hasMoves) {
                    if (turn)
                        side = 2;
                    else
                        side = 1;
                    gameSessionManager.updateWinner(gameId, side); // If Player 1 has no moves, Player 2 wins
                }
            }
        }).addOnFailureListener(e -> {
            Log.e("Firebase", "Failed to get turn value", e);
        });
    }

    public void listenForWinnerSideChange() {
        DatabaseReference gameRef = FirebaseDatabase.getInstance().getReference("GameSessions").child(gameId);
        gameRef.child("winnerside").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Long winnersideLong = snapshot.getValue(Long.class);
                if (winnersideLong == 0)
                    return;
                int winnerside = winnersideLong.intValue();
                // Check if the current player is the winner
                boolean isWin = (winnerside == playerSide);
                // Update user stats accordingly
                firestoreManager.updateUserStats(playerId, isWin, task -> {
                    Log.i("Error", "onDataChange: " + winnerside + " " + winnersideLong);
                    if (task.isSuccessful()) {
                        Log.d("GameActivity", isWin ? "Wins recorded" : "Losses recorded");

                    } else {
                        Log.e("GameActivity", "Failed to update user stats", task.getException());
                    }
                });

                // Remove listener to prevent duplicate updates
                gameRef.child("winnerside").removeEventListener(this);

                startActivity(new Intent(GameActivity.this, MainMenuActivity.class));
                finish();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("GameActivity", "Error in listenForWinnerSideChange()", error.toException());
            }
        });
    }

    private void setOpponentName(String playerId) {
        if (playerId == null) {
            Log.e("getPlayerName()", "Error: playerId is null");
            return;
        }

        firestoreManager.getUserProfile(playerId, task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                String username = task.getResult().getString("username");
                Log.d("getPlayerName()", "Fetched username: " + username);

                if (playerSide == 2) {
                    player1Name = username;
                    Log.d("getPlayerName()", "player1Name set: " + player1Name);
                    player1Text.setText("Player 1: " + player1Name);
                }
                if (playerSide == 1) {
                    player2Name = username;
                    Log.d("getPlayerName()", "player2Name set: " + player2Name);
                    player2Text.setText("Player 2: " + player2Name);
                }
            } else {
                Log.e("getPlayerName()", "Failed to fetch username from Firestore.");
            }
        });
    }


    private void getPlayer1Id(OnSuccessListener<String> callback) {
        DatabaseReference gameRef = FirebaseDatabase.getInstance().getReference("GameSessions").child(gameId).child("player1");
        gameRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String playerId = snapshot.getValue(String.class);
                    Log.d("getPlayer1Id()", "player1Id: " + playerId);
                    callback.onSuccess(playerId); // Pass the retrieved value
                } else {
                    Log.e("getPlayer1Id()", "player1Id not found.");
                    callback.onSuccess(null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("getPlayer1Id()", "Database error: " + error.getMessage());
                callback.onSuccess(null);
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

    @Override
    public void onClick(View v) {
        if (v == QuitButton)
            customDialog.show();
    }

    public void quitGame() {
        DatabaseReference gameRef = FirebaseDatabase.getInstance().getReference("GameSessions").child(gameId);
        if (playerSide == 1) {
            gameRef.child("winnerside").setValue(2); // Make Player 2 the winner
        } else if (playerSide == 2) {
            gameRef.child("winnerside").setValue(1); // Make Player 1 the winner
        }
    }
}
