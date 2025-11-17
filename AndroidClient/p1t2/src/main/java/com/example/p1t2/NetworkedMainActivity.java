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
import androidx.appcompat.app.AppCompatActivity;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.Random;


public class NetworkedMainActivity extends AppCompatActivity implements Observer{
    private static final String SERVER_HOST = "10.0.2.2"; // For emulator
    private static final int SERVER_PORT = 5000;

    private SequenceDice game;
    private TextView txtCurrentPlayer;
    private GridLayout boardGrid;
    private Button btnRollDice, btnRefreshBoard;
    private ImageView die1, die2;

    private ServerConnection serverConnection;
    private Player myPlayer;
    private String gameId;
    private boolean myTurn = false;
    private int lastDiceRoll = 0;

    private int[] diceImages = new int[]{
            R.drawable.die1, R.drawable.die2, R.drawable.die3,
            R.drawable.die4, R.drawable.die5, R.drawable.die6
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtCurrentPlayer = findViewById(R.id.txtCurrentPlayer);
        boardGrid = findViewById(R.id.boardGrid);
        btnRollDice = findViewById(R.id.btnRollDice);
        btnRefreshBoard = findViewById(R.id.btnShowBoard);
        die1 = findViewById(R.id.imgDie1);
        die2 = findViewById(R.id.imgDie2);

        // Get player info from intent
        String playerName = getIntent().getStringExtra("playerName");
        if (playerName == null || playerName.isEmpty()) {
            playerName = "Player" + (int)(Math.random() * 1000);
        }

        int playerId = (int)(Math.random() * 100000);
        myPlayer = new Player(playerId, playerName, "");

        setupBoardViews();
        connectToServer();

        btnRollDice.setOnClickListener(v -> rollDice());
        btnRefreshBoard.setOnClickListener(v -> updateBoardUI());
        btnRollDice.setEnabled(false);
    }

    private void connectToServer() {
        txtCurrentPlayer.setText("Connecting to server...");

        new Thread(() -> {
            try {
                serverConnection = new ServerConnection(SERVER_HOST, SERVER_PORT);
                serverConnection.setMessageHandler(this::handleServerMessage);

                runOnUiThread(() -> {
                    Toast.makeText(this, "Connected! Waiting for players...",
                            Toast.LENGTH_SHORT).show();
                    txtCurrentPlayer.setText("Waiting for game to start...");
                });

                // Send join command
                GameCommand joinCmd = new GameCommand.Builder(GameCommand.CommandType.PLAYER_JOIN)
                        .playerId(myPlayer.getId())
                        .message(myPlayer.getName())
                        .build();

                serverConnection.sendCommand(joinCmd);

            } catch (IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Connection failed: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                    Log.e("NetworkedMain", "Connection error", e);
                    finish();
                });
            }
        }).start();
    }

    private void handleServerMessage(GameCommand cmd) {
        runOnUiThread(() -> {
            switch (cmd.getType()) {
                case GAME_START:
                    handleGameStart(cmd);
                    break;

                case DICE_ROLL:
                    handleDiceRoll(cmd);
                    break;

                case GAME_STATE_UPDATE:
                    handleStateUpdate(cmd);
                    break;

                case TURN_CHANGE:
                    handleTurnChange(cmd);
                    break;

                case GAME_WIN:
                    handleGameWin(cmd);
                    break;

                case ERROR:
                    if (cmd.getMessage().contains("disconnected") ||
                            cmd.getMessage().contains("Game ended")) {
                        handleGameEnd(cmd.getMessage());
                    } else {
                        Toast.makeText(this, cmd.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        });
    }

    private void handleGameStart(GameCommand cmd) {
        GameState state = cmd.getGameState();
        gameId = state.getGameId();

        // Initialize local game
        game = new SequenceDice(state.getPlayers());
        state.applyToGame(game);

        // Find my player in the list
        for (Player p : game.players) {
            if (p.getId() == myPlayer.getId()) {
                myPlayer = p;
                break;
            }
        }

        Toast.makeText(this, "Game started! You are " + myPlayer.getColour(),
                Toast.LENGTH_LONG).show();

        updateBoardUI();

        Player currentPlayer = game.players.get(game.currentPlayerIndex);
        myTurn = (currentPlayer.getId() == myPlayer.getId());
        btnRollDice.setEnabled(myTurn);

        if (myTurn) {
            showTurnDialog();
        }
    }

    private void handleDiceRoll(GameCommand cmd) {
        lastDiceRoll = cmd.getDiceTotal();
        Player roller = game.getPlayerById(cmd.getPlayerId());

        // Animate dice
        animateDice(lastDiceRoll);

        Toast.makeText(this, roller.getName() + " rolled " + cmd.getDiceTotal(),
                Toast.LENGTH_SHORT).show();

        if (cmd.getPlayerId() == myPlayer.getId()) {
            Toast.makeText(this, "Select a cell to place your token",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void animateDice(int total) {
        // Animation settings
        int rollAnimations = 10; // number of face changes
        int delayTime = 100;     // milliseconds between frames
        Random random = new Random();

        new Thread(() -> {
            // Rolling animation effect
            for (int i = 0; i < rollAnimations; i++) {
                int dice1 = random.nextInt(6) + 1;
                int dice2 = random.nextInt(6) + 1;

                runOnUiThread(() -> {
                    die1.setImageResource(diceImages[dice1 - 1]);
                    die2.setImageResource(diceImages[dice2 - 1]);
                });

                try {
                    Thread.sleep(delayTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            // Final dice faces (based on given total)
            int dice1Val;
            if (total > 6) {
                dice1Val = total - 6;
            } else {
                dice1Val = total / 2;
            }
            int dice2Val = total - dice1Val;

            runOnUiThread(() -> {
                die1.setImageResource(diceImages[dice1Val - 1]);
                die2.setImageResource(diceImages[dice2Val - 1]);
            });
        }).start();
    }


    private void handleStateUpdate(GameCommand cmd) {
        cmd.getGameState().applyToGame(game);
        updateBoardUI();
    }

    private void handleTurnChange(GameCommand cmd) {
        Player nextPlayer = game.getPlayerById(cmd.getPlayerId());
        myTurn = (nextPlayer.getId() == myPlayer.getId());
        lastDiceRoll = 0;

        txtCurrentPlayer.setText("Current: " + nextPlayer.getName() +
                " (" + nextPlayer.getColour() + ")");
        btnRollDice.setEnabled(myTurn);

        if (myTurn) {
            showTurnDialog();
        }
    }

    private void handleGameWin(GameCommand cmd) {
        Player winner = game.getPlayerById(cmd.getPlayerId());
        GameState state = cmd.getGameState();

        // Update leaderboard
        LeaderboardManager leaderboard = new LeaderboardManager(this);
        if (game.isFourPlayerGame()) {
            int teamId = winner.getId() % 2;
            for (Player player : game.players) {
                if (player.getId() % 2 == teamId) {
                    leaderboard.addPlayerIfNew(player.getName());
                    leaderboard.incrementWin(player.getName());
                }
            }
        } else {
            leaderboard.addPlayerIfNew(winner.getName());
            leaderboard.incrementWin(winner.getName());
        }

        // Show results
        Intent intent = new Intent(this, Report_Activity.class);
        intent.putExtra("winnerText", winner.getName());
        intent.putExtra("roundsPlayed", state.getRoundsPlayed());
        intent.putExtra("tokensPlaced", state.getTokensPlaced());
        intent.putExtra("tokensRemoved", state.getTokensRemoved());
        intent.putExtra("longestSequences", game.getLongestSequences());
        startActivity(intent);
        finish();
    }

    private void showTurnDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Your Turn!")
                .setMessage("It's your turn. Roll the dice when ready.")
                .setPositiveButton("OK", null)
                .show();
    }
    private void handleGameEnd(String reason) {
        new AlertDialog.Builder(this)
                .setTitle("Game Ended")
                .setMessage(reason)
                .setPositiveButton("OK", (dialog, which) -> {
                    Intent intent = new Intent(NetworkedMainActivity.this, Menu_Activity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .setCancelable(false)
                .show();
    }

    private void rollDice() {
        if (!myTurn) {
            Toast.makeText(this, "Not your turn!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (lastDiceRoll != 0) {
            Toast.makeText(this, "Place your token first!", Toast.LENGTH_SHORT).show();
            return;
        }

        btnRollDice.setEnabled(false);

        // Send roll command to server
        new Thread(() -> {
            GameCommand rollCmd = new GameCommand.Builder(GameCommand.CommandType.DICE_ROLL)
                    .playerId(myPlayer.getId())
                    .build();
            serverConnection.sendCommand(rollCmd);
        }).start();
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
                tv.setText("0");
                tv.setBackgroundResource(android.R.drawable.btn_default);
                tv.setPadding(2, 2, 2, 2);
                tv.setOnClickListener(v -> onCellClicked(row, col));

                boardGrid.addView(tv);
            }
        }
    }

    private void onCellClicked(int row, int col) {
        if (!myTurn) {
            Toast.makeText(this, "Not your turn!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (lastDiceRoll == 0) {
            Toast.makeText(this, "Roll dice first!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Send token place command
        new Thread(() -> {
            GameCommand placeCmd = new GameCommand.Builder(GameCommand.CommandType.TOKEN_PLACE)
                    .playerId(myPlayer.getId())
                    .row(row)
                    .col(col)
                    .diceTotal(lastDiceRoll)
                    .build();
            serverConnection.sendCommand(placeCmd);
        }).start();
    }

    private void updateBoardUI() {
        if (game == null) return;

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

        if (game.currentPlayerIndex >= 0 && game.currentPlayerIndex < game.players.size()) {
            Player current = game.players.get(game.currentPlayerIndex);
            txtCurrentPlayer.setText("Current: " + current.getName() +
                    " (" + current.getColour() + ")");
        }
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serverConnection != null) {
            serverConnection.disconnect();
        }
    }
    @Override public void onGameStarted() { Toast.makeText(this, "Game started", Toast.LENGTH_SHORT).show(); }
    @Override public void onTurnStarted(Player player) { txtCurrentPlayer.setText("Current Player: " + player.getName() + " (" + player.getColour() + ")"); }
    @Override public void onDiceRolled(int total) {}
    @Override public boolean onTokenPlaced(Player player, int row, int col) { runOnUiThread(this::updateBoardUI); return true; }
    @Override public boolean onTokenRemoved(Player player, int row, int col) { runOnUiThread(this::updateBoardUI); return true; }
    @Override public void onGameWon(Player player) { /* handled in onCellClicked */ }

    static class ServerConnection {
        private Socket socket;
        private ObjectOutputStream out;
        private ObjectInputStream in;
        private MessageHandler handler;
        private volatile boolean running = true;

        interface MessageHandler {
            void handle(GameCommand message);
        }

        ServerConnection(String host, int port) throws IOException {
            socket = new Socket(host, port);
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());

            // Start listener thread
            new Thread(this::listen).start();
        }

        void setMessageHandler(MessageHandler handler) {
            this.handler = handler;
        }

        void sendCommand(GameCommand cmd) {
            try {
                synchronized (out) {
                    out.writeObject(cmd);
                    out.flush();
                    out.reset();
                }
            } catch (IOException e) {
                Log.e("ServerConnection", "Send failed", e);
            }
        }

        private void listen() {
            try {
                while (running) {
                    GameCommand cmd = (GameCommand) in.readObject();
                    if (handler != null) {
                        handler.handle(cmd);
                    }
                }
            } catch (EOFException | SocketException e) {
                // Server closed connection normally (game ended)
                if (running) {
                    Log.e("ServerConnection", "Server closed connection", e);
                    GameCommand errorCmd = new GameCommand.Builder(GameCommand.CommandType.ERROR)
                            .message("Connection to server closed.")
                            .build();
                    if (handler != null) {
                        handler.handle(errorCmd);
                    }
                }
            } catch (Exception e) {
                if (running) {
                    Log.e("ServerConnection", "Connection lost", e);
                    GameCommand errorCmd = new GameCommand.Builder(GameCommand.CommandType.ERROR)
                            .message("Connection to server lost. Game ended.")
                            .build();
                    if (handler != null) {
                        handler.handle(errorCmd);
                    }
                }
            }
        }

        void disconnect() {
            running = false;
            try {
                if (in != null) in.close();
                if (out != null) out.close();
                if (socket != null) socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}