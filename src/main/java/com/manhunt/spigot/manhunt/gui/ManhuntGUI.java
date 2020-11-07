package com.manhunt.spigot.manhunt.gui;

import com.manhunt.spigot.gui.Button;
import com.manhunt.spigot.gui.GUI;
import com.manhunt.spigot.manhunt.ManhuntManager;
import com.manhunt.spigot.manhunt.PlayStyle;
import com.manhunt.spigot.utility.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class ManhuntGUI extends GUI {

    public ManhuntGUI(ManhuntManager manhuntManager, Player player) {
        super(ChatColor.GREEN + "Select your play style!", 27, player);

        addButton(new Button(new ItemBuilder(Material.FEATHER).setDisplayName(ChatColor.GREEN + "Hunted").setLore(ChatColor.GRAY + "Click here to become a " + ChatColor.AQUA + "Hunted"), 11) {
            @Override
            public void onClick(Player player) {
                manhuntManager.getManhunt().addPlayer(player, PlayStyle.HUNTED);
                player.sendMessage(ChatColor.GRAY + "You have selected " + ChatColor.AQUA + "Hunted");
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);
                player.closeInventory();
            }
        });

        addButton(new Button(new ItemBuilder(Material.IRON_SWORD).setDisplayName(ChatColor.GREEN + "Hunter").setLore(ChatColor.GRAY + "Click here to become a " + ChatColor.RED + "Hunter"), 15) {
            @Override
            public void onClick(Player player) {
                manhuntManager.getManhunt().addPlayer(player, PlayStyle.HUNTER);
                player.sendMessage(ChatColor.GRAY + "You have selected " + ChatColor.RED + "Hunter");
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);
                player.closeInventory();
            }
        });
        manhuntManager.getMenuHandler().addGUI(this);
        construct();
    }

    @Override
    public void onGUIClose() {
        getPlayer().sendMessage("You did not select a role.");
    }
}