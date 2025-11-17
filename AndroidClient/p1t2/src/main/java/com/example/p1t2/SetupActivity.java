package com.example.p1t2;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.model.Player;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class SetupActivity extends AppCompatActivity {

    private Spinner spinnerNumPlayers;
    private LinearLayout playersContainer;
    private Button btnDecideStarter;
    private final String[] colorOptions = {"Red","Blue","Green","Yellow"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_setup);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rootLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        spinnerNumPlayers = findViewById(R.id.spinnerNumPlayers);
        playersContainer = findViewById(R.id.playersContainer);
        btnDecideStarter = findViewById(R.id.btnDecideStarter);

        ArrayAdapter<Integer> numAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                Arrays.asList(2,3,4));
        numAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerNumPlayers.setAdapter(numAdapter);

        buildPlayerInputs(2);

        spinnerNumPlayers.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int n = (Integer) spinnerNumPlayers.getSelectedItem();
                if (n == 4) {
                    buildTeamInputs();
                } else {
                    buildPlayerInputs(n);
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnDecideStarter.setOnClickListener(v -> onDecideStarter());
    }

    private void buildPlayerInputs(int numPlayers) {
        playersContainer.removeAllViews();

        for (int i = 1; i <= numPlayers; i++) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(0, 12, 0, 12);

            EditText edtName = new EditText(this);
            edtName.setHint("Player " + i + " name");
            edtName.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

            Spinner spColor = new Spinner(this);

            ArrayAdapter<String> colorAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item,
                    colorOptions);
            colorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spColor.setAdapter(colorAdapter);
            spColor.setLayoutParams(new LinearLayout.LayoutParams(300, LinearLayout.LayoutParams.WRAP_CONTENT));

            int defaultIndex = (i - 1) % colorOptions.length;
            spColor.setSelection(defaultIndex);

            row.addView(edtName);
            row.addView(spColor);

            row.setTag(new PlayerInputHolder(edtName, spColor));
            playersContainer.addView(row);
        }
    }


    private static class PlayerInputHolder {
        EditText name;
        Spinner color;
        PlayerInputHolder(EditText n, Spinner c) { name = n; color = c; }
    }

    private void onDecideStarter() {
        int childCount = playersContainer.getChildCount();
        List<Player> players = new ArrayList<>();

        for (int i = 0; i < childCount; i++) {
            View row = playersContainer.getChildAt(i);
            PlayerInputHolder holder = (PlayerInputHolder) row.getTag();
            String name = holder.name.getText().toString().trim();

            if (name.isEmpty()) {
                holder.name.setError("Enter name");
                holder.name.requestFocus();
                return;
            }

            String colour;
            if (holder.color.getVisibility() == View.VISIBLE) {
                colour = holder.color.getSelectedItem().toString();
            } else {
                int captainIndex = (i == 2) ? 0 : 1;
                View captainRow = playersContainer.getChildAt(captainIndex);
                PlayerInputHolder captainHolder = (PlayerInputHolder) captainRow.getTag();
                colour = captainHolder.color.getSelectedItem().toString();
            }

            players.add(new Player(i + 1, name, colour));
        }

        int starterIndex = decideStartingPlayerRollOff(players);

        new AlertDialog.Builder(this)
                .setTitle("Starting Player")
                .setMessage(players.get(starterIndex).getName() + " will start the game.")
                .setPositiveButton("Start Game", (d, w) -> {
                    Intent intent = new Intent(SetupActivity.this, MainActivity.class);
                    intent.putExtra("players", (Serializable) players);
                    intent.putExtra("startIndex", starterIndex);
                    startActivity(intent);
                    finish();
                })
                .setCancelable(false)
                .show();
    }


    private void buildTeamInputs() {
        playersContainer.removeAllViews();

        String[] defaultColors = {"Red", "Blue"};

        for (int i = 0; i < 4; i++) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(0, 12, 0, 12);

            EditText edtName = new EditText(this);
            if(i==0 || i==2)
            {
                edtName.setHint("Player " + (i + 1) + " name" + "(Team 1)");
            }
            else {
                edtName.setHint("Player " + (i + 1) + " name" + "(Team 2)");
            }

            edtName.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

            Spinner spColor = new Spinner(this);

            if (i == 0 || i == 1) {
                ArrayAdapter<String> colorAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, colorOptions);
                colorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spColor.setAdapter(colorAdapter);
                spColor.setLayoutParams(new LinearLayout.LayoutParams(300, LinearLayout.LayoutParams.WRAP_CONTENT));
                spColor.setSelection(Arrays.asList(colorOptions).indexOf(defaultColors[i])); // default color
                row.addView(edtName);
                row.addView(spColor);
            } else {
                spColor.setVisibility(View.GONE);
                row.addView(edtName);
            }

            row.setTag(new PlayerInputHolder(edtName, spColor));
            playersContainer.addView(row);
        }
    }

    private int decideStartingPlayerRollOff(List<Player> players) {
        Random rand = new Random();
        List<Integer> candidates = new ArrayList<>();
        for (int i = 0; i < players.size(); i++) candidates.add(i);

        while (candidates.size() > 1) {
            int highest = -1;
            List<Integer> newCandidates = new ArrayList<>();
            StringBuilder sb = new StringBuilder();
            sb.append("Roll-off round:\n");

            Map<Integer, Integer> rollMap = new HashMap<>();
            for (int idx : candidates) {
                int roll = rand.nextInt(6) + 1 + rand.nextInt(6) + 1;
                rollMap.put(idx, roll);
                sb.append(players.get(idx).getName()).append(" rolled ").append(roll).append("\n");
                if (roll > highest) {
                    highest = roll;
                }
            }

            for (int idx : candidates) {
                if (rollMap.get(idx) == highest) newCandidates.add(idx);
            }

            Toast.makeText(this, sb.toString(), Toast.LENGTH_LONG).show();

            if (newCandidates.size() == 1) {
                return newCandidates.get(0);
            } else {
                // tie - re-roll among tied players
                candidates = newCandidates;
                try { Thread.sleep(600); } catch (InterruptedException ignored) {}
            }
        }

        return candidates.get(0);
    }
}