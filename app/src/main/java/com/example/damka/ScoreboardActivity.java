package com.example.damka;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ScoreboardActivity extends AppCompatActivity {

    private ListView scoreboardListView;
    private ArrayList<String> scoreboardList;
    private ArrayAdapter<String> adapter;
    private DatabaseReference scoreboardRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scoreboard);

        scoreboardListView = findViewById(R.id.scoreboardListView);
        scoreboardList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, scoreboardList);
        scoreboardListView.setAdapter(adapter);

        scoreboardRef = FirebaseDatabase.getInstance().getReference("scoreboard");

        fetchScores();
    }

    private void fetchScores() {
        scoreboardRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                scoreboardList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String playerName = snapshot.child("playerName").getValue(String.class);
                    long wins = snapshot.child("wins").getValue(Long.class);
                    scoreboardList.add(playerName + " - Wins: " + wins);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ScoreboardActivity.this, "Failed to load scores.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}