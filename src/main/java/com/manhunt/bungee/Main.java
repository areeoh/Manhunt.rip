package com.manhunt.bungee;

import com.manhunt.bungee.commands.RejoinCommand;
import com.manhunt.bungee.data.Game;
import com.manhunt.bungee.listener.GameListener;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Main extends Plugin {

    private final Map<ServerInfo, Set<UUID>> games = new HashMap<>();

    @Override
    public void onEnable() {
        getProxy().getPluginManager().registerListener(this, new GameListener(this));
        getProxy().getPluginManager().registerCommand(this, new RejoinCommand(this));

        int port = -1;
        try {
            if (!getDataFolder().exists()) {
                if (!getDataFolder().mkdir()) {
                    throw new RuntimeException("Could not create configuration folder!");
                }
            }
            File config = new File(getDataFolder(), "config.yml");

            if (!config.exists()) {
                if (!config.createNewFile()) {
                    throw new RuntimeException("Could not create configuration file!");
                }
                Configuration conf = ConfigurationProvider.getProvider(YamlConfiguration.class).load(config);
                conf.set("port", 55555);
                ConfigurationProvider.getProvider(YamlConfiguration.class).save(conf, config);
                port = 55555;
            } else {
                port = ConfigurationProvider.getProvider(YamlConfiguration.class).load(config).getInt("port");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        SocketManager.init(port);
    }

    @Override
    public void onDisable() {
        SocketManager.end();
    }

    public Map<ServerInfo, Set<UUID>> getGames() {
        return games;
    }

    public ServerInfo getGame(UUID uuid) {
        for (Map.Entry<ServerInfo, Set<UUID>> entry : getGames().entrySet()) {
            if (entry.getValue().contains(uuid)) {
                return entry.getKey();
            }
        }
        return null;
    }
}