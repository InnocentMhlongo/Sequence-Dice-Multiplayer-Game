package com.example.model;

import java.io.Serializable;

public class GameCommand implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum CommandType {
        GAME_START,
        PLAYER_JOIN,
        DICE_ROLL,
        TOKEN_PLACE,
        TOKEN_REMOVE,
        TOKEN_REPLACE,
        TURN_CHANGE,
        GAME_WIN,
        GAME_STATE_UPDATE,
        ERROR
    }

    private final CommandType type;
    private final int playerId;
    private final int row;
    private final int col;
    private final int diceTotal;
    private final String message;
    private final GameState gameState;

    private GameCommand(Builder builder) {
        this.type = builder.type;
        this.playerId = builder.playerId;
        this.row = builder.row;
        this.col = builder.col;
        this.diceTotal = builder.diceTotal;
        this.message = builder.message;
        this.gameState = builder.gameState;
    }

    public CommandType getType() { return type; }
    public int getPlayerId() { return playerId; }
    public int getRow() { return row; }
    public int getCol() { return col; }
    public int getDiceTotal() { return diceTotal; }
    public String getMessage() { return message; }
    public GameState getGameState() { return gameState; }

    public static class Builder {
        private CommandType type;
        private int playerId = -1;
        private int row = -1;
        private int col = -1;
        private int diceTotal = 0;
        private String message = "";
        private GameState gameState;

        public Builder(CommandType type) {
            this.type = type;
        }

        public Builder playerId(int playerId) {
            this.playerId = playerId;
            return this;
        }

        public Builder row(int row) {
            this.row = row;
            return this;
        }

        public Builder col(int col) {
            this.col = col;
            return this;
        }

        public Builder diceTotal(int diceTotal) {
            this.diceTotal = diceTotal;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder gameState(GameState gameState) {
            this.gameState = gameState;
            return this;
        }

        public GameCommand build() {
            return new GameCommand(this);
        }
    }

    @Override
    public String toString() {
        return "com.example.model.GameCommand{type=" + type + ", playerId=" + playerId +
                ", row=" + row + ", col=" + col + ", diceTotal=" + diceTotal +
                ", message='" + message + "'}";
    }
}