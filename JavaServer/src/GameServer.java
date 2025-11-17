import com.example.model.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

public class GameServer {
    private static final int SERVER_PORT = 5000;
    private static final int CONNECTION_TIMEOUT = 100; // milliseconds

    private final List<ClientHandler> waitingPlayers = new ArrayList<>();
    private final List<GameSession> activeSessions = new CopyOnWriteArrayList<>();
    private final ExecutorService gameExecutor = Executors.newCachedThreadPool();
    private final Random random = new Random();
    private ServerSocket serverSocket;
    private final ReentrantLock queueLock = new ReentrantLock();
    private int targetPlayerCount = 0; // Randomly determined when first player joins

    public static void main(String[] args) {
        new GameServer().start();
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(SERVER_PORT);
            serverSocket.setSoTimeout(CONNECTION_TIMEOUT);

            System.out.println("=== Sequence-Dice Game Server Started ===");
            System.out.println("Listening on port " + SERVER_PORT);
            System.out.println("Main thread handles connection acceptance AND player grouping\n");

            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    handleNewConnection(clientSocket);

                } catch (SocketTimeoutException e) {
                    checkAndStartGame();
                }
            }

        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleNewConnection(Socket clientSocket) {
        System.out.println("[CONNECTION] New client connected from " +
                clientSocket.getInetAddress());

        ClientHandler handler = new ClientHandler(clientSocket, this);

        // Start client thread to handle I/O
        Thread clientThread = new Thread(handler, "Client-" + clientSocket.getRemoteSocketAddress());
        clientThread.start();
    }

    // Called by ClientHandler when player joins
    public void addWaitingPlayer(ClientHandler handler) {
        queueLock.lock();
        try {
            waitingPlayers.add(handler);
            System.out.println("[QUEUE] Player '" + handler.getPlayerName() +
                    "' joined queue. Total waiting: " + waitingPlayers.size());

            // First player? Randomly determine how many players we need
            if (waitingPlayers.size() == 1) {
                targetPlayerCount = determinePlayerCount();
                System.out.println("[GROUPING] Waiting for " + targetPlayerCount + " players...");
            }

            System.out.println("[GROUPING] Need " + (targetPlayerCount - waitingPlayers.size()) +
                    " more player(s)");
        } finally {
            queueLock.unlock();
        }
    }

    // Main thread checks if enough players to start game
    private void checkAndStartGame() {
        queueLock.lock();
        try {
            if (waitingPlayers.size() >= targetPlayerCount && targetPlayerCount > 0) {
                System.out.println("\n[GROUPING] Enough players! Starting game with " +
                        targetPlayerCount + " players...");

                // Take required number of players
                List<ClientHandler> gamePlayers = new ArrayList<>();
                for (int i = 0; i < targetPlayerCount; i++) {
                    if (!waitingPlayers.isEmpty()) {
                        ClientHandler handler = waitingPlayers.remove(0);
                        if (handler.isConnected()) {
                            gamePlayers.add(handler);
                        } else {
                            // Player disconnected, need one more
                            i--;
                        }
                    }
                }

                if (gamePlayers.size() >= 2) {
                    createGameSession(gamePlayers);

                    // Reset for next game group
                    if (!waitingPlayers.isEmpty()) {
                        targetPlayerCount = determinePlayerCount();
                        System.out.println("[GROUPING] " + waitingPlayers.size() +
                                " player(s) still waiting. New target: " +
                                targetPlayerCount + " players");
                    } else {
                        targetPlayerCount = 0;
                    }
                } else {
                    // Not enough valid players, put them back
                    waitingPlayers.addAll(gamePlayers);
                }
            }
        } finally {
            queueLock.unlock();
        }
    }

    // Randomly determine 2-4 players (weighted: 40% for 2, 30% for 3, 30% for 4)
    private int determinePlayerCount() {
        int rand = random.nextInt(10);
        if (rand < 4) {
            System.out.println("[RANDOM] Selected 2 players");
            return 2;
        } else if (rand < 7) {
            System.out.println("[RANDOM] Selected 3 players");
            return 3;
        } else {
            System.out.println("[RANDOM] Selected 4 players");
            return 4;
        }
    }

    // Create and start a new game session in its own thread
    private void createGameSession(List<ClientHandler> handlers) {
        String gameId = "GAME_" + System.currentTimeMillis();
        GameSession session = new GameSession(gameId, handlers, this);
        activeSessions.add(session);

        // Each game runs in its own thread
        gameExecutor.submit(session);
    }

    // Remove player from queue (if connection lost before game starts)
    public void removeWaitingPlayer(ClientHandler handler) {
        queueLock.lock();
        try {
            waitingPlayers.remove(handler);
            System.out.println("[QUEUE] Player '" + handler.getPlayerName() +
                    "' left queue. Total waiting: " + waitingPlayers.size());
        } finally {
            queueLock.unlock();
        }
    }

    // Remove completed game session
    public void removeGameSession(GameSession session) {
        activeSessions.remove(session);
        System.out.println("[SERVER] Game " + session.gameId + " ended. Active games: " +
                activeSessions.size() + "\n");
    }

    static class ClientHandler implements Runnable {
        private final Socket socket;
        private final GameServer server;
        private ObjectOutputStream out;
        private ObjectInputStream in;
        private String playerName;
        private int playerId;
        private volatile boolean connected = true;
        private GameSession currentGame;
        private final ReentrantLock writeLock = new ReentrantLock();

        public ClientHandler(Socket socket, GameServer server) {
            this.socket = socket;
            this.server = server;
        }

        @Override
        public void run() {
            try {
                out = new ObjectOutputStream(socket.getOutputStream());
                out.flush();
                in = new ObjectInputStream(socket.getInputStream());

                // Wait for player to join
                GameCommand cmd = (GameCommand) in.readObject();

                if (cmd.getType() == GameCommand.CommandType.PLAYER_JOIN) {
                    this.playerId = cmd.getPlayerId();
                    this.playerName = cmd.getMessage();
                    System.out.println("[JOIN] Player '" + playerName + "' (ID: " + playerId + ") joined");

                    // Add to waiting queue (main thread will group them)
                    server.addWaitingPlayer(this);
                } else {
                    System.out.println("[ERROR] First command was not PLAYER_JOIN");
                    return;
                }

                // Listen for game commands
                while (connected) {
                    cmd = (GameCommand) in.readObject();
                    if (currentGame != null) {
                        currentGame.handleCommand(cmd);
                    }
                }

            } catch (EOFException e) {
                System.out.println("[DISCONNECT] Player '" + playerName + "' disconnected (EOF)");
            } catch (SocketException e) {
                System.out.println("[DISCONNECT] Player '" + playerName + "' disconnected (Socket closed)");
            } catch (Exception e) {
                System.out.println("[ERROR] Connection error with player '" + playerName + "': " +
                        e.getMessage());
            } finally {
                cleanup();
            }
        }

        public void sendCommand(GameCommand cmd) {
            writeLock.lock();
            try {
                out.writeObject(cmd);
                out.flush();
                out.reset();
            } catch (IOException e) {
                System.out.println("[ERROR] Failed to send to '" + playerName + "': " +
                        e.getMessage());
                connected = false;
            } finally {
                writeLock.unlock();
            }
        }

        public void setCurrentGame(GameSession game) {
            this.currentGame = game;
        }

        public String getPlayerName() {
            return playerName != null ? playerName : "Unknown";
        }

        public int getPlayerId() {
            return playerId;
        }

        public boolean isConnected() {
            return connected && !socket.isClosed();
        }

        public void closeConnection() {
            connected = false;
            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void cleanup() {
            connected = false;

            if (currentGame == null) {
                server.removeWaitingPlayer(this);
            } else {
                currentGame.handleDisconnect(this);
            }

            try {
                if (in != null) in.close();
                if (out != null) out.close();
                if (socket != null) socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    static class GameSession implements Runnable {
        private final String gameId;
        private final List<ClientHandler> clients;
        private final SequenceDice game;
        private final GameServer server;
        private final Random random = new Random();
        private volatile boolean gameActive = true;
        private final ReentrantLock gameLock = new ReentrantLock();

        public GameSession(String gameId, List<ClientHandler> clients, GameServer server) {
            this.gameId = gameId;
            this.clients = new CopyOnWriteArrayList<>(clients);
            this.server = server;

            // Create players
            String[] colors = {"Red", "Blue", "Green", "Yellow"};
            List<Player> players = new ArrayList<>();
            for (int i = 0; i < clients.size(); i++) {
                ClientHandler client = clients.get(i);
                Player player = new Player(client.getPlayerId(), client.getPlayerName(), colors[i]);
                players.add(player);
                client.setCurrentGame(this);
            }

            this.game = new SequenceDice(players);
        }

        @Override
        public void run() {
            try {
                System.out.println("[GAME " + gameId + "] Thread started with players: " +
                        getPlayerNames());
                startGame();

                System.out.println("[GAME " + gameId + "] Game thread running, coordinating gameplay...");

            } catch (Exception e) {
                System.out.println("[GAME " + gameId + "] Error: " + e.getMessage());
                e.printStackTrace();
                endGame();
            }
        }

        private String getPlayerNames() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < game.players.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(game.players.get(i).getName());
            }
            return sb.toString();
        }

        private void startGame() {
            GameState state = GameState.fromGame(game, gameId);
            GameCommand startCmd = new GameCommand.Builder(GameCommand.CommandType.GAME_START)
                    .gameState(state)
                    .message("Game starting!")
                    .build();

            broadcastToAll(startCmd);
            System.out.println("[GAME " + gameId + "] Game started, " +
                    game.players.get(0).getName() + "'s turn to roll");
        }

        public void handleCommand(GameCommand cmd) {
            if (!gameActive) return;

            gameLock.lock();
            try {
                Player currentPlayer = game.players.get(game.currentPlayerIndex);

                // Validate it's the correct player's turn
                if (cmd.getPlayerId() != currentPlayer.getId()) {
                    System.out.println("[GAME " + gameId + "] Invalid turn from player " +
                            cmd.getPlayerId() + " (expected " + currentPlayer.getId() + ")");
                    return;
                }

                switch (cmd.getType()) {
                    case DICE_ROLL:
                        handleDiceRoll(currentPlayer);
                        break;

                    case TOKEN_PLACE:
                        handleTokenPlace(cmd, currentPlayer);
                        break;
                }
            } finally {
                gameLock.unlock();
            }
        }

        private void handleDiceRoll(Player player) {
            int dice1 = random.nextInt(6) + 1;
            int dice2 = random.nextInt(6) + 1;
            int total = dice1 + dice2;
            game.diceTotal = total;

            System.out.println("[GAME " + gameId + "] " + player.getName() + " rolled " +
                    dice1 + " + " + dice2 + " = " + total);

            GameCommand rollCmd = new GameCommand.Builder(GameCommand.CommandType.DICE_ROLL)
                    .playerId(player.getId())
                    .diceTotal(total)
                    .message("Rolled " + total)
                    .build();

            broadcastToAll(rollCmd);
        }

        private void handleTokenPlace(GameCommand cmd, Player player) {
            boolean success = game.handleRoll(player, cmd.getDiceTotal(),
                    cmd.getRow(), cmd.getCol());

            if (success) {
                System.out.println("[GAME " + gameId + "] " + player.getName() +
                        " placed token at (" + cmd.getRow() + "," + cmd.getCol() + ")");

                // Broadcast updated state
                GameState state = GameState.fromGame(game, gameId);
                GameCommand updateCmd = new GameCommand.Builder(GameCommand.CommandType.GAME_STATE_UPDATE)
                        .playerId(player.getId())
                        .gameState(state)
                        .build();
                broadcastToAll(updateCmd);

                // Check for win
                if (game.isGameWon(player)) {
                    System.out.println("[GAME " + gameId + "] " + player.getName() + " WINS!");

                    GameCommand winCmd = new GameCommand.Builder(GameCommand.CommandType.GAME_WIN)
                            .playerId(player.getId())
                            .message(player.getName() + " wins!")
                            .gameState(state)
                            .build();
                    broadcastToAll(winCmd);

                    // Game over, close all connections and terminate
                    endGameAndCloseConnections();
                } else {
                    // Next turn
                    game.setPlayer();
                    Player nextPlayer = game.players.get(game.currentPlayerIndex);

                    System.out.println("[GAME " + gameId + "] Next turn: " + nextPlayer.getName());

                    GameCommand turnCmd = new GameCommand.Builder(GameCommand.CommandType.TURN_CHANGE)
                            .playerId(nextPlayer.getId())
                            .message("Your turn")
                            .build();
                    broadcastToAll(turnCmd);
                }
            } else {
                System.out.println("[GAME " + gameId + "] Invalid move by " + player.getName());

                GameCommand errorCmd = new GameCommand.Builder(GameCommand.CommandType.ERROR)
                        .playerId(player.getId())
                        .message("Invalid move")
                        .build();

                ClientHandler client = findClient(player.getId());
                if (client != null) {
                    client.sendCommand(errorCmd);
                }
            }
        }

        public void handleDisconnect(ClientHandler disconnected) {
            gameLock.lock();
            try {
                if (!gameActive) return;

                System.out.println("[GAME " + gameId + "] Player '" + disconnected.getPlayerName() +
                        "' disconnected during game");

                clients.remove(disconnected);

                if (clients.size() < 2) {
                    System.out.println("[GAME " + gameId + "] Not enough players remaining, ending game");

                    GameCommand errorCmd = new GameCommand.Builder(GameCommand.CommandType.ERROR)
                            .message("Player disconnected. Game ended.")
                            .build();
                    broadcastToAll(errorCmd);

                    endGameAndCloseConnections();
                }
            } finally {
                gameLock.unlock();
            }
        }

        private void broadcastToAll(GameCommand cmd) {
            for (ClientHandler client : clients) {
                if (client.isConnected()) {
                    client.sendCommand(cmd);
                }
            }
        }

        private ClientHandler findClient(int playerId) {
            for (ClientHandler client : clients) {
                if (client.getPlayerId() == playerId) {
                    return client;
                }
            }
            return null;
        }

        private void endGameAndCloseConnections() {
            gameActive = false;

            System.out.println("[GAME " + gameId + "] Terminating game and closing all connections...");

            // Small delay to ensure final messages are sent
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Close all client connections
            for (ClientHandler client : clients) {
                try {
                    client.closeConnection();
                } catch (Exception e) {
                    System.out.println("[ERROR] Failed to close connection for " +
                            client.getPlayerName() + ": " + e.getMessage());
                }
            }

            server.removeGameSession(this);
            System.out.println("[GAME " + gameId + "] Game thread terminated\n");
        }

        private void endGame() {
            gameActive = false;
            server.removeGameSession(this);
        }
    }
}