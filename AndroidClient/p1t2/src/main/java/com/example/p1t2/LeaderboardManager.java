package com.example.p1t2;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;
import java.util.Map;

public class LeaderboardManager {
    private static final String PREFS_NAME = "leaderboard_prefs";
    private SharedPreferences prefs;

    public LeaderboardManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void incrementWin(String playerName) {
        int currentWins = prefs.getInt(playerName, 0);
        prefs.edit().putInt(playerName, currentWins + 1).apply();
    }

    public int getWins(String playerName) {
        return prefs.getInt(playerName, 0);
    }

    public Map<String, Integer> getAllPlayers() {
        Map<String, ?> allEntries = prefs.getAll();
        Map<String, Integer> leaderboard = new HashMap<>();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            leaderboard.put(entry.getKey(), (Integer) entry.getValue());
        }
        return leaderboard;
    }

    public void addPlayerIfNew(String playerName) {
        if (!prefs.contains(playerName)) {
            prefs.edit().putInt(playerName, 0).apply(); // Initialize with 0 wins
        }
    }
}
