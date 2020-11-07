package com.manhunt.spigot.commands;

import com.manhunt.spigot.Main;
import com.manhunt.spigot.manhunt.Manhunt;
import com.manhunt.spigot.manhunt.gui.ManhuntGUI;
import net.md_5.bungee.protocol.packet.Chat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ManhuntCommand implements CommandExecutor {

    private final Main main;

    public ManhuntCommand(Main main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(args.length >= 1) {
            if (args[0].equalsIgnoreCase("start") && sender.hasPermission("manhunt.start")) {
                startCommand(sender, args);
                return true;
            }
            if (args[0].equalsIgnoreCase("stop") && sender.hasPermission("manhunt.stop")) {
                stopCommand(sender, args);
                return true;
            }
            if(args[0].equalsIgnoreCase("give") && sender.hasPermission("manhunt.give")) {
                if (args.length >= 2) {
                    if(args[1].equalsIgnoreCase("rod")) {
                        giveRodCommand(sender, args);
                        return true;
                    }
                }
            }
        }
        if(sender instanceof Player) {
            Player player = (Player) sender;
            if (main.getManhuntManager().getManhunt().isStarted()) {
                player.sendMessage(ChatColor.GRAY + "You cannot change your playstyle. The game has already started.");
                return true;
            }
            player.openInventory(new ManhuntGUI(main.getManhuntManager(), player).getInventory());
        }
        return true;
    }

    private void giveRodCommand(CommandSender sender, String[] args) {
        Player receiver;
        if(args.length >= 3) {
            receiver = Bukkit.getPlayer(args[2]);
        } else if (sender instanceof Player) {
            receiver = (Player) sender;
        } else {
            return;
        }
        if(receiver == null) {
            sender.sendMessage("Cannot find player " + args[2] + ".");
            return;
        }

        Player player = (Player) sender;
        player.getInventory().addItem(main.getManhuntManager().getPlayerHandler().tracking_rod);
    }

    private void stopCommand(CommandSender sender, String[] args) {
    }

    private void startCommand(CommandSender sender, String[] args) {
        if(main.getManhuntManager().getManhunt().getGameState() != Manhunt.GameState.WAITING_FOR_PLAYERS) {
            sender.sendMessage(main.getManhuntManager().getManhunt().getGameState().getStartedText());
            return;
        }
        main.getManhuntManager().getManhunt().startGame();
    }
}