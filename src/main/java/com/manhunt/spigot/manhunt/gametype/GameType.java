package com.manhunt.spigot.manhunt.gametype;

public class GameType {

    private String gameType;
    private int hunters;

    public GameType(String gameType, int runners) {
        this.gameType = gameType;
        this.hunters = runners;
    }

    public String getGameType() {
        return gameType;
    }

    public void setGameType(String gameType) {
        this.gameType = gameType;
    }

    public int getHunters() {
        return hunters;
    }

    public void setHunters(int hunters) {
        this.hunters = hunters;
    }

    public int getRunners() {
        return 1;
    }

    public int getMinPlayers() {
        return getRunners() + getHunters();
    }
}