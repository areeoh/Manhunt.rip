package com.manhunt.spigot.options;

import com.manhunt.spigot.Main;
import com.manhunt.spigot.manhunt.gametype.GameType;
import com.manhunt.spigot.manhunt.gametype._1V1;
import com.manhunt.spigot.manhunt.gametype._1v2;
import com.manhunt.spigot.manhunt.gametype._1v3;

public class Options {

    private final Main main;
    public GameType gameType;

    public Options(Main main) {
        this.main = main;
        loadDefaults();
        loadGameType();
    }

    private void loadDefaults() {
        this.main.getConfig().options().copyDefaults(true);
        this.main.saveConfig();
    }

    private void loadGameType() {
        String string = this.main.getConfig().getString("Manhunt.GameType");
        if (string == null) {
            throw new NullPointerException("GameType cannot be null. Available types (1v1, 1v2, 1v3");
        }
        if (string.equalsIgnoreCase("1v1")) {
            this.gameType = new _1V1();
        }
        if (string.equalsIgnoreCase("1v2")) {
            this.gameType = new _1v2();
        }
        if (string.equalsIgnoreCase("1v3")) {
            this.gameType = new _1v3();
        }
        if (this.gameType == null) {
            throw new NullPointerException("GameType cannot be null. Available types (1v1, 1v2, 1v3");
        }
    }
}