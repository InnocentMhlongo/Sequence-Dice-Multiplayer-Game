package com.example.model;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;


public class SequenceDice implements Serializable {
    public  GameBoard board; //represents current state of gameboard
    public  List<Player> players;
    private int roundsPlayed = 0;
    private int tokensPlaced = 0;
    private int tokensRemoved = 0;
    public Player getPlayerById(int id){
        for(Player p : players){
            if(id == p.getId()){
                return p;
            }
        }
        return  null;
    }
    private  List<Observer> observers;//gameObservers are notified of game events
    public int currentPlayerIndex; // track current player's turn in the 'players' list.
    private  Random random;// Random object to simulate dice rolls.

    public int diceTotal;

    private Map<Player, List<Integer>> playerSequences; // Track sequences for each player
    private Map<Integer, List<Integer>> teamSequences; // Track sequences for each team
    private Map<Player, List<Integer>> longestPlayerSequences; // Longest sequences for players
    private Map<Integer, List<Integer>> longestTeamSequences;  // Longest sequences for teams
    public SequenceDice(List<Player> players) {
        this.board = new GameBoard();
        this.players = players;
        this.observers = new ArrayList<>();
        this.currentPlayerIndex = 0;
        this.random = new Random();
        this.diceTotal = 0;

        this.playerSequences = new HashMap<>();
        this.teamSequences = new HashMap<>();
        this.longestPlayerSequences = new HashMap<>();
        this.longestTeamSequences = new HashMap<>();

        for (Player player : players) {
            playerSequences.put(player, new ArrayList<Integer>());
            longestPlayerSequences.put(player, new ArrayList<Integer>());
        }
        if (players.size() == 4) {
            for (int i = 0; i < 2; i++) {
                teamSequences.put(i, new ArrayList<Integer>());
                longestTeamSequences.put(i, new ArrayList<Integer>());
            }
        }
    }
    public void resetGame(){
        this.board = new GameBoard();
        this.observers = new ArrayList<>();
        this.currentPlayerIndex = 0;
        this.random = new Random();
        this.playerSequences = new HashMap<>();
        this.teamSequences = new HashMap<>();
        this.longestPlayerSequences = new HashMap<>();
        this.longestTeamSequences = new HashMap<>();
        diceTotal = 0;
    }
    public void incrementRound() {
        roundsPlayed++;
    }
    public void addObserver(Observer observer) {
        observers.add(observer);
    }
    public void startGame() {
        notifyGameStarted();
    }

    public void setPlayer(){ //next player. Unless 2 or 12 is rolled
        int previousPlayerIndex = currentPlayerIndex;
        if(diceTotal != 12 && diceTotal != 2){
            currentPlayerIndex = (currentPlayerIndex+1)%players.size()  ;
        }
        // Increment rounds only if the turn moved to a new player
        if (currentPlayerIndex != previousPlayerIndex) {
            incrementRound();
        }
    }

    public boolean isGameWon(Player player) {
        int sequenceLength = players.size() <= 2 ? 5 : 6; // 5 for 2 players, 6 for 4 players
        int playerId = player.getId();
        int teamId = playerId % 2; // Team ID (0 for even, 1 for odd)

        if (players.size() == 4) { // Check for team win
            // Check rows for a winning sequence
            for (int row = 0; row < board.board.length; row++) {
                int consecutiveCount = 0;
                for (int col = 0; col < board.board[0].length; col++) {
                    int token = board.getToken(row, col);
                    if (token != 0 && token % 2 == teamId) {
                        consecutiveCount++;
                        if (consecutiveCount == sequenceLength) {
                            return true;
                        }
                    } else {
                        consecutiveCount = 0;
                    }
                }
            }

            // Check columns for a winning sequence
            for (int col = 0; col < board.board[0].length; col++) {
                int consecutiveCount = 0;
                for (int row = 0; row < board.board.length; row++) {
                    int token = board.getToken(row, col);
                    if (token != 0 && token % 2 == teamId) {
                        consecutiveCount++;
                        if (consecutiveCount == sequenceLength) {
                            return true;
                        }
                    } else {
                        consecutiveCount = 0;
                    }
                }
            }

            // Check diagonals (top-left to bottom-right) for a winning sequence
            for (int start = 0; start <= board.board.length - sequenceLength; start++) {
                int consecutiveCount = 0;
                for (int offset = 0; offset < board.board.length; offset++) {
                    int row = start + offset;
                    int col = offset;
                    if (row < board.board.length && col < board.board[0].length) {
                        int token = board.getToken(row, col);
                        if (token != 0 && token % 2 == teamId) {
                            consecutiveCount++;
                            if (consecutiveCount == sequenceLength) {
                                return true;
                            }
                        } else {
                            consecutiveCount = 0;
                        }
                    }
                }
            }

            // Check diagonals (bottom-left to top-right) for a winning sequence
            for (int start = 0; start <= board.board.length - sequenceLength; start++) {
                int consecutiveCount = 0;
                for (int offset = 0; offset < board.board.length; offset++) {
                    int row = board.board.length - 1 - start - offset;
                    int col = offset;
                    if (row >= 0 && col < board.board[0].length) {
                        int token = board.getToken(row, col);
                        if (token != 0 && token % 2 == teamId) {
                            consecutiveCount++;
                            if (consecutiveCount == sequenceLength) {
                                return true;
                            }
                        } else {
                            consecutiveCount = 0;
                        }
                    }
                }
            }
        } else {
            // Check rows for a winning sequence
            for (int row = 0; row < board.board.length; row++) {
                int consecutiveCount = 0;
                for (int col = 0; col < board.board[0].length; col++) {
                    if (board.getToken(row, col) == playerId) {
                        consecutiveCount++;
                        if (consecutiveCount == sequenceLength) {
                            return true;
                        }
                    } else {
                        consecutiveCount = 0;
                    }
                }
            }

            // Check columns for a winning sequence
            for (int col = 0; col < board.board[0].length; col++) {
                int consecutiveCount = 0;
                for (int row = 0; row < board.board.length; row++) {
                    if (board.getToken(row, col) == playerId) {
                        consecutiveCount++;
                        if (consecutiveCount == sequenceLength) {
                            return true;
                        }
                    } else {
                        consecutiveCount = 0;
                    }
                }
            }

            // Check diagonals (top-left to bottom-right) for a winning sequence
            for (int start = 0; start <= board.board.length - sequenceLength; start++) {
                int consecutiveCount = 0;
                for (int offset = 0; offset < board.board.length; offset++) {
                    int row = start + offset;
                    int col = offset;
                    if (row < board.board.length && col < board.board[0].length) {
                        if (board.getToken(row, col) == playerId) {
                            consecutiveCount++;
                            if (consecutiveCount == sequenceLength) {
                                return true;
                            }
                        } else {
                            consecutiveCount = 0;
                        }
                    }
                }
            }

            // Check diagonals (bottom-left to top-right) for a winning sequence
            for (int start = 0; start <= board.board.length - sequenceLength; start++) {
                int consecutiveCount = 0;
                for (int offset = 0; offset < board.board.length; offset++) {
                    int row = board.board.length - 1 - start - offset;
                    int col = offset;
                    if (row >= 0 && col < board.board[0].length) {
                        if (board.getToken(row, col) == playerId) {
                            consecutiveCount++;
                            if (consecutiveCount == sequenceLength) {
                                return true;
                            }
                        } else {
                            consecutiveCount = 0;
                        }
                    }
                }
            }
        }

        return false;
    }


    private void notifyGameStarted() {
        for (Observer observer : observers) {
            observer.onGameStarted();
        }
    }

    public void countTokenPlaced(){
        tokensPlaced++;
    }
    public void countTokenRemoved(){
        tokensRemoved++;
    }

    public int getTokensPlaced(){
        return  tokensPlaced;
    }
    public int getTokensRemoved(){
        return tokensRemoved;
    }

    public int getRoundsPlayed() {
        return roundsPlayed;
    }

    private boolean notifyTokenPlaced(Player player, int row, int col){
        for(Observer observer:observers){
            observer.onTokenPlaced(player,row,col);
        }
        updateSequence(player,row,col);
        countTokenPlaced();
        return true;
    }

    private boolean notifyTokenRemoved(Player player,int row,int col){
        for(Observer observer:observers){
            observer.onTokenRemoved(player,row,col);
        }
        updateSequence(player,row,col);
        countTokenRemoved();
        return true;
    }

    public boolean handleRoll(Player player, int total,int row,int col) {
        diceTotal = total;
        if (total == 2 || total == 12) {
            return handleExtraTurn(player, total,row,col);
        } else if (total == 10) {
            return handleDefensiveRoll(player,row,col);
        } else if (total == 11) {
            return handleWildRoll(player,row,col);
        } else {
            return   handleNormalRoll(player, total,row,col);
        }
    }

    private boolean handleExtraTurn(Player player, int total,int row,int col) {
        return handleNormalRoll(player, total,row,col);
        // Allow player another turn
    }

    private boolean handleDefensiveRoll(Player player, int row, int col) {
        //  remove an opponent's token that is not 2 or 12
        if (board.isBoardEmptyOr(player, isFourPlayerGame()) || board.allInSafePositions()) {
            return true; // No valid tokens to remove, skip turn
        }

        if(board.allInSafePositions()) return  true;
        if (!board.isCellEmpty(row, col) && board.getCellNumber(row, col) != 2 && board.getCellNumber(row, col) != 12 ) {
            if(isFourPlayerGame()){
                if(board.getTeamToken(row,col) != player.getId()%2){
                    board.removeToken(row, col);
                    return notifyTokenRemoved(player, row, col);
                }
            }
            else {
                if(board.getToken(row,col) != player.getId()){
                    board.removeToken(row, col);
                    return notifyTokenRemoved(player, row, col);
                }
            }
        }
        return false;
    }
    private void updateSequence(Player player, int row, int col) {
        int playerId = player.getId();
        int teamId = playerId % 2;

        // Get the longest sequence for the player/teams in rows, columns, and diagonals
        List<Integer> rowSequence = getLongestRowSequence(playerId, row);
        List<Integer> colSequence = getLongestColSequence(playerId, col);
        List<Integer> diagSequence1 = getLongestDiagonalSequence(playerId, true);  // Top-left to bottom-right
        List<Integer> diagSequence2 = getLongestDiagonalSequence(playerId, false); // Bottom-left to top-right

        // Determine the longest sequence among row, column, and diagonals
        List<Integer> longestSequence = getLongest(rowSequence, colSequence, diagSequence1, diagSequence2);

        // Update the player's longest sequence
        if (longestSequence.size() > longestPlayerSequences.get(player).size()) {
            longestPlayerSequences.put(player, new ArrayList<>(longestSequence));
        }

        // If there are 4 players, update the team sequence as well
        if (players.size() == 4) {
            List<Integer> teamSequence = longestTeamSequences.get(teamId);
            if (longestSequence.size() > teamSequence.size()) {
                longestTeamSequences.put(teamId, new ArrayList<>(longestSequence));
            }
        }
    }


    private List<Integer> getLongestRowSequence(int playerId, int row) {
        List<Integer> longestSequence = new ArrayList<>();
        List<Integer> currentSequence = new ArrayList<>();

        // Adjust playerId for team mode (if 4 players)
        int effectivePlayerId = (isFourPlayerGame()) ? playerId % 2 : playerId;

        for (int c = 0; c < board.board[0].length; c++) {
            // Compare using effectivePlayerId
            int token = board.getToken(row,c);
            if(isFourPlayerGame()){
                token= board.getTeamToken(row,c);
            }
            if (token == effectivePlayerId) {
                currentSequence.add(board.getCellNumber(row, c));
            } else {
                if (currentSequence.size() > longestSequence.size()) {
                    longestSequence = new ArrayList<>(currentSequence);
                }
                currentSequence.clear();
            }
        }
        if (currentSequence.size() > longestSequence.size()) {
            longestSequence = new ArrayList<>(currentSequence);
        }
        return longestSequence;
    }


    private List<Integer> getLongestColSequence(int playerId, int col) {
        List<Integer> longestSequence = new ArrayList<>();
        List<Integer> currentSequence = new ArrayList<>();

        // Adjust playerId for team mode (if 4 players)
        int effectivePlayerId = (isFourPlayerGame()) ? playerId % 2 : playerId;

        for (int r = 0; r < board.board.length; r++) {
            int token = board.getToken(r,col);
            if(isFourPlayerGame()){
                token = board.getTeamToken(r,col);
            }
            if (token == effectivePlayerId) {
                currentSequence.add(board.getCellNumber(r, col));
            } else {
                if (currentSequence.size() > longestSequence.size()) {
                    longestSequence = new ArrayList<>(currentSequence);
                }
                currentSequence.clear();
            }
        }
        if (currentSequence.size() > longestSequence.size()) {
            longestSequence = new ArrayList<>(currentSequence);
        }
        return longestSequence;
    }


    private List<Integer> getLongestDiagonalSequence(int playerId, boolean topLeftToBottomRight) {
        List<Integer> longestSequence = new ArrayList<>();
        List<Integer> currentSequence = new ArrayList<>();
        int size = board.board.length;

        // Adjust playerId for team mode (if 4 players)
        int effectivePlayerId = (isFourPlayerGame()) ? playerId % 2 : playerId;

        if (topLeftToBottomRight) {
            for (int d = 0; d < size; d++) {
                int r = d, c = d;
                int token = board.getToken(r,c);
                if(isFourPlayerGame()){
                    token = board.getTeamToken(r,c);
                }
                if (token == effectivePlayerId) {
                    currentSequence.add(board.getCellNumber(r, c));
                } else {
                    if (currentSequence.size() > longestSequence.size()) {
                        longestSequence = new ArrayList<>(currentSequence);
                    }
                    currentSequence.clear();
                }
            }
        } else {
            for (int d = 0; d < size; d++) {
                int r = size - 1 - d, c = d;
                int token = board.getToken(r,c);
                if(isFourPlayerGame()){
                    token = board.getTeamToken(r,c);
                }
                if (token == effectivePlayerId) {
                    currentSequence.add(board.getCellNumber(r, c));
                } else {
                    if (currentSequence.size() > longestSequence.size()) {
                        longestSequence = new ArrayList<>(currentSequence);
                    }
                    currentSequence.clear();
                }
            }
        }

        if (currentSequence.size() > longestSequence.size()) {
            longestSequence = new ArrayList<>(currentSequence);
        }

        return longestSequence;
    }


    public boolean isFourPlayerGame() {
        return players.size() == 4; // Replace numPlayers with the actual variable in your game
    }


    private List<Integer> getLongest(List<Integer>... sequences) {
        List<Integer> longest = new ArrayList<>();
        for (List<Integer> sequence : sequences) {
            if (sequence.size() > longest.size()) {
                longest = sequence;
            }
        }
        return longest;
    }



    private boolean handleWildRoll(Player player,int row,int col) {
        // Place token in any empty cell
        if (board.isCellEmpty(row, col)) {
            board.placeToken(row, col, player.getId());
            return notifyTokenPlaced(player, row, col);
        }
        return false;
    }

    private boolean handleNormalRoll(Player player, int total,int row,int col) {

        if (board.allTakenByPlayer(total, player.getId(), isFourPlayerGame())) {
            return true; // All spots are taken by the current player, skip turn
        }
        if(board.hasEmptyCell(total)){ //placing on empty spot
            if(board.getCellNumber(row,col) == total && board.isCellEmpty(row,col)){
                board.placeToken(row,col,player.getId());
                return notifyTokenPlaced(player,row,col);
            }
        }
        else{//replacing
            if(board.allTakenForSame(player.getId(),board.getCellNumber(row,col),isFourPlayerGame())){
                return true;
            }
            if(board.getCellNumber(row,col) == total){
                if(isFourPlayerGame()){
                    if(board.getTeamToken(row,col) != player.getId()%2){
                        board.replaceToken(row,col,player.getId());
                        countTokenRemoved();
                        return notifyTokenPlaced(player,row,col);
                    }
                }
                else if(board.getToken(row,col) != player.getId()){
                    board.replaceToken(row,col,player.getId());
                    countTokenRemoved();
                    return notifyTokenPlaced(player,row,col);
                }

            }
        }
        return  false;
    }

    public String getLongestSequences() {
        StringBuilder result = new StringBuilder();

        // If there are 2 or 3 players
        if (players.size() == 2 || players.size() == 3) {
            for (Player player : players) {
                List<Integer> sequence = longestPlayerSequences.get(player);
                result.append("Player: ").append(player.getName())
                        .append("/").append(player.getColour()) // Add player's color
                        .append(" - Longest Sequence: ").append(sequenceToString(sequence)).append("\n");
            }
        }
        // If there are 4 players (2 teams)
        else if (players.size() == 4) {
            for (int teamId = 0; teamId < 2; teamId++) {
                Player playerOfTeam = players.get(teamId+1);
                String colour = playerOfTeam.getColour();
                List<Integer> sequence = longestTeamSequences.get(teamId);
                result.append("Team: ").append(colour) // Team color
                        .append(" - Longest Sequence: ").append(sequenceToString(sequence)).append("\n");
            }
        }

        return result.toString();
    }

    // Helper method to convert sequence list to a comma-separated string
    private String sequenceToString(List<Integer> sequence) {
        return sequence.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
    }


}

