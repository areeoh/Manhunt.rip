package com.manhunt.spigot;

import com.manhunt.spigot.commands.ManhuntCommand;
import com.manhunt.spigot.connection.BungeeHandler;
import com.manhunt.spigot.manhunt.ManhuntManager;
import com.manhunt.spigot.options.Options;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private ManhuntManager manhuntManager;
    private Options options;
    private BungeeHandler bungeeHandler;

    @Override
    public void onEnable() {
        this.bungeeHandler = new BungeeHandler(this);
        this.options = new Options(this);
        this.manhuntManager = new ManhuntManager(this);

        getCommand("manhunt").setExecutor(new ManhuntCommand(this));
    }

    @Override
    public void onDisable() {
        this.bungeeHandler.onDisable();
    }

    public ManhuntManager getManhuntManager() {
        return manhuntManager;
    }

    public Options getOptions() {
        return options;
    }
}