import com.example.model.GameCommand;
import com.example.model.GameState;
import com.example.model.Observer;
import com.example.model.Player;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class SequenceDiceClient implements Observer {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 5000;

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Player myPlayer;
    private boolean connected = false;
    private boolean gameActive = false;
    private boolean myTurn = false;
    private int lastDiceRoll = 0;
    private String gameId = "N/A";

    public SequenceDiceClient(String playerName) {
        int playerId = (int) (Math.random() * 100000);
        this.myPlayer = new Player(playerId, playerName, "");
    }

    public void start() {
        try {
            connectToServer();
            new Thread(this::listenForMessages, "Client-Listener").start();

            // Send initial join command
            GameCommand joinCmd = new GameCommand.Builder(GameCommand.CommandType.PLAYER_JOIN)
                    .playerId(myPlayer.getId())
                    .message(myPlayer.getName())
                    .build();
            sendCommand(joinCmd);

            System.out.println("--- Client " + myPlayer.getName() + " connected ---");
            System.out.println("Waiting for opponent(s)...");

            handleUserInput();

        } catch (IOException e) {
            System.err.println("Connection error: " + e.getMessage());
            cleanup();
        }
    }

    private void connectToServer() throws IOException {
        socket = new Socket(SERVER_HOST, SERVER_PORT);
        out = new ObjectOutputStream(socket.getOutputStream());
        out.flush();
        in = new ObjectInputStream(socket.getInputStream());
        connected = true;
    }

    private void listenForMessages() {
        try {
            while (connected) {
                GameCommand cmd = (GameCommand) in.readObject();
                handleServerMessage(cmd);
            }
        } catch (EOFException e) {
            System.out.println("\n[DISCONNECT] Server closed the connection.");
        } catch (IOException | ClassNotFoundException e) {
            if (connected) {
                System.out.println("\n[ERROR] Network error: " + e.getMessage());
            }
        } finally {
            cleanup();
        }
    }

    private void handleUserInput() {
        Scanner scanner = new Scanner(System.in);
        while (connected) {
            if (gameActive && myTurn) {
                if (lastDiceRoll == 0) {
                    System.out.print("\n>>> Your turn! Type 'roll' to roll the dice: ");
                    String input = scanner.nextLine().trim();
                    if (input.equalsIgnoreCase("roll")) {
                        rollDice();
                    }
                } else {
                    System.out.print("\n>>> Rolled " + lastDiceRoll + ". Enter move (row,col): ");
                    String input = scanner.nextLine().trim();
                    try {
                        String[] parts = input.split(",");
                        int row = Integer.parseInt(parts[0].trim());
                        int col = Integer.parseInt(parts[1].trim());
                        placeToken(row, col);
                    } catch (Exception e) {
                        System.out.println("Invalid input. Use format 'row,col'.");
                    }
                }
            } else {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    private void rollDice() {
        GameCommand rollCmd = new GameCommand.Builder(GameCommand.CommandType.DICE_ROLL)
                .playerId(myPlayer.getId())
                .build();
        sendCommand(rollCmd);
        System.out.println("[ACTION] Rolling dice...");
        myTurn = false;
    }

    private void placeToken(int row, int col) {
        if (lastDiceRoll == 0) {
            System.out.println("[ERROR] Must roll dice first!");
            return;
        }
        GameCommand placeCmd = new GameCommand.Builder(GameCommand.CommandType.TOKEN_PLACE)
                .playerId(myPlayer.getId())
                .diceTotal(lastDiceRoll)
                .row(row)
                .col(col)
                .build();
        sendCommand(placeCmd);
        System.out.println("[ACTION] Placing token at (" + row + "," + col + ")...");
        lastDiceRoll = 0;
        myTurn = false;
    }

    private void sendCommand(GameCommand cmd) {
        try {
            out.writeObject(cmd);
            out.flush();
            out.reset();
        } catch (IOException e) {
            System.err.println("[ERROR] Failed to send command: " + e.getMessage());
            connected = false;
        }
    }

    @Override
    public void handleServerMessage(GameCommand cmd) {
        switch (cmd.getType()) {
            case GAME_START -> handleGameStart(cmd);
            case DICE_ROLL -> handleDiceRoll(cmd);
            case GAME_STATE_UPDATE -> handleStateUpdate(cmd);
            case TURN_CHANGE -> handleTurnChange(cmd);
            case GAME_WIN -> handleGameWin(cmd);
            case ERROR -> handleError(cmd);
            default -> System.out.println("[SERVER] Unhandled command: " + cmd.getType());
        }
    }

    private void handleGameStart(GameCommand cmd) {
        GameState state = cmd.getGameState();
        gameId = state.getGameId();
        gameActive = true;

        for (Player p : state.getPlayers()) {
            if (p.getId() == myPlayer.getId()) {
                myPlayer = p;
                break;
            }
        }

        System.out.println("\n*** GAME " + gameId + " STARTED! ***");
        System.out.println("You are: " + myPlayer.getName() + " (" + myPlayer.getColour() + ")");
        System.out.println("Total players: " + state.getPlayers().size());

        // Notify observer
        onGameStarted();

        handleTurnChange(new GameCommand.Builder(GameCommand.CommandType.TURN_CHANGE)
                .playerId(state.getPlayers().get(0).getId())
                .build());
    }

    private void handleDiceRoll(GameCommand cmd) {
        Player roller = cmd.getPlayerId() == myPlayer.getId() ? myPlayer : new Player(cmd.getPlayerId(), "Opponent", "");
        lastDiceRoll = cmd.getDiceTotal();
        System.out.println("\n[ROLL] " + roller.getName() + " rolled a " + lastDiceRoll + "!");
        onDiceRolled(lastDiceRoll);

        if (cmd.getPlayerId() == myPlayer.getId()) {
            myTurn = true;
        }
    }

    private void handleStateUpdate(GameCommand cmd) {
        System.out.println("[UPDATE] Game state updated after move by Player ID: " + cmd.getPlayerId());
    }

    private void handleTurnChange(GameCommand cmd) {
        myTurn = cmd.getPlayerId() == myPlayer.getId();
        lastDiceRoll = 0;
        Player currentPlayer = new Player(cmd.getPlayerId(), "Next Player", "");

        if (myTurn) {
            System.out.println("\n*** IT IS YOUR TURN! ***");
            onTurnStarted(myPlayer);
        } else {
            System.out.println("\n--- Waiting for Player ID " + cmd.getPlayerId() + " to move. ---");
            onTurnStarted(currentPlayer);
        }
    }

    private void handleGameWin(GameCommand cmd) {
        Player winner = new Player(cmd.getPlayerId(), "Winner", "");
        System.out.println("\n!!! GAME OVER !!!");
        System.out.println(cmd.getMessage());
        onGameWon(winner);
        cleanup();
    }

    private void handleError(GameCommand cmd) {
        System.err.println("\n[SERVER ERROR] " + cmd.getMessage());
        if (cmd.getMessage().contains("disconnected") || cmd.getMessage().contains("Game ended")) {
            cleanup();
        }
    }

    private void cleanup() {
        if (!connected) return;
        connected = false;
        gameActive = false;
        System.out.println("Disconnected and client shut down.");
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException ignored) {}
        System.exit(0);
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter your player name: ");
        String playerName = scanner.nextLine().trim();
        if (playerName.isEmpty()) playerName = "ConsolePlayer";
        new SequenceDiceClient(playerName).start();
    }

    // Observer interface implementations
    @Override
    public void onGameStarted() {
        System.out.println("[OBSERVER] Game has started!");
    }

    @Override
    public void onTurnStarted(Player player) {
        System.out.println("[OBSERVER] Turn started for: " + player.getName());
    }

    @Override
    public void onDiceRolled(int total) {
        System.out.println("[OBSERVER] Dice rolled: " + total);
    }

    @Override
    public boolean onTokenPlaced(Player player, int row, int col) {
        System.out.println("[OBSERVER] Token placed by " + player.getName() + " at (" + row + "," + col + ")");
        return true;
    }

    @Override
    public boolean onTokenRemoved(Player player, int row, int col) {
        System.out.println("[OBSERVER] Token removed by " + player.getName() + " at (" + row + "," + col + ")");
        return true;
    }

    @Override
    public void onGameWon(Player player) {
        System.out.println("[OBSERVER] Game won by: " + player.getName());
    }
}
