package com.manhunt.spigot.utility;

import com.manhunt.spigot.Main;
import org.bukkit.event.Listener;

public class Handler implements Listener {

    private final Main instance;

    public Handler(Main instance) {
        this.instance = instance;
        instance.getServer().getPluginManager().registerEvents(this, instance);
    }

    protected Main getInstance() {
        return instance;
    }
}