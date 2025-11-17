package com.example.model;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
public class GameBoard implements Serializable {
    public final Cell[][] board;
    private final Map<Integer, Integer> cellCounts;

    public GameBoard() {

        //gameboard grid
        board = new Cell[][]{
                {new Cell(2), new Cell(3), new Cell(4), new Cell(5), new Cell(6), new Cell(2)},
                {new Cell(6), new Cell(7), new Cell(8), new Cell(9), new Cell(7), new Cell(3)},
                {new Cell(5), new Cell(9), new Cell(12), new Cell(12), new Cell(8), new Cell(4)},
                {new Cell(4), new Cell(8), new Cell(12), new Cell(12), new Cell(9), new Cell(5)},
                {new Cell(3), new Cell(7), new Cell(9), new Cell(8), new Cell(7), new Cell(6)},
                {new Cell(2), new Cell(6), new Cell(5), new Cell(4), new Cell(3), new Cell(2)}
        };

        this.cellCounts = new HashMap<>();
        for (int i = 2; i <= 12; i++) {
            cellCounts.put(i, 0);
        }
    }
    public boolean allTakenByPlayer(int number, int playerId, boolean isFourPlayerGame) {
        int ownedCount = 0;
        int totalCellsForNumber = 0;

        for (int r = 0; r < board.length; r++) {
            for (int c = 0; c < board[0].length; c++) {
                if (board[r][c].getCellNumber() == number) {
                    totalCellsForNumber++;
                    int tokenOwnerId = board[r][c].getPlayerId();
                    if (tokenOwnerId != 0) {
                        if (isFourPlayerGame) {
                            if (tokenOwnerId % 2 == playerId % 2) {
                                ownedCount++;
                            }
                        } else {
                            if (tokenOwnerId == playerId) {
                                ownedCount++;
                            }
                        }
                    }
                }
            }
        }
        return ownedCount == totalCellsForNumber;
    }
    public int getToken(int row, int col) {
        return board[row][col].getPlayerId();
    }
    public int getTeamToken(int row,int col){
        if(board[row][col].getPlayerId() == 0){
            return -1;
        }
        return board[row][col].getPlayerId()%2;
    }

    public void placeToken(int row, int col, int playerId) {
        board[row][col].setPlayerId(playerId);
        cellCounts.put(getCellNumber(row, col), cellCounts.get(getCellNumber(row, col)) + 1);
    }

    public void replaceToken(int row,int col,int playerId){
        board[row][col].setPlayerId(playerId);
    }

    public void removeToken(int row, int col) {
        int cellNumber = getCellNumber(row, col);
        cellCounts.put(cellNumber, cellCounts.get(cellNumber) - 1);
        board[row][col].setPlayerId(0);
    }

    public int getCellNumber(int row, int col) {
        return board[row][col].getCellNumber();
    }

    public boolean isCellEmpty(int row, int col) {
        return board[row][col].getPlayerId() == 0;
    }

    public boolean hasEmptyCell(int number) {
        return getCellCount(number) < 4;
    }
    public boolean allTakenForSame(int id, int number,boolean teamPlay){
        if(getCellCount(number) < 4 )return false;
        else{
            for(Cell[] cells : board){
                for(Cell c : cells){
                    if(c.getCellNumber() == number){
                        if(teamPlay){
                            if(id%2 != c.getPlayerId()%2){
                                return false;
                            }
                        }
                        else{
                            if(id != c.getPlayerId())return false;
                        }
                    }
                }
            }
        }

        return true;
    }
    private int getCellCount(int number) {
        return cellCounts.get(number);
    }

    @Override
    public String toString() {
        StringBuilder ans = new StringBuilder();
        for(int i = 0 ;i < 6 ;i++){
            for(int j = 0 ; j< 6;j++){
                if(board[i][j].getCellNumber()<12){
                    ans.append(board[i][j]).append("  ");
                }
                else{
                    ans.append(board[i][j]).append(" ");
                }
            }
            ans.append("\n");
        }
        return ans.toString();
    }
    public boolean isBoardEmptyOr(Player p,boolean teamPlay){
        for(Cell[] c : board){
            for(Cell c1:c){
                if(c1.getPlayerId() != 0){
                    if(teamPlay){
                        if(p.getId()%2 != c1.getPlayerId()%2){
                            return false;
                        }
                    }
                    else if(p.getId() != c1.getPlayerId()){
                        return false;
                    }

                }
            }
        }
        return  true;
    }
    public  boolean allInSafePositions(){
        for(Cell[] cells : board){
            for(Cell c : cells){
                if(c.getPlayerId() != 0 ){
                    if(c.getCellNumber() != 12 && c.getCellNumber() != 2){
                        return false;
                    }
                }
            }
        }
        return  true;
    }
}

