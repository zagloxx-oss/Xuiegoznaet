package com.minimilitary3d;

public class PlayerData {
    public String name;
    public boolean isReady;
    public boolean isHost;

    public PlayerData(String name, boolean isHost) {
        this.name = name;
        this.isHost = isHost;
        this.isReady = false;
    }
}