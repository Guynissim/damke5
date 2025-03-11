package com.example.damka;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameSessionManager {
    private DatabaseReference gameRef;

    public GameSessionManager() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        gameRef = database.getReference("GameSessions");
    }

    public void createGameSession(String gameId, String playerId) {
        HashMap<String, Object> initialState = new HashMap<>();
        initialState.put("player1", playerId);
        initialState.put("player2", null);
        initialState.put("turn", true); //player1 - true, player2 - false
        initialState.put("boardState", getInitialBoardState());
        initialState.put("winnerside", 0); // player1 - 1,player2 - 2, no winner yet - 0

        gameRef.child(gameId).setValue(initialState).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("DEBUG", "Game session created successfully");
            } else {
                Log.e("DEBUG", "Failed to create game session: " + task.getException());
            }
        });

        DatabaseReference gameSessionRef = gameRef.child(gameId);
        // Remove the game if Player 1 disconnects
        gameSessionRef.child("player1").onDisconnect().removeValue();
    }

    public void joinGameSession(String gameId, String playerId) {
        gameRef.child(gameId).child("player2").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String existingPlayer2 = task.getResult().getValue(String.class);
                if (existingPlayer2 == null) {
                    gameRef.child(gameId).child("player2").setValue(playerId).addOnCompleteListener(joinTask -> {
                        if (joinTask.isSuccessful()) {
                            Log.d("DEBUG", "Player 2 has joined the game!");
                        } else {
                            Log.e("DEBUG", "Failed to join game session: " + joinTask.getException());
                        }
                    });
                } else {
                    Log.e("DEBUG", "Game is already full.");
                }
            } else {
                Log.e("DEBUG", "Failed to check player2 status: " + task.getException());
            }
        });
    }

    public void updateGameState(String gameId, Map<String, Object> gameState) {
        gameRef.child(gameId).updateChildren(gameState).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("DEBUG", "Game state updated successfully");
            } else {
                Log.e("DEBUG", "Failed to update game state: " + task.getException());
            }
        });
    }

    public void checkAvailableMoves(String gameId) {
        gameRef.child("turn").get().addOnSuccessListener(dataSnapshot -> {
            if (dataSnapshot.exists()) {
                int side;
                boolean turn = dataSnapshot.getValue(Boolean.class); //player1 - true, player2 - false
                boolean hasMoves = hasAvailableMoves(turn);
                if (!hasMoves) {
                    if (turn) //player1 has no moves
                        side = 2;
                    else //player2 has no moves
                        side = 1;
                    updateWinner(gameId, side);
                }
            }
        }).addOnFailureListener(e -> {
            Log.e("Firebase", "Failed to get turn value", e);
        });
    }

    public boolean hasAvailableMoves(boolean turn) {
        String player = turn ? "player1" : "player2";

        // Loop through the board and check if player has at least one valid move
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (board[i][j].belongsTo(player)) { // Check if this piece belongs to the player
                    if (canMove(i, j)) { // Check if it has valid moves
                        return true; // At least one move is possible
                    }
                }
            }
        }
        return false; // No moves available
    }

    public void listenToGameSession(String gameId, GameStateListener listener) {
        gameRef.child(gameId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) { // Prevent null errors
                    HashMap<String, Object> gameState = (HashMap<String, Object>) snapshot.getValue();
                    if (gameState != null) {
                        listener.onGameStateChanged(gameState);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onError(error.toException());
            }
        });
    }

    public void updateWinner(String gameId, int winnerSide) {
        if (gameId == null || gameId.isEmpty()) {
            Log.e("GameSessionManager", "Game ID is null or empty. Cannot update winner.");
            return;
        }

        DatabaseReference gameRef = FirebaseDatabase.getInstance().getReference("GameSessions").child(gameId);
        gameRef.child("winnerside").setValue(winnerSide)
                .addOnSuccessListener(aVoid -> Log.d("GameSessionManager", "Winner updated successfully: " + winnerSide))
                .addOnFailureListener(e -> Log.e("GameSessionManager", "Failed to update winner", e));
    }

    private List<List<Integer>> getInitialBoardState() {
        int[][] initialBoard = {
                {0, 1, 0, 1, 0, 1, 0, 1},
                {1, 0, 1, 0, 1, 0, 1, 0},
                {0, 1, 0, 1, 0, 1, 0, 1},
                {0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0},
                {2, 0, 2, 0, 2, 0, 2, 0},
                {0, 2, 0, 2, 0, 2, 0, 2},
                {2, 0, 2, 0, 2, 0, 2, 0}
        };

        List<List<Integer>> boardStateList = new ArrayList<>();
        for (int[] row : initialBoard) {
            List<Integer> rowList = new ArrayList<>();
            for (int cell : row) {
                rowList.add(cell);
            }
            boardStateList.add(rowList);
        }
        return boardStateList;
    }

    // Define the interface for callback
    public interface GameDataListener {
        void onGameDataReceived(Map<String, Object> gameState);
    }


    public interface GameStateListener {
        void onGameStateChanged(Map<String, Object> gameState);

        void onError(@NonNull Exception e);
    }
}
