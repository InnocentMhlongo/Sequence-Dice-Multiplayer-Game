package com.example.p1t2;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Toast;

/**
 * REPLACES SetupActivity.java - Simplified for networked version
 * Server randomly determines number of players
 */
public class NetworkedSetupActivity extends AppCompatActivity {
    private EditText edtPlayerName;
    private Button btnJoinGame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_networked_setup);

        edtPlayerName = findViewById(R.id.edtPlayerName);
        btnJoinGame = findViewById(R.id.btnJoinGame);

        btnJoinGame.setOnClickListener(v -> joinGame());
    }

    private void joinGame() {
        String playerName = edtPlayerName.getText().toString().trim();

        if (playerName.isEmpty()) {
            edtPlayerName.setError("Enter your name");
            edtPlayerName.requestFocus();
            return;
        }

        // Start the networked main activity
        Intent intent = new Intent(this, NetworkedMainActivity.class);
        intent.putExtra("playerName", playerName);
        startActivity(intent);
        finish();
    }
}