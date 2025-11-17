package com.example.p1t2;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.model.*;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements Observer {
    private SequenceDice game;
    private TextView txtCurrentPlayer;
    private GridLayout boardGrid;
    private Button btnRollDice, btnShowBoard;

    private int diceTotal;
    private Random rand = new Random();
    ImageView die1,die2;
    ArrayList<Player> players;

    @Override
    @SuppressWarnings("unchecked")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
//
        txtCurrentPlayer = findViewById(R.id.txtCurrentPlayer);
        boardGrid = findViewById(R.id.boardGrid);
        btnRollDice = findViewById(R.id.btnRollDice);
        btnShowBoard = findViewById(R.id.btnShowBoard);
        die1 = findViewById(R.id.imgDie1);
        die2 = findViewById(R.id.imgDie2);

        players = (ArrayList<Player>) getIntent().getSerializableExtra("players");
        int startIndex = getIntent().getIntExtra("startIndex", 0);
        if (players == null) {
            players = new ArrayList<>();
            players.add(new Player(1, "P1", "Red"));
            players.add(new Player(2, "P2", "Blue"));
        }

        game = new SequenceDice(players);
        game.addObserver(this);
        game.currentPlayerIndex = startIndex;
        game.startGame();

//        Die die = new Die(die1, die2);
//        die.setOnDiceRollCompleteListener(total -> {
//            diceTotal = total;
//            Toast.makeText(MainActivity.this, "Rolled " + diceTotal, Toast.LENGTH_SHORT).show();
//        });
//        btnRollDice.setOnClickListener(v -> {
//                if (diceTotal != 0) {
//                    Toast.makeText(this, "You already rolled a " + diceTotal + ". Place your token first!", Toast.LENGTH_SHORT).show();
//                    return;
//                    }
//
//            die.rollDice();
//        });
        btnRollDice.setOnClickListener(v -> {
            if (diceTotal != 0) {
                Toast.makeText(MainActivity.this, "You already rolled a " + diceTotal + ". Place your token first!", Toast.LENGTH_SHORT).show();
                return;
            }

            Die die = new Die(die1, die2);
            die.setOnDiceRollCompleteListener(total -> {
                diceTotal = total;
                Toast.makeText(MainActivity.this, "Rolled " + diceTotal, Toast.LENGTH_SHORT).show();

                final Player current = game.players.get(game.currentPlayerIndex);

                boolean isForcedSkip = false;
                String skipMessage = "";

                // Check if player has to skip immediately after rolling a 10
                if (diceTotal == 10) {
                    if (game.board.isBoardEmptyOr(current, game.isFourPlayerGame()) || game.board.allInSafePositions()) {
                        isForcedSkip = true;
                        skipMessage = "You had no opponent tokens to remove. Your turn is skipped.";
                    }
                }
                // Check if player has to skip for a normal roll
                else if (diceTotal != 2 && diceTotal != 12 && diceTotal != 11) {
                    if (game.board.allTakenByPlayer(diceTotal, current.getId(), game.isFourPlayerGame())) {
                        isForcedSkip = true;
                        skipMessage = "All spots for a roll of " + diceTotal + " are already yours. Your turn is skipped.";
                    }
                }

                if (isForcedSkip) {
                    game.setPlayer();
                    Player next = game.players.get(game.currentPlayerIndex);
                    showSkipDialog(next, skipMessage);
                    diceTotal = 0; // Reset for next player
                }
            });

            die.rollDice();
        });

        setupBoardViews();
        updateBoardUI();
        btnShowBoard.setOnClickListener(v -> updateBoardUI());
    }

    private void setupBoardViews() {
        boardGrid.removeAllViews();
        int size = 6;
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                final int row = r;
                final int col = c;
                TextView tv = new TextView(this);
                int cellSizePx = (int) (getResources().getDisplayMetrics().density * 52);
                tv.setWidth(cellSizePx);
                tv.setHeight(cellSizePx);
                tv.setGravity(Gravity.CENTER);
                tv.setText(String.valueOf(game.board.getCellNumber(row, col)));
                tv.setBackgroundResource(android.R.drawable.btn_default);
                tv.setPadding(2,2,2,2);
                tv.setOnClickListener(v -> onCellClicked(row, col));
                boardGrid.addView(tv);
            }
        }
    }

    private void onCellClicked(int row, int col) {
//        if (diceTotal == 0) {
//            Toast.makeText(this, "Roll the dice first!", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        final Player current = game.players.get(game.currentPlayerIndex);
//
//        boolean isForcedSkip = false;
//        String skipMessage = "";
//
//        if (diceTotal == 10) {
//            if (game.board.isBoardEmptyOr(current, game.isFourPlayerGame()) || game.board.allInSafePositions()) {
//                isForcedSkip = true;
//                skipMessage = "You had no opponent tokens to remove. Your turn is skipped.";
//            }
//        }
//        else if (diceTotal != 2 && diceTotal != 12 && diceTotal != 11) {
//            if (game.board.allTakenByPlayer(diceTotal, current.getId(), game.isFourPlayerGame())) {
//                isForcedSkip = true;
//                skipMessage = "All spots for a roll of " + diceTotal + " are already yours. Your turn is skipped.";
//            }
//        }
//
//        if (isForcedSkip) {
//            game.setPlayer();
//            Player next = game.players.get(game.currentPlayerIndex);
//            showSkipDialog(next, skipMessage);
//            diceTotal = 0;
//            return;
//        }
//        boolean success = false;
//        try {
//            success = game.handleRoll(current, diceTotal, row, col);
//        } catch (Exception e) {
//            Log.e("MainActivity", "Exception in handleRoll", e);
//        }
//
//        if (!success) {
//            Toast.makeText(this, "Invalid move! Try again.", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        updateBoardUI();
        if (diceTotal == 0) {
            Toast.makeText(this, "Roll the dice first!", Toast.LENGTH_SHORT).show();
            return;
        }

        final Player current = game.players.get(game.currentPlayerIndex);
        boolean success = false;
        try {
            success = game.handleRoll(current, diceTotal, row, col);
        } catch (Exception e) {
            Log.e("MainActivity", "Exception in handleRoll", e);
        }

        if (!success) {
            Toast.makeText(this, "Invalid move! Try again.", Toast.LENGTH_SHORT).show();
            return;
        }

        updateBoardUI();

        if (game.isGameWon(current)) {
            LeaderboardManager leaderboard = new LeaderboardManager(this);
            if (game.isFourPlayerGame()) {
                int teamId = current.getId() % 2;
                for (Player player : game.players) {
                    if (player.getId() % 2 == teamId) {
                        leaderboard.addPlayerIfNew(player.getName());
                        leaderboard.incrementWin(player.getName());
                    }
                }
            } else {
                leaderboard.addPlayerIfNew(current.getName());
                leaderboard.incrementWin(current.getName());
            }

            Intent intent = new Intent(this, Report_Activity.class);
            intent.putExtra("winnerText", current.getName());
            intent.putExtra("roundsPlayed", game.getRoundsPlayed());
            intent.putExtra("tokensPlaced", game.getTokensPlaced());
            intent.putExtra("tokensRemoved", game.getTokensRemoved());
            intent.putExtra("longestSequences", game.getLongestSequences());
            startActivity(intent);
        } else {
            game.setPlayer();
            Player next = game.players.get(game.currentPlayerIndex);
            showPassDialog(next);
            diceTotal = 0; // Reset dice for the next player
        }
    }

    private void showSkipDialog(Player next, String message) {
        new AlertDialog.Builder(this)
                .setTitle("Turn Skipped")
                .setMessage(message + "\n\nPass the device to " + next.getName() + " (" + next.getColour() + "). Tap Continue when ready.")
                .setPositiveButton("Continue", (d, w) -> {
                    txtCurrentPlayer.setText("Current Player: " + next.getName() + " (" + next.getColour() + ")");
                })
                .setCancelable(false)
                .show();
    }
    private void showPassDialog(Player next) {
        new AlertDialog.Builder(this)
                .setTitle("Pass Device")
                .setMessage("Pass the device to " + next.getName() + " (" + next.getColour() + "). Tap Continue when ready.")
                .setPositiveButton("Continue", (d, w) -> {
                    txtCurrentPlayer.setText("Current Player: " + next.getName() + " (" + next.getColour() + ")");
                })
                .setCancelable(false)
                .show();
    }

    private void updateBoardUI() {
        // update each cell view
        int childCount = boardGrid.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View v = boardGrid.getChildAt(i);
            int row = i / 6;
            int col = i % 6;
            if (!(v instanceof TextView)) continue;
            TextView tv = (TextView) v;
            int tokenOwner = game.board.getToken(row, col);
            tv.setText(String.valueOf(game.board.getCellNumber(row, col)));
            if (tokenOwner != 0) {
                Player owner = game.getPlayerById(tokenOwner);
                tv.setBackgroundColor(getColorFromName(owner.getColour()));
                tv.setTextColor(Color.WHITE);
            } else {
                tv.setBackgroundResource(android.R.drawable.btn_default);
                tv.setTextColor(Color.BLACK);
            }
        }
        Player current = game.players.get(game.currentPlayerIndex);
        txtCurrentPlayer.setText("Current Player: " + current.getName() + " (" + current.getColour() + ")");
    }

    private int getColorFromName(String name) {
        if (name == null) return Color.DKGRAY;
        switch (name.toLowerCase()) {
            case "red": return Color.RED;
            case "blue": return Color.BLUE;
            case "green": return Color.GREEN;
            case "yellow": return Color.YELLOW;
            default: return Color.DKGRAY;
        }
    }

    // Observer callbacks (optional UI updates)
    @Override public void onGameStarted() { Toast.makeText(this, "Game started", Toast.LENGTH_SHORT).show(); }
    @Override public void onTurnStarted(Player player) { txtCurrentPlayer.setText("Current Player: " + player.getName() + " (" + player.getColour() + ")"); }
    @Override public void onDiceRolled(int total) {}
    @Override public boolean onTokenPlaced(Player player, int row, int col) { runOnUiThread(this::updateBoardUI); return true; }
    @Override public boolean onTokenRemoved(Player player, int row, int col) { runOnUiThread(this::updateBoardUI); return true; }
    @Override public void onGameWon(Player player) { /* handled in onCellClicked */ }
}
