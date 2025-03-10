package com.example.damka;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;

public class ScoreboardActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ScoreboardAdapter adapter;
    private List<UserProfile> userList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scoreboard);

        recyclerView = findViewById(R.id.scoreboardRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        userList = new ArrayList<>();
        adapter = new ScoreboardAdapter(userList);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        loadScoreboard();
    }

    private void loadScoreboard() {
        db.collection("Users")
                .orderBy("wins", Query.Direction.DESCENDING) // Sort by wins
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            userList.clear();
                            for (DocumentSnapshot document : task.getResult()) {
                                String username = document.getString("username");
                                long wins = document.getLong("wins") != null ? document.getLong("wins") : 0;
                                long losses = document.getLong("losses") != null ? document.getLong("losses") : 0;
                                userList.add(new UserProfile(username, (int) wins, (int) losses));
                            }
                            adapter.notifyDataSetChanged();
                        } else {
                            Log.e("Scoreboard", "Error getting documents: ", task.getException());
                            Toast.makeText(ScoreboardActivity.this, "Failed to load scoreboard.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
