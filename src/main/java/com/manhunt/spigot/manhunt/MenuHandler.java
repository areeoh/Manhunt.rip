package com.manhunt.spigot.manhunt;

import com.manhunt.spigot.Main;
import com.manhunt.spigot.utility.Handler;
import com.manhunt.spigot.gui.Button;
import com.manhunt.spigot.gui.GUI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

import java.util.HashSet;
import java.util.Set;

public class MenuHandler extends Handler {

    private final Set<GUI> guiSet = new HashSet<>();

    public MenuHandler(Main instance) {
        super(instance);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) {
            return;
        }
        if (event.getCurrentItem() == null) {
            return;
        }
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        if (!isGUI(event.getView().getTitle())) {
            return;
        }
        final GUI gui = getGUI(event.getInventory(), event.getView().getTitle());
        if (gui == null) {
            return;
        }
        final Button button = gui.getButton(event.getCurrentItem());
        if (button == null) {
            return;
        }
        button.onClick((Player) event.getWhoClicked());
        event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }
        final String title = event.getView().getTitle();
        if (!isGUI(title)) {
            return;
        }
        removeGUI(getGUI(event.getInventory(), title));
    }

    public void addGUI(GUI gui) {
        guiSet.add(gui);
    }

    public void removeGUI(GUI gui) {
        guiSet.remove(gui);
    }

    public GUI getGUI(Inventory inventory, String title) {
        for (GUI gui : guiSet) {
            if (gui.getTitle().equalsIgnoreCase(title)) {
                if (gui.getInventory().getViewers().size() > 0) {
                    if (gui.getPlayer().getUniqueId().equals(inventory.getViewers().get(0).getUniqueId())) {
                        return gui;
                    }
                }
            }
        }
        return null;
    }

    public boolean isGUI(String title) {
        for (GUI gui : guiSet) {
            if (gui.getTitle().equalsIgnoreCase(title)) {
                return true;
            }
        }
        return false;
    }
}
