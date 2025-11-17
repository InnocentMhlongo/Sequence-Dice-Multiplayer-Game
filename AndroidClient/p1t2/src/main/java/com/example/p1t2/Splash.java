package com.example.p1t2;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Splash extends AppCompatActivity {

    Button btnStartGame;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        btnStartGame = findViewById(R.id.btnStartGame);
        btnStartGame.setOnClickListener(view -> {
            Intent intent = new Intent(Splash.this,Menu_Activity.class);
            startActivity(intent);
        });
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(Splash.this, Menu_Activity.class);
            startActivity(intent);
            finish();
        }, 2000);
    }
}