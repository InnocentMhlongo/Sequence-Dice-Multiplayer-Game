package com.example.model;

import java.io.Serializable;
import java.util.List;

public class GameState implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int[][] boardState;
    private final List<Player> players;
    private final int currentPlayerIndex;
    private final int lastDiceRoll;
    private final int roundsPlayed;
    private final int tokensPlaced;
    private final int tokensRemoved;
    private final String gameId;

    public GameState(int[][] boardState, List<Player> players, int currentPlayerIndex,
                     int lastDiceRoll, int roundsPlayed, int tokensPlaced,
                     int tokensRemoved, String gameId) {
        this.boardState = boardState;
        this.players = players;
        this.currentPlayerIndex = currentPlayerIndex;
        this.lastDiceRoll = lastDiceRoll;
        this.roundsPlayed = roundsPlayed;
        this.tokensPlaced = tokensPlaced;
        this.tokensRemoved = tokensRemoved;
        this.gameId = gameId;
    }

    public int[][] getBoardState() { return boardState; }
    public List<Player> getPlayers() { return players; }
    public int getCurrentPlayerIndex() { return currentPlayerIndex; }
    public int getLastDiceRoll() { return lastDiceRoll; }
    public int getRoundsPlayed() { return roundsPlayed; }
    public int getTokensPlaced() { return tokensPlaced; }
    public int getTokensRemoved() { return tokensRemoved; }
    public String getGameId() { return gameId; }

    public static GameState fromGame(SequenceDice game, String gameId) {
        int[][] boardState = new int[6][6];
        for (int r = 0; r < 6; r++) {
            for (int c = 0; c < 6; c++) {
                boardState[r][c] = game.board.getToken(r, c);
            }
        }

        return new GameState(
                boardState,
                game.players,
                game.currentPlayerIndex,
                game.diceTotal,
                game.getRoundsPlayed(),
                game.getTokensPlaced(),
                game.getTokensRemoved(),
                gameId
        );
    }


    public void applyToGame(SequenceDice game) {
        // Update board
        for (int r = 0; r < 6; r++) {
            for (int c = 0; c < 6; c++) {
                int playerId = boardState[r][c];
                if (playerId != game.board.getToken(r, c)) {
                    if (playerId == 0) {
                        game.board.removeToken(r, c);
                    } else {
                        game.board.board[r][c].setPlayerId(playerId);
                    }
                }
            }
        }

        // Update current player
        game.currentPlayerIndex = currentPlayerIndex;
        game.diceTotal = lastDiceRoll;
    }
}