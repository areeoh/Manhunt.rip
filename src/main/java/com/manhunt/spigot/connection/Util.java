package com.manhunt.spigot.connection;

import org.bukkit.Bukkit;

import java.util.logging.Level;

class Util {
    static void log(String message){
        Bukkit.getLogger().log(Level.INFO, message);
    }
}
