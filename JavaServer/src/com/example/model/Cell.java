package com.example.model;

public class Cell {
    private final int cellNumber;
    private int playerId;

    public Cell(int cellNumber) {
        this.cellNumber = cellNumber;
        this.playerId = 0;
    }

    public int getCellNumber() {
        return cellNumber;
    }

    public int getPlayerId() {
        return playerId;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    @Override
    public String toString() {

        return "[" + cellNumber + "," + playerId + "]";
    }
}
