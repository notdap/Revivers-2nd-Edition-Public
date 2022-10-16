package net.revivers.reviverstwo.arenas.games;

import com.nametagedit.plugin.NametagEdit;
import net.revivers.reviverstwo.ReviversTwo;
import net.revivers.reviverstwo.arenas.games.worlds.WorldManager;
import net.revivers.reviverstwo.matches.Match;
import net.revivers.reviverstwo.matches.MatchManager;
import net.revivers.reviverstwo.utilities.board.FastBoard;
import net.revivers.reviverstwo.arenas.Arena;
import net.revivers.reviverstwo.utilities.ActionBar;
import net.revivers.reviverstwo.utilities.Chat;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Game {

    private final Arena arena;
    private final String id;

    private int maxPlayers;
    private final int minPlayers;

    private final int blastProtection;
    private final int fireworkPercentage = ReviversTwo.getConfiguration().getInt("Firework Percentage");

    private int round = 0;
    private final AtomicInteger timer = new AtomicInteger(15);

    private final HashMap<Player, FastBoard> boards = new HashMap<>();
    private final LinkedHashMap<Player, Boolean> players = new LinkedHashMap<>();
    private final ArrayList<Player> spectators = new ArrayList<>();

    // Do not ask me how this works, it just works.
    private final List<Player> first = new ArrayList<>();
    private final List<Player> second = new ArrayList<>();
    private final List<Player> third = new ArrayList<>();

    private final World world;
    private final Location spawnLocation;
    private final Location lobbyLocation;

    private final AtomicInteger compassUpdaterTask = new AtomicInteger();
    private final AtomicInteger timerTaskId = new AtomicInteger(0);

    private boolean hasGameEnded = false;

    public Game(String id, Arena arena, int maxPlayers, int minPlayers, int blastProtection) {
        this.arena = arena;
        this.id = id;

        this.maxPlayers = maxPlayers;
        this.minPlayers = minPlayers;

        this.blastProtection = blastProtection;

        world = WorldManager.cloneWorld(this, arena);

        spawnLocation = new Location(
                world,
                arena.getSpawnLocation().getX(),
                arena.getSpawnLocation().getY(),
                arena.getSpawnLocation().getZ(),
                arena.getSpawnLocation().getYaw(),
                arena.getSpawnLocation().getPitch()
        );
        lobbyLocation = new Location(
                world,
                arena.getLobbyLocation().getX(),
                arena.getLobbyLocation().getY(),
                arena.getLobbyLocation().getZ(),
                arena.getLobbyLocation().getYaw(),
                arena.getLobbyLocation().getPitch()
        );

        updateScoreboards();
    }

    public void join(Player player) {
        PlayerHandler.setPlayerGame(player, this);

        player.setFoodLevel(20);
        player.setHealth(20);

        player.setGameMode(GameMode.ADVENTURE);
        player.closeInventory();

        if (round != 0) {
            addSpectator(player);
            return;
        } else if (players.size() >= maxPlayers) {
            addSpectator(player);
            return;
        }

        updateScoreboards();

        for (Player loopPlayer : players.keySet()) {
            loopPlayer.showPlayer(player);
            player.showPlayer(loopPlayer);
        }

        players.put(player, false);
        player.teleport(lobbyLocation);

        ItemStack bed = new ItemStack(Material.BED, 1);
        ItemMeta meta = bed.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&c&lReturn to Lobby &7(Right Click)"));
        bed.setItemMeta(meta);
        player.getInventory().setItem(8, bed);

        List<Player> totalPlayers = new ArrayList<>(players.keySet());
        totalPlayers.addAll(spectators);
        totalPlayers.forEach((loopPlayer) -> {
            loopPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', ReviversTwo.getChat().getPlayerSuffix(player) + player.getName() + "&e has joined (&b" + players.size() + "&e/&b" + maxPlayers + "&e)!"));
            loopPlayer.showPlayer(player);
            player.showPlayer(loopPlayer);
        });

        if (players.size() == minPlayers) {
            startLobbyTimer();
        }
    }

    public void leave(Player player, boolean inMatch) {
        if (players.containsKey(player)) {
            if (players.get(player)) untag(player);
        }

        NametagEdit.getApi().setPrefix(player, "&f");

        List<Player> playersToList = new ArrayList<>(players.keySet());
        runWinnerUpdater(playersToList);

        players.remove(player);
        spectators.remove(player);

        // ReviversTwo.getGhostFactory().removePlayer(player);

        updateScoreboards();

        player.getActivePotionEffects().forEach((potionEffect ->
                player.removePotionEffect(potionEffect.getType())));

        boards.get(player).delete();
        boards.remove(player);

        PlayerHandler.removePlayerGame(player);

        player.setExp(0);
        player.setLevel(0);

        ItemStack is = new ItemStack(Material.AIR, 1);
        player.getInventory().setHelmet(is);
        player.getInventory().clear();

        ActionBar actionBar = new ActionBar();
        actionBar.sendActionBar(player, "");

        player.setAllowFlight(false);
        player.setGameMode(GameMode.ADVENTURE);

        List<Player> totalPlayers = new ArrayList<>(players.keySet());
        totalPlayers.addAll(spectators);

        if (round == 0) {
            totalPlayers.forEach((loopPlayer) -> loopPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', ReviversTwo.getChat().getPlayerSuffix(player) + player.getName() + "&e has quit!")));
        }

        for (Player loopPlayer : Bukkit.getOnlinePlayers()) {
            loopPlayer.showPlayer(player);
            player.showPlayer(loopPlayer);
        }

        if (inMatch) {
            if (round != 0) {
                if (players.size() == 1 && !hasGameEnded && MatchManager.hasPlayerMatch(playersToList.get(0))
                        & MatchManager.getPlayerMatch(player) == MatchManager.getPlayerMatch(playersToList.get(0))) {
                    destroyGameRemains();
                } else if (players.size() <= 1 && !hasGameEnded) {
                    try {
                        Bukkit.getScheduler().cancelTask(timerTaskId.get());
                        playersToList.forEach(this::untag);
                        endGame();
                    } catch (Exception ignored) {}
                }
            }

            Location reviversLobbyLocation = ReviversTwo.getLobbyLocation();
            player.teleport(reviversLobbyLocation);
        } else {
            if (round != 0) {
                if (players.size() <= 1 && !hasGameEnded) {
                    try {
                        Bukkit.getScheduler().cancelTask(timerTaskId.get());
                        playersToList.forEach(this::untag);
                        endGame();
                    } catch (Exception ignored) {
                    }
                }
            }
        }

        for (Player onlinePlayer : players.keySet()) {
            onlinePlayer.showPlayer(player);
            player.showPlayer(onlinePlayer);
        }

        if (round == 0 && players.size() == 0) {
            destroyGameRemains();
        }
    }

    private void startRound() {
        round++;

        players.forEach((player, tagState) ->
                untag(player));

        ArrayList<Player> itPlayers = new ArrayList<>();

        Random randomizer = new Random();
        if (players.size() <= 6) {
            int random = randomizer.nextInt((players.size()));

            List<Player> playersList = new ArrayList<>(players.keySet());

            Player selectedIt = playersList.get(random);

            itPlayers.add(selectedIt);
        } else {
            int isItAmount = Math.round(((float) players.size() * ReviversTwo.getConfiguration().getInt("TNT Percentage")) / 100);

            List<Player> playerList = new ArrayList<>(players.keySet());
            for (int i = 0; i < isItAmount; i++) {
                int random = randomizer.nextInt((playerList.size()));

                itPlayers.add(playerList.get(random));
                playerList.remove(random);
            }
        }

        if (round == 1) {
            players.forEach((player, tagState) -> player.playSound(player.getLocation(), Sound.ENDERDRAGON_GROWL, 3.0f, 1.0f));
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("&eThe TNT has been released to ");
        AtomicInteger loopNumber = new AtomicInteger();

        itPlayers.forEach((player) -> {
            loopNumber.getAndIncrement();
            String separator = (loopNumber.get() == (itPlayers.size() - 1)) ? " and " : (loopNumber.get() == itPlayers.size()) ? "!" : ", ";
            stringBuilder.append(ReviversTwo.getChat().getPlayerSuffix(player)).append(player.getName()).append("&e").append(separator);
            tag(player);
        });

        players.forEach((player, tagState) -> {
            player.sendMessage("\n");
            String roundStartMessage = (players.size() > 6) ? "&f&lRound " + round + " has started!\n" : "&f&lDeathmatch has started!";
            Chat.sendCenteredMessageV2(player, roundStartMessage);
            Chat.sendCenteredMessageV2(player, stringBuilder.toString());
            player.sendMessage("\n");
            String startAs = (tagState) ? "&cYou started as IT! Give it to someone else quickly!\n" : "&aYou did NOT start as IT! Run away!\n";
            Chat.sendCenteredMessageV2(player, startAs);
            player.sendMessage("\n");
        });

        spectators.forEach((player -> {
            player.sendMessage("\n");
            String roundStartMessage = (players.size() > 6) ? "&f&lRound " + round + " has started!\n" : "&f&lDeathmatch has started!";
            Chat.sendCenteredMessageV2(player, roundStartMessage);
            Chat.sendCenteredMessageV2(player, stringBuilder.toString());
            player.sendMessage("\n");
        }));

        if (players.size() <= 6) {
            for (int i = 0; i < 3; i++) {
                players.forEach((player, tagState) ->
                        player.teleport(spawnLocation));
                spectators.forEach((player) ->
                        player.teleport(spawnLocation));
            }
        }

        startGameTimer();
    }

    private void startGameTimer() {
        int secondsToRemove = round * 5;
        if (secondsToRemove > 35) secondsToRemove = 35;

        int originalTimer = (60 - secondsToRemove) + 1;

        timer.set(originalTimer);
        AtomicBoolean hasReachedZero = new AtomicBoolean(false);

        timerTaskId.set(Bukkit.getScheduler().scheduleSyncRepeatingTask(ReviversTwo.getPlugin(), () ->
        {

            List<Player> totalPlayers = new ArrayList<>(players.keySet());
            totalPlayers.addAll(spectators);

            if (timer.get() > 0)
                timer.getAndDecrement();

            for (Player player : totalPlayers) {
                player.setExp((1.0F / Integer.toUnsignedLong(originalTimer)) * Integer.toUnsignedLong(timer.get()));
                player.setLevel(timer.get());
            }

            updateScoreboards();

            if (timer.get() == 0 && !hasReachedZero.get()) {
                hasReachedZero.set(true);

                List<Player> dominoEffect = new ArrayList<>();
                List<Player> toKill = new ArrayList<>();

                List<Player> players = new CopyOnWriteArrayList<>();
                List<Player> itPlayers = new CopyOnWriteArrayList<>();

                this.players.forEach((player, tagState) ->
                {
                    if (tagState) {
                        itPlayers.add(player);
                    } else {
                        players.add(player);
                    }
                });

                while (this.players.containsValue(true)) {
                    for (Player player : itPlayers) {
                        if (!dominoEffect.contains(player)) {
                            totalPlayers.forEach((toMessagePlayer) ->
                            {
                                if (toMessagePlayer.equals(player)) {
                                    toMessagePlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', "&eYou blew up!"));
                                } else {
                                    toMessagePlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', ReviversTwo.getChat().getPlayerSuffix(player) + player.getName() + "&e blew up!"));
                                }
                            });
                        }

                        List<Entity> nearbyEntities = player.getNearbyEntities(2.6, 2.6, 2.6);
                        for (Entity entity : nearbyEntities) {

                            if (!(entity instanceof Player nearbyPlayer)) continue;

                            if (PlayerHandler.getPlayerGame(nearbyPlayer) == null) continue;
                            if (PlayerHandler.getPlayerGame(nearbyPlayer) != this) continue;
                            if (!players.contains(nearbyPlayer)) continue;

                            if (toKill.contains(nearbyPlayer)) continue;

                            Random random = new Random();
                            int blastProtectionPercentage = random.nextInt(100) + 1;
                            if (blastProtectionPercentage <= blastProtection) {
                                nearbyPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', "&eYour &bBlast Protection&e saved you from " + ReviversTwo.getChat().getPlayerSuffix(player) + player.getName() + "&e's explosion!"));
                                continue;
                            }

                            this.players.replace(nearbyPlayer, true);
                            players.remove(nearbyPlayer);
                            dominoEffect.add(nearbyPlayer);
                            itPlayers.add(nearbyPlayer);

                            totalPlayers.forEach((toMessagePlayer) ->
                            {
                                if (toMessagePlayer.equals(nearbyPlayer)) {
                                    toMessagePlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', "&eYou were blown up by " + ReviversTwo.getChat().getPlayerSuffix(player) + player.getName() + "&e's explosion!"));
                                } else {
                                    toMessagePlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', ReviversTwo.getChat().getPlayerSuffix(nearbyPlayer) + nearbyPlayer.getName() + "&e blew up!"));
                                }
                            });

                        }

                        List<Player> combinedLists = Stream.of(players, itPlayers)
                                .flatMap(Collection::stream)
                                .collect(Collectors.toList());

                        runWinnerUpdater(combinedLists);

                        world.playEffect(player.getLocation(), Effect.EXPLOSION_HUGE, 0);

                        for (Player loopPlayer : totalPlayers) {
                            if (loopPlayer == player) continue;
                            loopPlayer.playSound(player.getLocation(), Sound.EXPLODE, 100F, 0.5F);
                        }

                        untag(player);
                        itPlayers.remove(player);
                        toKill.add(player);
                        addSpectator(player);

                        player.playSound(player.getLocation(), Sound.EXPLODE, 100F, 0.5F);

                        runWinnerUpdater(players);
                    }
                }

                if (toKill.size() == this.players.size()) {
                    first.clear();
                }

                this.players.keySet().removeIf(toKill::contains);

                if (players.size() <= 1) {
                    endGame();
                } else {
                    Bukkit.getScheduler().scheduleSyncDelayedTask(ReviversTwo.getPlugin(), this::startRound, 8 * 20);
                }

                Bukkit.getScheduler().cancelTask(timerTaskId.get());

                updateScoreboards();

            }

        }, 0, 20));
    }

    public void runWinnerUpdater(List<Player> playerList) {
        playerList.forEach((winPlayerUpdate) ->
        {
            switch (playerList.size()) {
                case 3 -> third.add(winPlayerUpdate);
                case 2 -> {
                    third.remove(winPlayerUpdate);
                    second.add(winPlayerUpdate);
                }
                case 1 -> {
                    second.remove(winPlayerUpdate);
                    first.add(winPlayerUpdate);
                }
            }
        });
    }

    public void tag(Player attacker, Player victim) {
        if (!isIt(attacker) || isIt(victim)) return;

        untag(attacker);
        tag(victim);

        List<Player> totalPlayers = new ArrayList<>(players.keySet());
        totalPlayers.addAll(spectators);

        attacker.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aYou tagged " + ReviversTwo.getChat().getPlayerSuffix(victim) + victim.getName() + "&a!"));
        victim.sendMessage((ChatColor.translateAlternateColorCodes('&', ReviversTwo.getChat().getPlayerSuffix(attacker) + attacker.getName() + "&c tagged you!")));
        totalPlayers.forEach((player) -> {
            if (player.equals(attacker) || player.equals(victim)) return;
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', ReviversTwo.getChat().getPlayerSuffix(victim) + victim.getName() + "&7 is IT!"));
        });
    }

    public void tag(Player player) {
        players.replace(player, true);

        player.removePotionEffect(PotionEffectType.SPEED);
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 3));

        player.playSound(player.getLocation(), Sound.NOTE_PLING, 3.0f, 2.0f);

        NametagEdit.getApi().setPrefix(player, "&c[IT] ");

        player.getInventory().setHelmet(new ItemStack(Material.TNT, 1));
        player.getInventory().setItem(0, new ItemStack(Material.TNT, 1));

        ItemStack compass = new ItemStack(Material.COMPASS, 1);
        ItemMeta meta = compass.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&aNearest Player"));
        compass.setItemMeta(meta);
        player.getInventory().setItem(1, compass);

        updateScoreboards();

        Firework fw = (Firework) player.getWorld().spawnEntity(player.getLocation(), EntityType.FIREWORK);
        FireworkMeta fwm = fw.getFireworkMeta();

        fwm.setPower(0);
        fwm.addEffect(FireworkEffect.builder().with(FireworkEffect.Type.CREEPER).withColor(Color.GREEN).withFade(Color.RED).build());

        fw.setFireworkMeta(fwm);

        Bukkit.getScheduler().runTaskLaterAsynchronously(ReviversTwo.getPlugin(), () -> {
            Random random = new Random();
            int fireworkChance = random.nextInt(100) + 1;
            if (fireworkChance <= fireworkPercentage) {
                Bukkit.getScheduler().runTaskLaterAsynchronously(ReviversTwo.getPlugin(), fw::detonate, 7);
            } else {
                fw.remove();
            }
        }, 2);
    }

    public void untag(Player player) {
        player.removePotionEffect(PotionEffectType.SPEED);
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));

        players.replace(player, false);
        updateScoreboards();

        NametagEdit.getApi().setPrefix(player, "&f");
        ItemStack is;
        if (MatchManager.hasPlayerMatch(player)) {
            Match playerMatch = MatchManager.getPlayerMatch(player);
            is = playerMatch.getPlayerHat(player).item();
        } else {
            is = new ItemStack(Material.AIR, 1);
        }
        player.getInventory().setHelmet(is);

        player.getInventory().setItem(0, new ItemStack(Material.AIR, 0));
        player.getInventory().setItem(1, new ItemStack(Material.AIR, 0));
    }

    public void kill(Player player) {
        players.remove(player);
        untag(player);
        addSpectator(player);

        updateScoreboards();
        runWinnerUpdater(new ArrayList<>(players.keySet()));

        if (players.size() <= 1)
            endGame();
    }

    public void addSpectator(Player player) {
        ActionBar actionBar = new ActionBar();
        actionBar.sendActionBar(player, "");
        player.teleport(lobbyLocation);
        spectators.add(player);

        NametagEdit.getApi().setPrefix(player, "&7[DEAD] ");

        player.setAllowFlight(true);

        player.getInventory().clear();
        player.getInventory().setHelmet(new ItemStack(Material.AIR, 1));

        // ReviversTwo.getGhostFactory().addPlayer(player);
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0));

        for (Player loopedPlayer : players.keySet()) {
            loopedPlayer.hidePlayer(player);
            for (Player spectatorLoopPlayer : spectators) {
                loopedPlayer.hidePlayer(spectatorLoopPlayer);
            }
        }

        for (Player spectatorLoopPlayer : spectators) {
            player.hidePlayer(spectatorLoopPlayer);
            spectatorLoopPlayer.hidePlayer(player);
        }

        if (MatchManager.hasPlayerMatch(player)) {
            if (MatchManager.getPlayerMatch(player).getLeader() == player) {
                // Play Again item
                ItemStack paper = new ItemStack(Material.PAPER, 1);
                ItemMeta paperMeta = paper.getItemMeta();
                paperMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b&lPlay Again &7(Right Click)"));
                paper.setItemMeta(paperMeta);
                player.getInventory().setItem(7, paper);
            }
        }

        // Return to Lobby (bed) item
        ItemStack bed = new ItemStack(Material.BED, 1);
        ItemMeta bedMeta = bed.getItemMeta();
        bedMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&c&lReturn to Lobby &7(Right Click)"));
        bed.setItemMeta(bedMeta);
        player.getInventory().setItem(8, bed);

        // Player Teleporter item
        ItemStack playerTeleporter = new ItemStack(Material.COMPASS, 1);
        ItemMeta playerTeleporterMeta = playerTeleporter.getItemMeta();
        playerTeleporterMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&a&lTeleporter &7(Right Click)"));
        playerTeleporter.setItemMeta(playerTeleporterMeta);
        player.getInventory().setItem(0, playerTeleporter);
    }

    public void endGame() {
        hasGameEnded = true;

        List<Player> totalPlayers = new ArrayList<>(players.keySet());
        totalPlayers.addAll(spectators);

        totalPlayers.forEach((player -> {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
            Chat.sendCenteredMessageV2(player, "&f&lTNT Tag");
            player.sendMessage("");
            if (first.size() >= 1) {
                Chat.sendCenteredMessageV2(player, "&e&l1st Place: " + ReviversTwo.getChat().getPlayerPrefix(first.get(0)) + first.get(0).getName());
                if (second.size() > 0)
                    Chat.sendCenteredMessageV2(player, "&e&l2nd Place: " + ReviversTwo.getChat().getPlayerPrefix(second.get(0)) + second.get(0).getName());
                if (third.size() > 0)
                    Chat.sendCenteredMessageV2(player, "&e&l3rd Place: " + ReviversTwo.getChat().getPlayerPrefix(third.get(0)) + third.get(0).getName());
            } else {
                Chat.sendCenteredMessageV2(player, "&eWinner &7-&c DRAW!");
            }
            player.sendMessage("");
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
        }));

        Bukkit.getScheduler().runTaskLater(ReviversTwo.getPlugin(), this::destroyGameRemains, 10 * 20);
    }

    public void destroyGameRemains() {
        List<Player> totalPlayers = new ArrayList<>(players.keySet());
        totalPlayers.addAll(spectators);

        totalPlayers.forEach((player) -> this.leave(player, false));

        if (round != 0)
            Bukkit.getScheduler().cancelTask(compassUpdaterTask.get());

        GameManager.removeGame(id);
    }

    public void startLobbyTimer() {
        List<Player> totalPlayers = new ArrayList<>(players.keySet());
        totalPlayers.addAll(spectators);

        timer.set(15);
        AtomicInteger taskId = new AtomicInteger();

        AtomicBoolean hasReachedZero = new AtomicBoolean(false);

        taskId.set(Bukkit.getScheduler().scheduleSyncRepeatingTask(ReviversTwo.getPlugin(), () ->
        {
            if (!hasReachedZero.get()) {

                timer.getAndDecrement();

                if (players.size() < minPlayers) {
                    Bukkit.getScheduler().cancelTask(taskId.get());
                    timer.set(15);
                    updateScoreboards();
                    return;
                }

                updateScoreboards();

                if (timer.get() == 10) {
                    String message = ChatColor.translateAlternateColorCodes('&', "&eThe game starts in &6" + timer.get() + "&e seconds!");
                    totalPlayers.forEach((forEachPlayer) -> {
                        forEachPlayer.sendMessage(message);
                        forEachPlayer.playSound(forEachPlayer.getLocation(), Sound.NOTE_STICKS, 3, 1F);
                    });
                }

                if (timer.get() <= 5 && timer.get() != 0) {
                    String message = ChatColor.translateAlternateColorCodes('&', "&eThe game starts in &c" + timer.get() + "&e seconds!");
                    totalPlayers.forEach((forEachPlayer) -> {
                        forEachPlayer.sendMessage(message);
                        forEachPlayer.playSound(forEachPlayer.getLocation(), Sound.NOTE_STICKS, 3, 1F);
                    });
                }

                if (timer.get() == 0) {
                    hasReachedZero.set(true);

                    players.forEach((forEachPlayer, tagState) -> {
                        forEachPlayer.teleport(spawnLocation);
                        forEachPlayer.closeInventory();
                        forEachPlayer.getInventory().clear();
                        ItemStack is;
                        if (MatchManager.hasPlayerMatch(forEachPlayer)) {
                            Match playerMatch = MatchManager.getPlayerMatch(forEachPlayer);
                            is = playerMatch.getPlayerHat(forEachPlayer).item();
                        } else {
                            is = new ItemStack(Material.AIR, 1);
                        }
                        forEachPlayer.getInventory().setHelmet(is);
                        forEachPlayer.setFireTicks(0);
                    });

                    startRound();

                    compassUpdaterTask.set(Bukkit.getScheduler().scheduleSyncRepeatingTask(ReviversTwo.getPlugin(), () -> {
                        for (Player loopPlayer : players.keySet()) { // Loop all players
                            if (players.get(loopPlayer)) { // Select only the IT ones
                                if (loopPlayer.getInventory().getItem(1).getType().equals(Material.COMPASS)) { // Check if they have a compass in their inv
                                    // Initialise the variables
                                    double closestDistance = Double.MAX_VALUE;
                                    Player closestPlayer = null;
                                    for (Player playerInWorld : world.getPlayers()) { // Loop all players in the same world
                                        double dist = playerInWorld.getLocation().distance(loopPlayer.getLocation()); // Calculate the distance
                                        if (dist < closestDistance) {
                                            if (PlayerHandler.getPlayerGame(playerInWorld) != null) {
                                                if (PlayerHandler.getPlayerGame(playerInWorld).equals(this)) {
                                                    if (!PlayerHandler.getPlayerGame(playerInWorld).getSpectators().contains(playerInWorld)) {
                                                        if (!players.get(playerInWorld)) {
                                                            closestDistance = dist;
                                                            closestPlayer = playerInWorld;
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    if (closestPlayer != null) {
                                        loopPlayer.setCompassTarget(closestPlayer.getLocation());
                                    }
                                }
                            }
                        }
                    }, 0, 1));

                    Bukkit.getScheduler().cancelTask(taskId.get());
                }

            }
        }, 0, 20));
    }

    private boolean white = true;
    private void updateScoreboards() {
        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        List<Player> totalPlayers = new ArrayList<>(players.keySet());
        totalPlayers.addAll(spectators);
        if (round == 0) {
            totalPlayers.forEach((player) ->
            {

                if (!boards.containsKey(player)) {
                    boards.put(player, new FastBoard(player));
                    boards.get(player).updateTitle(ChatColor.translateAlternateColorCodes('&', "&e&lTNT TAG"));
                }

                if (players.size() < minPlayers) {
                    boards.get(player).updateLines(
                            ChatColor.translateAlternateColorCodes('&', "&7" + dateFormat.format(Date.from(Instant.now())) + " &8" + id),
                            ChatColor.translateAlternateColorCodes('&', ""),
                            ChatColor.translateAlternateColorCodes('&', "&fMap: &a" + arena.getName()),
                            ChatColor.translateAlternateColorCodes('&', "&fPlayers: &a" + players.size() + "/" + maxPlayers),
                            ChatColor.translateAlternateColorCodes('&', ""),
                            ChatColor.translateAlternateColorCodes('&', "&fStarting in &a" + timer.get() + "s&f if"),
                            ChatColor.translateAlternateColorCodes('&', "&a" + (minPlayers - players.size()) + "&f more players join"),
                            ChatColor.translateAlternateColorCodes('&', ""),
                            ChatColor.translateAlternateColorCodes('&', "&fGame: &aTNT Tag"),
                            ChatColor.translateAlternateColorCodes('&', ""),
                            ChatColor.translateAlternateColorCodes('&', "&e" + ReviversTwo.getConfiguration().getString("Server IP"))
                    );
                } else {
                    boards.get(player).updateLines(
                            ChatColor.translateAlternateColorCodes('&', "&7" + dateFormat.format(Date.from(Instant.now())) + " &8" + id),
                            ChatColor.translateAlternateColorCodes('&', ""),
                            ChatColor.translateAlternateColorCodes('&', "&fMap: &a" + arena.getName()),
                            ChatColor.translateAlternateColorCodes('&', "&fPlayers: &a" + players.size() + "/" + maxPlayers),
                            ChatColor.translateAlternateColorCodes('&', ""),
                            ChatColor.translateAlternateColorCodes('&', "&fStarting in &a" + timer.get() + "s&f to"),
                            ChatColor.translateAlternateColorCodes('&', "&fallow time for"),
                            ChatColor.translateAlternateColorCodes('&', "&fadditional players"),
                            ChatColor.translateAlternateColorCodes('&', ""),
                            ChatColor.translateAlternateColorCodes('&', "&fGame: &aTNT Tag"),
                            ChatColor.translateAlternateColorCodes('&', ""),
                            ChatColor.translateAlternateColorCodes('&', "&e" + ReviversTwo.getConfiguration().getString("Server IP"))
                    );
                }
            });
        } else {

            white = !white;

            players.forEach((player, tagState) -> {

                if (!boards.containsKey(player)) {
                    boards.put(player, new FastBoard(player));
                    boards.get(player).updateTitle(ChatColor.translateAlternateColorCodes('&', "&e&lTNT TAG"));
                }

                boards.get(player).updateLines(
                        ChatColor.translateAlternateColorCodes('&', "&7Round #" + round),
                        ChatColor.translateAlternateColorCodes('&', ""),
                        ChatColor.translateAlternateColorCodes('&', "&eExplosion in " + ((timer.get() <= 15) ? (timer.get() <= 5) ? "&c" + timer.get() + "s" : "&6" + timer.get() + "s" : "&a" + timer.get() + "s")),
                        ChatColor.translateAlternateColorCodes('&', ""),
                        ChatColor.translateAlternateColorCodes('&', "&fGoal: " + ((tagState) ? "&cTag someone!" : "&aRun away!")),
                        ChatColor.translateAlternateColorCodes('&', ""),
                        ChatColor.translateAlternateColorCodes('&', "&fAlive: &a" + players.size() + " Players"),
                        ChatColor.translateAlternateColorCodes('&', ""),
                        ChatColor.translateAlternateColorCodes('&', "&7" + dateFormat.format(Date.from(Instant.now())) + " &8" + id),
                        ChatColor.translateAlternateColorCodes('&', "&e" + ReviversTwo.getConfiguration().getString("Server IP"))
                );

                ActionBar actionBar = new ActionBar();
                if (tagState) {
                    String color = (white) ? "&f" : "&c";
                    actionBar.sendActionBar(player, ChatColor.translateAlternateColorCodes('&', color + "You're IT, tag someone!"));
                } else {
                    actionBar.sendActionBar(player, ChatColor.translateAlternateColorCodes('&', "&aRun away!"));
                }

            });
            spectators.forEach((player) -> {

                if (!boards.containsKey(player)) {
                    boards.put(player, new FastBoard(player));
                    boards.get(player).updateTitle(ChatColor.translateAlternateColorCodes('&', "&e&lTNT TAG"));
                }

                boards.get(player).updateLines(
                        ChatColor.translateAlternateColorCodes('&', "&7Round #" + round),
                        ChatColor.translateAlternateColorCodes('&', ""),
                        ChatColor.translateAlternateColorCodes('&', "&eExplosion in " + ((timer.get() <= 15) ? (timer.get() <= 5) ? "&c" + timer.get() + "s" : "&6" + timer.get() + "s" : "&a" + timer.get() + "s")),
                        ChatColor.translateAlternateColorCodes('&', ""),
                        ChatColor.translateAlternateColorCodes('&', "&fAlive: &a" + players.size() + " Players"),
                        ChatColor.translateAlternateColorCodes('&', ""),
                        ChatColor.translateAlternateColorCodes('&', "&7" + dateFormat.format(Date.from(Instant.now())) + " &8" + id),
                        ChatColor.translateAlternateColorCodes('&', "&e" + ReviversTwo.getConfiguration().getString("Server IP"))
                );

            });
        }
    }

    public Boolean isIt(Player player) {
        if (!players.containsKey(player)) return null;
        return players.get(player);
    }

    public ArrayList<Player> getSpectators() {
        return spectators;
    }

    public String getID() {
        return id;
    }

    public int getMinPlayers() {
        return minPlayers;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public LinkedHashMap<Player, Boolean> getPlayers() {
        return players;
    }

    public int getRound() {
        return round;
    }

}
