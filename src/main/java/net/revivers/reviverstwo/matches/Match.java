package net.revivers.reviverstwo.matches;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.revivers.reviverstwo.ReviversTwo;
import net.revivers.reviverstwo.arenas.games.hats.Hat;
import net.revivers.reviverstwo.arenas.games.hats.HatManager;
import net.revivers.reviverstwo.arenas.Arena;
import net.revivers.reviverstwo.arenas.ArenaManager;
import net.revivers.reviverstwo.arenas.games.Game;
import net.revivers.reviverstwo.arenas.games.GameManager;
import net.revivers.reviverstwo.arenas.games.PlayerHandler;
import net.revivers.reviverstwo.utilities.RandomText;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Match {

    private Player leader;
    private final HashMap<Player, Hat> players = new HashMap<>();
    private boolean isDisbanded;

    private Arena selectedMap = null;

    private Arena lastPlayedArena = null;
    private long lastGameStartTime = 0;

    private int blastProtection = ReviversTwo.getConfiguration().getInt("Blast Protection Percentage");

    public Match(Player leader) {
        this.leader = leader;
        players.put(leader, HatManager.getHats().get(ReviversTwo.getConfiguration().getString("Default Hat")));
    }

    public Player getLeader() {
        return leader;
    }

    public List<Player> getPlayers() {
        return new ArrayList<>(players.keySet());
    }
    public Hat getPlayerHat(Player player) {
        return players.get(player);
    }
    public void setPlayerHat(Player player, Hat hat) {
        players.put(player, hat);
    }

    public void start() {
        Arena arena;
        if (selectedMap == null) {
            if (lastPlayedArena != null) {
                arena = ArenaManager.getRandomArena(getLastPlayedArena());
            } else {
                arena = ArenaManager.getRandomArena();
            }
        } else {
            arena = selectedMap;
        }
        lastPlayedArena = arena;

        String gameId = ((players.size() >= 8) ? "b" : "m") + new RandomText(4).get();
        Game game = new Game(gameId, arena, players.size(), 2, blastProtection);
        GameManager.createGame(game);

        getPlayers().forEach((player) -> {
            if (PlayerHandler.getPlayerGame(player) != null)
                PlayerHandler.getPlayerGame(player).leave(player, true);
        });

        for (Player player : getPlayers()) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e&lGAME SETTINGS:"));
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7 ╠ Map: &6" + arena.getName()));
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7 ╚ Blast Protection: &6" + blastProtection + "%"));
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
        }

        getPlayers().forEach((game::join));

        lastGameStartTime = Instant.now().getEpochSecond();

    }

    public void promote(Player promotedPlayer) {
        for (ItemStack item : leader.getInventory()) {
            if (item != null) {
                if (item.hasItemMeta()) {
                    if (item.getItemMeta().hasDisplayName()) {
                        if (item.getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', "&b&lPlay Again &7(Right Click)"))) {
                            leader.getInventory().remove(item);
                        }
                    }
                }
            }
        }

        leader = promotedPlayer;

        for (Player player : getPlayers()) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', ReviversTwo.getChat().getPlayerPrefix(promotedPlayer) + promotedPlayer.getName() + "&e has been promoted to Match Leader."));
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
        }
    }

    public void add(Player addPlayer) {
        players.put(addPlayer, HatManager.getHats().get(ReviversTwo.getConfiguration().getString("Default Hat")));
        MatchManager.addPlayerMatch(addPlayer, this);

        for (Player player : getPlayers()) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', ReviversTwo.getChat().getPlayerPrefix(addPlayer) + addPlayer.getName() + "&a joined the match."));
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
        }
    }

    public void invite(Player invitedPlayer, Player inviter) {
        for (Player player : getPlayers()) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', ReviversTwo.getChat().getPlayerPrefix(invitedPlayer) + invitedPlayer.getName() + "&e has been invited to the match."));
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&eThey have &c60&e seconds to accept."));
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
        }

        MatchManager.addPlayerMatchInvite(invitedPlayer, this);
        invitedPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
        invitedPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', ReviversTwo.getChat().getPlayerPrefix(inviter) + inviter.getName() + "&e has invited you to join their match!"));
        TextComponent acceptMessage = new TextComponent(ChatColor.translateAlternateColorCodes('&', "&eYou have &c60&e seconds to accept. &aClick here to join!"));
        acceptMessage.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/match accept"));
        invitedPlayer.spigot().sendMessage(acceptMessage);
        invitedPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));

        Bukkit.getScheduler().runTaskLaterAsynchronously(ReviversTwo.getPlugin(), () ->
        {
            if (MatchManager.hasPlayerMatchInvite(invitedPlayer)) {
                invitedPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
                invitedPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', ReviversTwo.getChat().getPlayerPrefix(inviter) + inviter.getName() + "&c's match invite has expired."));
                invitedPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));

                for (Player player : getPlayers()) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', ReviversTwo.getChat().getPlayerPrefix(invitedPlayer) + invitedPlayer.getName() + "&c has not accepted the match invite in time."));
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
                }

                MatchManager.removePlayerMatchInvite(invitedPlayer);

                if (getPlayers().size() == 1) {
                    isDisbanded = true;

                    Player lastPlayerStanding = getPlayers().get(0);

                    MatchManager.removePlayerMatch(lastPlayerStanding);
                    lastPlayerStanding.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
                    lastPlayerStanding.sendMessage(ChatColor.translateAlternateColorCodes('&', "&eThe match has been disbanded since all players left."));
                    lastPlayerStanding.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));

                    MatchManager.removePlayerMatch(lastPlayerStanding);
                }

            }
        }, 60 * 20);
    }

    public void leave(Player leavePlayer) {
        for (Player player : getPlayers()) {
            if (player == leavePlayer) continue;
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', ReviversTwo.getChat().getPlayerPrefix(leavePlayer) + leavePlayer.getName() + "&e left the match."));
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
        }

        removePlayer(leavePlayer);

        leavePlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
        leavePlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', "&eYou've left the match."));
        leavePlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
    }

    public void kick(Player kickPlayer) {
        for (Player player : getPlayers()) {
            if (player == kickPlayer) continue;
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', ReviversTwo.getChat().getPlayerPrefix(kickPlayer) + kickPlayer.getName() + "&e has been kicked from your match."));
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
        }

        removePlayer(kickPlayer);

        kickPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
        kickPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', "&eYou've been kicked from the match."));
        kickPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
    }

    public void removePlayer(Player player) {
        MatchManager.removePlayerMatch(player);
        players.remove(player);

        if (players.size() == 0) {
            disband();
        } else {
            if (players.size() == 1) {
                isDisbanded = true;

                Player lastPlayerStanding = getPlayers().get(0);

                MatchManager.removePlayerMatch(lastPlayerStanding);
                lastPlayerStanding.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
                lastPlayerStanding.sendMessage(ChatColor.translateAlternateColorCodes('&', "&eThe match has been disbanded since all players left."));
                lastPlayerStanding.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));

                MatchManager.removePlayerMatch(lastPlayerStanding);

                return;
            }
            if (leader == player) {
                leader = getPlayers().get(0);

                for (Player loopPlayer : getPlayers()) {
                    loopPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
                    loopPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', ReviversTwo.getChat().getPlayerPrefix(leader) + leader.getName() + "&e has been automatically promoted to Match Leader."));
                    loopPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
                }
            }
        }
    }

    public boolean isDisbanded() {
        return isDisbanded;
    }
    public void disband() {
        isDisbanded = true;

        for (Player player : getPlayers()) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cThe match has been disbanded by the Match Leader."));
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
            MatchManager.removePlayerMatch(player);
        }
    }

    public Arena getSelectedMap() {
        return selectedMap;
    }
    public void setSelectedMap(Arena selectedMap) {
        this.selectedMap = selectedMap;
    }

    public Arena getLastPlayedArena() {
        return lastPlayedArena;
    }

    public long getLastGameStartTime() {
        return lastGameStartTime;
    }

    public void setBlastProtection(int blastProtection) {
        this.blastProtection = blastProtection;

        for (Player player : getPlayers()) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&eThe blast protection % has been set to " + blastProtection + " by the Match Leader."));
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
        }
    }

}
