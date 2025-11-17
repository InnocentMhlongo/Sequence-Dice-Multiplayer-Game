package com.example.p1t2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LeaderboardActivity extends AppCompatActivity {
     TextView txtLeaderboard;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_leaderboard);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        txtLeaderboard = findViewById(R.id.txtLeaderboard);

        LeaderboardManager leaderboardManager = new LeaderboardManager(this);
        Map<String, Integer> leaderboard = leaderboardManager.getAllPlayers();

        if (!leaderboard.isEmpty()) {
            List<Map.Entry<String, Integer>> sortedLeaderboard = new ArrayList<>(leaderboard.entrySet());
            Collections.sort(sortedLeaderboard, (entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()));

            List<Map.Entry<String, Integer>> top10Players = sortedLeaderboard.stream()
                    .limit(10)
                    .collect(Collectors.toList());

            StringBuilder leaderboardText = new StringBuilder();
            for (Map.Entry<String, Integer> entry : top10Players) {
                leaderboardText.append(entry.getKey()).append(": ").append(entry.getValue()).append(" wins\n");
            }

            txtLeaderboard.setText(leaderboardText.toString());
        }
    }
    public void goHome(View v) {
        Intent i = new Intent(this, Menu_Activity.class);
        startActivity(i);
    }
}