package com.gamecodeschool.snakegame;

public class Player extends Person {
    private String gameName;
    private int gameId;

    public Player(String name, int age, String gameName, int gameId) {
        super(name, age);
        this.gameName = gameName;
        this.gameId = gameId;
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public int getGameId() {
        return gameId;
    }

    public void setGameId(int gameId) {
        this.gameId = gameId;
    }
}
