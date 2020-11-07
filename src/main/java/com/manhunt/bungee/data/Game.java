package com.manhunt.bungee.data;

import net.md_5.bungee.api.config.ServerInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Game {

    private final ServerInfo server;
    private final List<UUID> players;

    public Game(ServerInfo server) {
        this.server = server;
        this.players = new ArrayList<>();
    }

    public ServerInfo getServer() {
        return server;
    }

    public List<UUID> getPlayers() {
        return players;
    }
}