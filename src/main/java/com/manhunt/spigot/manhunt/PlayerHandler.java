package com.manhunt.spigot.manhunt;

import com.manhunt.spigot.Main;
import com.manhunt.spigot.manhunt.gui.ManhuntGUI;
import com.manhunt.spigot.utility.Handler;
import com.manhunt.spigot.utility.ItemBuilder;
import org.apache.commons.lang.WordUtils;
import org.bukkit.*;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class PlayerHandler extends Handler {

    public final ItemStack tracking_rod = new ItemBuilder(Material.FISHING_ROD).setDisplayName(ChatColor.RED.toString() + ChatColor.BOLD + "Tracking Rod").setLore(ChatColor.GRAY + "Shows the current coordinated of the hunted player.");
    public final ItemStack role_selector = new ItemBuilder(Material.SLIME_BALL).setDisplayName(ChatColor.RED.toString() + ChatColor.BOLD + "Role Selector").setLore(ChatColor.GRAY + "Right click to change your preferred role.");
    private final Map<UUID, Long> respawnMap = new HashMap<>();
    private final Map<UUID, Long> leaveTimer = new HashMap<>();

    public PlayerHandler(Main instance) {
        super(instance);

        new BukkitRunnable() {
            @Override
            public void run() {
                Manhunt manhunt = getInstance().getManhuntManager().getManhunt();
                for (Map.Entry<UUID, PlayStyle> entry : manhunt.getPlayStyleMap().entrySet()) {
                    Player player = Bukkit.getPlayer(entry.getKey());
                    if (player == null) {
                        continue;
                    }
                    UUID uuid = manhunt.getPlayStyleMap().entrySet().stream().filter(newEntry -> newEntry.getValue() == PlayStyle.HUNTED).map(Map.Entry::getKey).findFirst().orElse(null);
                    if (uuid == null) {
                        continue;
                    }
                    Player speedRunner = Bukkit.getPlayer(uuid);
                    if (speedRunner == null) {
                        continue;
                    }
                    if (player.getLocation().getWorld().getEnvironment().equals(speedRunner.getWorld().getEnvironment())) {
                        player.setCompassTarget(speedRunner.getLocation());
                    }
                }
                for (Iterator<Map.Entry<UUID, Long>> it = respawnMap.entrySet().iterator(); it.hasNext(); ) {
                    Map.Entry<UUID, Long> entry = it.next();
                    long sysTime = (System.currentTimeMillis() - entry.getValue());
                    Player player = Bukkit.getPlayer(entry.getKey());
                    if (player == null) {
                        it.remove();
                        continue;
                    }
                    if (player.getGameMode() != GameMode.SPECTATOR) {
                        player.setGameMode(GameMode.SPECTATOR);
                    }
                    if (sysTime >= 10000) {
                        player.setGameMode(PlayStyle.HUNTER.getGamemode());
                        Location spawnLoc = player.getBedSpawnLocation();
                        if (spawnLoc == null) {
                            spawnLoc = Bukkit.getWorlds().get(0).getSpawnLocation();
                        }
                        player.teleport(spawnLoc);
                        it.remove();
                    }
                }
                if(!leaveTimer.isEmpty()) {
                    for (Iterator<Map.Entry<UUID, Long>> it = leaveTimer.entrySet().iterator(); it.hasNext(); ) {
                        Map.Entry<UUID, Long> entry = it.next();

                        if (Bukkit.getPlayer(entry.getKey()) != null) {
                            it.remove();
                            continue;
                        }
                        if (System.currentTimeMillis() - entry.getValue() >= 60000) {
                            manhunt.end(true);
                            it.remove();
                        }
                    }
                }
            }
        }.runTaskTimer(getInstance(), 20L, 20L);
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        if (!event.getAction().name().contains("RIGHT")) {
            return;
        }
        Player player = event.getPlayer();
        if (!tracking_rod.isSimilar(player.getInventory().getItemInMainHand())) {
            return;
        }
        List<UUID> hunted = getInstance().getManhuntManager().getManhunt().getPlayStyles(PlayStyle.HUNTED);
        if (hunted.isEmpty()) {
            player.sendMessage(ChatColor.RED + "There is no hunted.");
            return;
        }
        Player huntedPlayer = Bukkit.getPlayer(hunted.get(0));
        Bukkit.broadcastMessage(player.getName() + " has used a tracking rod!");
        Bukkit.broadcastMessage(" ");
        Bukkit.broadcastMessage("Runner: " + huntedPlayer.getName());
        Bukkit.broadcastMessage("Co-Ordinates: <" + huntedPlayer.getLocation().getX() + ", " + huntedPlayer.getLocation().getY() + ", " + huntedPlayer.getLocation().getZ() + ">");
        Bukkit.broadcastMessage("World: " + WordUtils.capitalize(huntedPlayer.getLocation().getWorld().getEnvironment().name()));
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!event.getAction().name().contains("RIGHT")) {
            return;
        }
        if (!role_selector.isSimilar(player.getInventory().getItemInMainHand())) {
            return;
        }
        player.openInventory(new ManhuntGUI(getInstance().getManhuntManager(), player).getInventory());
    }

    @EventHandler
    public void onPlayerDrop(PlayerDropItemEvent event) {
        ItemStack item = event.getItemDrop().getItemStack();
        if(role_selector.isSimilar(item)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onItemMoveInventory(InventoryClickEvent event) {
        ItemStack currentItem = event.getCurrentItem();
        if(currentItem == null) {
            return;
        }
        if(role_selector.isSimilar(currentItem)) {
            event.setResult(Event.Result.DENY);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Manhunt manhunt = getInstance().getManhuntManager().getManhunt();
        if (!manhunt.getPlayStyleMap().containsKey(event.getPlayer().getUniqueId())) {
            return;
        }
        if (manhunt.getPlayStyleMap().get(event.getPlayer().getUniqueId()) != PlayStyle.HUNTER) {
            return;
        }
        if (!manhunt.isStarted()) {
            return;
        }
        if (System.currentTimeMillis() - manhunt.getStartTime() >= 30000) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler
    public void onHandleFrozenPvP(EntityDamageByEntityEvent event) {
        Manhunt manhunt = getInstance().getManhuntManager().getManhunt();
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        if (!(event.getDamager() instanceof Player)) {
            return;
        }
        Player damager = (Player) event.getDamager();
        if (!(manhunt.getPlayStyle(damager) == PlayStyle.HUNTER || manhunt.getPlayStyle((Player) event.getEntity()) == PlayStyle.HUNTER)) {
            return;
        }
        if (!manhunt.isStarted()) {
            return;
        }
        if (System.currentTimeMillis() - manhunt.getStartTime() >= 30000) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler
    public void handleRunnerRespawn(PlayerRespawnEvent event) {
        Manhunt manhunt = getInstance().getManhuntManager().getManhunt();
        if (!manhunt.getPlayStyleMap().containsKey(event.getPlayer().getUniqueId())) {
            return;
        }
        if (manhunt.getPlayStyleMap().get(event.getPlayer().getUniqueId()) != PlayStyle.HUNTED) {
            return;
        }
        manhunt.getPlayStyleMap().put(event.getPlayer().getUniqueId(), PlayStyle.SPECTATOR);
        event.getPlayer().setGameMode(GameMode.SPECTATOR);
    }

    @EventHandler
    public void handleHunterRespawn(PlayerRespawnEvent event) {
        Manhunt manhunt = getInstance().getManhuntManager().getManhunt();
        if (!manhunt.getPlayStyleMap().containsKey(event.getPlayer().getUniqueId())) {
            return;
        }
        if (manhunt.getPlayStyleMap().get(event.getPlayer().getUniqueId()) != PlayStyle.HUNTER) {
            return;
        }
        event.getPlayer().getInventory().addItem(new ItemStack(Material.COMPASS));
    }

    @EventHandler
    public void onHunterDeath(PlayerDeathEvent event) {
        Manhunt manhunt = getInstance().getManhuntManager().getManhunt();
        if (!manhunt.getPlayStyleMap().containsKey(event.getEntity().getUniqueId())) {
            return;
        }
        if (manhunt.getPlayStyleMap().get(event.getEntity().getUniqueId()) != PlayStyle.HUNTER) {
            return;
        }
        event.getDrops().removeIf(itemStack -> itemStack.getType() == Material.COMPASS);

        this.respawnMap.put(event.getEntity().getUniqueId(), System.currentTimeMillis());
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Manhunt manhunt = getInstance().getManhuntManager().getManhunt();
        Player entity = event.getEntity();
        if (!manhunt.getPlayStyleMap().containsKey(entity.getUniqueId())) {
            return;
        }
        PlayStyle style = manhunt.getPlayStyle(entity);
        if (style != PlayStyle.HUNTED) {
            return;
        }
        if (manhunt.getPlayStyleMap().entrySet().stream().noneMatch(entry -> {
            Player player1 = Bukkit.getPlayer(entry.getKey());
            return player1 != null && !player1.isDead() && entry.getValue() == PlayStyle.HUNTED;
        })) {
            manhunt.end(false);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Manhunt manhunt = getInstance().getManhuntManager().getManhunt();
        Player player = event.getPlayer();
        if (!manhunt.isStarted()) {
            manhunt.getPlayStyleMap().put(player.getUniqueId(), PlayStyle.NONE);

            player.getInventory().clear();
            player.getInventory().setItem(4, role_selector);
            player.openInventory(new ManhuntGUI(getInstance().getManhuntManager(), player).getInventory());
            return;
        }
        if (!manhunt.getPlayStyleMap().containsKey(player.getUniqueId())) {
            manhunt.getPlayStyleMap().put(player.getUniqueId(), PlayStyle.SPECTATOR);
        }
        player.setGameMode(manhunt.getPlayStyleMap().get(player.getUniqueId()).getGamemode());
    }

    @EventHandler
    public void onHandlePlayerJoinGameStart(PlayerJoinEvent event) {
        Manhunt manhunt = getInstance().getManhuntManager().getManhunt();
        if (manhunt.isStarted()) {
            return;
        }
        if (Bukkit.getOnlinePlayers().size() == manhunt.getGameType().getMinPlayers()) {
            manhunt.startGame();
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        event.getPlayer().setGameMode(GameMode.SURVIVAL);
        Manhunt manhunt = getInstance().getManhuntManager().getManhunt();
        if (manhunt.isStarted()) {
            return;
        }
        manhunt.getPlayStyleMap().remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPlayerQuitHandleGame(PlayerQuitEvent event) {
        leaveTimer.put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        Manhunt manhunt = getInstance().getManhuntManager().getManhunt();
        if (manhunt.isStarted()) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) {
            return;
        }
        Player damager = (Player) event.getDamager();
        Manhunt manhunt = getInstance().getManhuntManager().getManhunt();
        if (manhunt.getPlayStyle(damager) != PlayStyle.HUNTER) {
            return;
        }
        if (!(event.getEntity() instanceof EnderDragon)) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler
    public void onCompassDrop(PlayerDropItemEvent event) {
        Manhunt manhunt = getInstance().getManhuntManager().getManhunt();
        if (!manhunt.getPlayStyleMap().containsKey(event.getPlayer().getUniqueId())) {
            return;
        }
        if (manhunt.getPlayStyleMap().get(event.getPlayer().getUniqueId()) != PlayStyle.HUNTER) {
            return;
        }
        if (event.getItemDrop().getItemStack().getType() != Material.COMPASS) {
            return;
        }
        event.setCancelled(true);
    }
}
