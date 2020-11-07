package com.manhunt.bungee.listener;

import com.manhunt.bungee.Main;
import com.manhunt.bungee.ReceivedDataEvent;
import com.manhunt.bungee.data.Game;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.*;

public class GameListener implements Listener {

    private final Main instance;

    public GameListener(Main instance) {
        this.instance = instance;
    }

    @EventHandler
    public void onDataReceive(ReceivedDataEvent event) {
        if (!event.getChannel().equals("manhunt:game")) {
            return;
        }
        List<UUID> players = event.getData().getObject("players");

        Game game = new Game(instance.getProxy().getServerInfo(event.getSender()));
        for (UUID player : players) {
            game.getPlayers().add(player);
        }
        instance.getGames().put(instance.getProxy().getServerInfo(event.getSender()), new HashSet<>());
        instance.getGames().get(instance.getProxy().getServerInfo(event.getSender())).addAll(players);
    }

    @EventHandler
    public void onDataReceiveEndGame(ReceivedDataEvent event) {
        if(!event.getChannel().equals("manhunt:endgame")) {
            return;
        }
        for (Map.Entry<ServerInfo, Set<UUID>> entry : instance.getGames().entrySet()) {
            if(entry.getKey().equals(instance.getProxy().getServerInfo(event.getSender()))) {
                instance.getGames().remove(entry.getKey());
            }
        }
    }
}