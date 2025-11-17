package com.example.p1t2;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Menu_Activity extends AppCompatActivity {
    Button btnPlay,btnLeaderboard,btnGameRules;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_menu);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        btnLeaderboard = findViewById(R.id.btnLeaderboard);
        btnPlay = findViewById(R.id.btnPlay);
        btnGameRules = findViewById(R.id.btnGameRules);

        btnPlay.setOnClickListener(view -> {
            Intent intent = new Intent(Menu_Activity.this, NetworkedSetupActivity.class);
            startActivity(intent);
        });
        btnLeaderboard.setOnClickListener(view -> {
            Intent intent = new Intent(Menu_Activity.this,LeaderboardActivity.class);
            startActivity(intent);
        });
        btnGameRules.setOnClickListener(view -> {
            Intent intent = new Intent(Menu_Activity.this,GameRulesActivity.class);
            startActivity(intent);
        });
    }
}