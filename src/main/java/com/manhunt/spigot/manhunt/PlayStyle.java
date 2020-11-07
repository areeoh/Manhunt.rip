package com.manhunt.spigot.manhunt;

import org.bukkit.GameMode;

public enum PlayStyle {

    HUNTED(GameMode.SURVIVAL),
    HUNTER(GameMode.SURVIVAL),
    SPECTATOR(GameMode.SPECTATOR),
    NONE(GameMode.ADVENTURE);

    private final GameMode gamemode;

    PlayStyle(GameMode gamemode) {
        this.gamemode = gamemode;
    }

    public GameMode getGamemode() {
        return gamemode;
    }
}