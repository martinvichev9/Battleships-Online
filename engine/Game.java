package bg.sofia.uni.fmi.mjt.battleships.engine;

import bg.sofia.uni.fmi.mjt.battleships.enums.Status;

public class Game {

    private static final int ONE_PLAYER = 1;
    private static final int MAX_PLAYERS_COUNT = 2;

    private String name;
    private String creator;
    private Status status;
    private int countPlayers;

    public Game(String name, String creator) {
        this.name = name;
        this.creator = creator;
        this.status = Status.PENDING;
        this.countPlayers = ONE_PLAYER;
    }

    public void startGame() {
        this.status = Status.IN_PROGRESS;
        ++this.countPlayers;
    }

    public void finishGame() {
        this.status = Status.PENDING;
        --this.countPlayers;
    }

    public String getName() {
        return this.name;
    }

    public String getCreator() {
        return this.creator;
    }

    public String getStatus() {
        return this.status.getStatus();
    }

    public String getPlayersCount() {
        return this.countPlayers + "/" + MAX_PLAYERS_COUNT;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("%-10s", "| " + name) + "|");
        builder.append(String.format("%-10s", " " + creator) + "|");
        builder.append(String.format("%-12s", " " + status.getStatus()) + "|");
        builder.append(String.format("%-10s", " " + getPlayersCount()) + "|");

        return builder.toString();
    }

}
