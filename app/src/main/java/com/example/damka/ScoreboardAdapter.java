package com.example.damka;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ScoreboardAdapter extends RecyclerView.Adapter<ScoreboardAdapter.ViewHolder> {
    private List<UserProfile> userList;

    public ScoreboardAdapter(List<UserProfile> userList) {
        this.userList = userList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_scoreboard, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserProfile user = userList.get(position);
        holder.usernameText.setText(user.getUsername());
        holder.winsText.setText("Wins: " + user.getWins());
        holder.lossesText.setText("Losses: " + user.getLosses());
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView usernameText, winsText, lossesText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameText = itemView.findViewById(R.id.usernameText);
            winsText = itemView.findViewById(R.id.winsText);
            lossesText = itemView.findViewById(R.id.lossesText);
        }
    }
}
