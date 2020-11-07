package com.manhunt.spigot.manhunt;

import com.manhunt.spigot.Main;

public class ManhuntManager {

    private final Manhunt manhunt;

    private final MenuHandler menuHandler;
    private final PlayerHandler playerHandler;

    public ManhuntManager(Main main) {
        this.manhunt = new Manhunt(main);
        this.menuHandler = new MenuHandler(main);
        this.playerHandler = new PlayerHandler(main);
    }

    public MenuHandler getMenuHandler() {
        return menuHandler;
    }

    public PlayerHandler getPlayerHandler() {
        return playerHandler;
    }

    public Manhunt getManhunt() {
        return manhunt;
    }
}