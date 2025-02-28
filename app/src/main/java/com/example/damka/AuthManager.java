package com.example.damka;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class AuthManager {
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    public AuthManager() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    public void registerUser(String email, String password, String username, OnCompleteListener<AuthResult> listener) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Authentication succeeded
                        String userId = auth.getCurrentUser().getUid();
                        Map<String, Object> user = new HashMap<>();
                        user.put("username", username);
                        user.put("wins", 0);
                        user.put("losses", 0);

                        // Write to Firestore
                        db.collection("Users").document(userId)
                                .set(user)
                                .addOnCompleteListener(writeTask -> {
                                    if (writeTask.isSuccessful()) {
                                        // Notify the original listener of success
                                        listener.onComplete(task);
                                    } else {
                                        // Notify the original listener of Firestore write failure
                                        Exception e = writeTask.getException();
                                        task.getResult().toString(); // For debugging purposes
                                        listener.onComplete(task); // Adjust properly if task structure varies.
                                    }
                                });
                    } else {
                        // Notify the original listener of authentication failure
                        listener.onComplete(task);
                    }
                });
    }

    public void loginUser(String email, String password, OnCompleteListener<AuthResult> listener) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(listener);
    }

    public String getCurrentUserId() {
        if (auth.getCurrentUser() != null) {
            return auth.getCurrentUser().getUid();
        } else {
            return null; // User not authenticated
        }
    }
}
