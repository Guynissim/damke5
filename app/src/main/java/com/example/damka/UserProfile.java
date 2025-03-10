package com.example.damka;

public class UserProfile {
    private String username;
    private int wins;
    private int losses;

    public UserProfile(String username, int wins, int losses) {
        this.username = username;
        this.wins = wins;
        this.losses = losses;
    }

    public String getUsername() {
        return username;
    }

    public int getWins() {
        return wins;
    }

    public int getLosses() {
        return losses;
    }
}
