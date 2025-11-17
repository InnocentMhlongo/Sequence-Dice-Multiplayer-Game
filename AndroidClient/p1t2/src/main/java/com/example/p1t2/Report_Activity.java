package com.example.p1t2;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.model.Player;
import com.example.model.SequenceDice;

public class Report_Activity extends AppCompatActivity {
    TextView txtWinner, txtRoundsPlayed, txtTokensPlaced, txtTokensRemoved, txtLongestSequences;
    Button btnMenu;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_report2);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        txtWinner = findViewById(R.id.txtWinner);
        txtRoundsPlayed = findViewById(R.id.txtRounds);
        txtTokensPlaced = findViewById(R.id.txtTokensPlaced);
        txtTokensRemoved = findViewById(R.id.txtTokensRemoved);
        txtLongestSequences = findViewById(R.id.txtLongestSequence);
        btnMenu = findViewById(R.id.btnMenu);

        String winnerText = getIntent().getStringExtra("winnerText");
        int roundsPlayed = getIntent().getIntExtra("roundsPlayed", 0);
        int tokensPlaced = getIntent().getIntExtra("tokensPlaced", 0);
        int tokensRemoved = getIntent().getIntExtra("tokensRemoved", 0);
        String longestSequences = getIntent().getStringExtra("longestSequences");

        txtWinner.setText("WINNER!!!!:"+winnerText);
        txtRoundsPlayed.setText("Rounds Played: " + roundsPlayed);
        txtTokensPlaced.setText("Tokens Placed: " + tokensPlaced);
        txtTokensRemoved.setText("Tokens Removed: " + tokensRemoved);
        txtLongestSequences.setText("Longest Sequences:\n" + longestSequences);

        btnMenu.setOnClickListener(v -> {
            Intent intent = new Intent(Report_Activity.this,Menu_Activity.class);
            startActivity(intent);
        });
    }
}