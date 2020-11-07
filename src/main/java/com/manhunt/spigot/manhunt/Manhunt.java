package com.manhunt.spigot.manhunt;

import com.manhunt.shared.Data;
import com.manhunt.spigot.Main;
import com.manhunt.spigot.connection.SocketAPI;
import com.manhunt.spigot.manhunt.gametype.GameType;
import com.manhunt.spigot.utility.RandomCollection;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Manhunt {

    private final Main main;
    private long startTime = 0L;
    private final GameType gameType;
    private final Map<UUID, PlayStyle> playStyleMap;
    private GameState gameState = GameState.WAITING_FOR_PLAYERS;

    public Manhunt(Main main) {
        this.main = main;
        this.gameType = main.getOptions().gameType;
        this.playStyleMap = new HashMap<>();
    }

    public void startGame() {
        if (getGameState() == GameState.STARTING) {
            return;
        }
        setGameState(GameState.STARTING);
        startCountdown();
    }

    public void startCountdown() {
        BukkitRunnable bukkitRunnable = new BukkitRunnable() {
            int countdown = 30;

            @Override
            public void run() {
                if (countdown <= 0) {
                    setStartTime(System.currentTimeMillis());
                    RandomCollection<Player> randomCollection = new RandomCollection<>();
                    List<UUID> uuidList = getPlayStyles(PlayStyle.HUNTED);
                    for (UUID uuid : uuidList) {
                        Player player = Bukkit.getPlayer(uuid);
                        randomCollection.add((100.0D / uuidList.size()) + getPercentageAdvantage(player), player);
                    }
                    List<Player> list = new ArrayList<>(Bukkit.getOnlinePlayers());
                    Player hunted = list.get(ThreadLocalRandom.current().nextInt(0, list.size()));

                    if (!randomCollection.isEmpty()) {
                        hunted = randomCollection.next();
                    }
                    getPlayStyleMap().put(hunted.getUniqueId(), PlayStyle.HUNTED);

                    for (Map.Entry<UUID, PlayStyle> entry : getPlayStyleMap().entrySet()) {
                        if (entry.getValue() == PlayStyle.HUNTED) {
                            if (hunted.getUniqueId() != entry.getKey()) {
                                getPlayStyleMap().put(entry.getKey(), PlayStyle.HUNTER);
                            }
                        }
                    }
                    for (Player online : Bukkit.getOnlinePlayers()) {
                        online.getInventory().clear();
                        if (getPlayStyle(online) == PlayStyle.NONE || getPlayStyle(online) == PlayStyle.SPECTATOR) {
                            addPlayer(online, PlayStyle.HUNTER);
                            online.getInventory().addItem(new ItemStack(Material.COMPASS));
                        }
                        online.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20);
                        online.setHealth(online.getAttribute(Attribute.GENERIC_MAX_HEALTH).getDefaultValue());
                        online.setFoodLevel(20);
                        online.setGameMode(getPlayStyle(online).getGamemode());
                        online.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
                    }
                    Bukkit.broadcastMessage(hunted.getName() + " is the hunted!");

                    Bukkit.broadcastMessage(ChatColor.GREEN + "Manhunt has begun! Good luck!");
                    Bukkit.broadcastMessage(ChatColor.GREEN + "All hunters have been frozen for 30 seconds!");
                    Data data = new Data();

                    ArrayList<UUID> players = new ArrayList<>(getPlayStyleMap().keySet());

                    data.addObject("players", players);
                    SocketAPI.sendDataToServer("manhunt:game", data);

                    this.cancel();
                    return;
                }
                if (countdown % 10 == 0 || countdown < 10) {
                    for (Player online : Bukkit.getOnlinePlayers()) {
                        online.sendTitle("", "Game starting in " + countdown + " seconds", 0, 20, 10);
                    }
                }
                countdown--;
            }
        };
        bukkitRunnable.runTaskTimer(main, 20L, 20L);
    }

    private void handleScoreboard(Player player) {
        Scoreboard newScoreboard = player.getScoreboard();
        Team ally = newScoreboard.getTeam("ally");
        if (ally == null) {
            ally = newScoreboard.registerNewTeam("ally");
        }
        Team enemy = newScoreboard.getTeam("enemy");
        if (enemy == null) {
            enemy = newScoreboard.registerNewTeam("enemy");
        }

        ally.setPrefix(ChatColor.GREEN + "");
        enemy.setPrefix(ChatColor.RED + "");

        ally.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
        enemy.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);

        for (Map.Entry<UUID, PlayStyle> entry : getPlayStyleMap().entrySet()) {
            Player other = Bukkit.getPlayer(entry.getKey());
            if (other == null) {
                continue;
            }
            PlayStyle value = entry.getValue();
            PlayStyle playStyle = getPlayStyle(player);

            if (value == playStyle) {
                ally.addEntry(other.getName());
            } else {
                enemy.addEntry(other.getName());
            }
        }
    }

    private double getPercentageAdvantage(Player player) {
        double extra = 0.0D;
        for (int i = 1; i < 10; i++) {
            if (player.hasPermission("manhunt.preference." + i + "0%")) {
                extra = i * 10.0D;
            }
        }
        return extra;
    }

    public GameType getGameType() {
        return gameType;
    }

    public boolean isStarted() {
        return startTime != 0;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public Map<UUID, PlayStyle> getPlayStyleMap() {
        return playStyleMap;
    }

    public List<UUID> getPlayStyles(PlayStyle... playStyle) {
        List<UUID> list = new ArrayList<>();
        for (Map.Entry<UUID, PlayStyle> entry : getPlayStyleMap().entrySet()) {
            if (Arrays.asList(playStyle).contains(entry.getValue())) {
                list.add(entry.getKey());
            }
        }
        return list;
    }

    public PlayStyle getPlayStyle(Player entity) {
        return getPlayStyleMap().getOrDefault(entity.getUniqueId(), PlayStyle.SPECTATOR);
    }

    public Main getMain() {
        return main;
    }

    public GameState getGameState() {
        return gameState;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    public void end(boolean forced) {
        new BukkitRunnable() {
            @Override
            public void run() {
                getPlayStyleMap().forEach((uuid, playStyle) -> {
                    Player player = Bukkit.getPlayer(uuid);
                    if (player != null) {
                        SocketAPI.connectPlayerToServer(player.getName(), "hub");
                    }
                });
            }
        }.runTaskLater(getMain(), 200L);

        if (!forced) {
            Bukkit.broadcastMessage(ChatColor.RED + "THE HUNTERS WIN!");
        } else {
            Bukkit.broadcastMessage(ChatColor.RED + "The game has ended.");
        }
        Bukkit.getOnlinePlayers().forEach(player -> player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1.0F, 1.0F));
        SocketAPI.sendDataToServer("manhunt:endgame", new Data());
    }

    //NOT REALLY USED ONLY USED TO CHECK IF GAME HAS STARTED (OTHER WAYS TO CHECK)
    //WAS GOING TO BE USED FOR THE MOTD API THING
    public enum GameState {
        WAITING_FOR_PLAYERS,
        STARTING(ChatColor.RED + "Game is already starting!"),
        IN_PROGRESS(ChatColor.RED + "Game is already in progess!");

        String startedText = "";

        GameState() {
        }

        GameState(String startedText) {
            this.startedText = startedText;
        }

        public String getStartedText() {
            return startedText;
        }
    }

    public void addPlayer(Player player, PlayStyle playStyle) {
        getPlayStyleMap().put(player.getUniqueId(), playStyle);
    }
}
