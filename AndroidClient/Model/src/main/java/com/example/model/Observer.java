package com.example.model;
public interface Observer {
    void onGameStarted();
    void onTurnStarted(Player player);
    void onDiceRolled(int total);
    boolean onTokenPlaced(Player player, int row, int col);
    boolean onTokenRemoved(Player player, int row, int col);
    void onGameWon(Player player);
}