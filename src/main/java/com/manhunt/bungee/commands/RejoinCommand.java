package com.manhunt.bungee.commands;

import com.manhunt.bungee.Main;
import com.manhunt.bungee.SocketAPI;
import com.manhunt.bungee.data.Game;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class RejoinCommand extends Command {

    private final Main instance;

    public RejoinCommand(Main instance) {
        super("rejoin");
        this.instance = instance;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(!(sender instanceof ProxiedPlayer)) {
            sender.sendMessage(new ComponentBuilder("You must be a player to run this command.").create());
            return;
        }
        ProxiedPlayer player = (ProxiedPlayer) sender;
        ServerInfo game = this.instance.getGame(player.getUniqueId());
        if(game == null) {
            sender.sendMessage(new ComponentBuilder("There is no server to reconnect to.").create());
            return;
        }
        if(game.equals(player.getServer().getInfo())) {
            sender.sendMessage(new ComponentBuilder("You are already connected to " + game.getName()).create());
            return;
        }
        sender.sendMessage(new ComponentBuilder("Reconnecting to " + game.getName()).create());
        player.connect(game);
    }
}
