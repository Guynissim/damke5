package com.example.damka;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class FireStoreManager {
    private final FirebaseFirestore db;

    public FireStoreManager() {
        db = FirebaseFirestore.getInstance();
    }

    // Save user profile (username, email, wins, losses)
    public void saveUserProfile(String userId, String username, String email, int wins, int losses, OnCompleteListener<Void> listener) {
        Map<String, Object> userProfile = new HashMap<>();
        userProfile.put("username", username);
        userProfile.put("email", email);
        userProfile.put("wins", wins);
        userProfile.put("losses", losses);

        db.collection("Users").document(userId)
                .set(userProfile, SetOptions.merge())
                .addOnCompleteListener(listener);
    }

    // Get full user profile
    public void getUserProfile(String userId, OnCompleteListener<DocumentSnapshot> listener) {
        db.collection("Users").document(userId)
                .get()
                .addOnCompleteListener(listener);
    }

    // Get a waiting game for Player 2
    public void getWaitingGame(OnCompleteListener<QuerySnapshot> listener) {
        db.collection("WaitingGames").limit(1)
                .get()
                .addOnCompleteListener(listener);
    }

    // Add a game to the waiting list (Player 1 creates a game)
    public void addGameToWaitingList(String gameId, Map<String, Object> gameData, OnCompleteListener<Void> listener) {
        db.collection("WaitingGames").document(gameId)
                .set(gameData)
                .addOnCompleteListener(listener);
    }

    // Remove a game from the waiting list (Player 2 joins a game)
    public void removeGameFromWaitingList(String gameId, OnCompleteListener<Void> listener) {
        db.collection("WaitingGames").document(gameId)
                .delete()
                .addOnCompleteListener(listener);
    }

    // Update game details (game state, moves, etc.)
    public void updateGameMetadata(String gameId, Map<String, Object> gameData, OnCompleteListener<Void> listener) {
        db.collection("Games").document(gameId)
                .set(gameData, SetOptions.merge())
                .addOnCompleteListener(listener);
    }

    // Check if a specific game exists
    public void doesGameExist(String gameId, OnCompleteListener<DocumentSnapshot> listener) {
        db.collection("Games").document(gameId)
                .get()
                .addOnCompleteListener(listener);
    }
}

