package com.example.p1t2;

import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class GameRulesActivity extends AppCompatActivity {
    private TextView rulesTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_game_rules);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        rulesTextView = findViewById(R.id.rulesTextView);

        loadGameRules();
    }
    private void loadGameRules() {
        try (InputStream is = getResources().openRawResource(R.raw.game_rules);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {

            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append("\n");
            }

            builder.append("\n\nWatch how to play video");

            SpannableString spannableString = new SpannableString(builder.toString());

            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    Intent intent = new Intent(Intent.ACTION_VIEW,
                            android.net.Uri.parse("https://www.youtube.com/watch?v=3o3EWAIfU10"));
                    startActivity(intent);
                }
            };

            int start = builder.indexOf("Watch how to play video");
            int end = start + "Watch how to play video".length();
            spannableString.setSpan(clickableSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            rulesTextView.setText(spannableString);
            rulesTextView.setMovementMethod(LinkMovementMethod.getInstance());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void goHome(View v) {
        Intent i = new Intent(this, Menu_Activity.class);
        startActivity(i);
    }
}