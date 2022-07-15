package net.revivers.reviverstwo.matches;

import org.bukkit.entity.Player;

import java.util.HashMap;

public class MatchManager {

    private static final HashMap<Player, Match> playerMatchInvites = new HashMap<>();
    private static final HashMap<Player, Match> playerMatches = new HashMap<>();

    public static void addPlayerMatch(Player player, Match match) {
        playerMatches.put(player, match);
    }

    public static void addPlayerMatchInvite(Player player, Match match) {
        playerMatchInvites.put(player, match);
    }

    public static Match getPlayerMatchInvite(Player player) {
        return playerMatchInvites.get(player);
    }

    public static boolean hasPlayerMatch(Player player) {
        return playerMatches.containsKey(player);
    }

    public static Match getPlayerMatch(Player player) {
        return playerMatches.get(player);
    }

    public static void removePlayerMatch(Player player) {
        if (!hasPlayerMatch(player)) return;

        Match match = playerMatches.get(player);
        playerMatches.remove(player);
        match.removePlayer(player);
    }

    public static boolean hasPlayerMatchInvite(Player player) {
        return playerMatchInvites.containsKey(player);
    }

    public static void removePlayerMatchInvite(Player player) {
        if (!hasPlayerMatchInvite(player)) return;
        playerMatchInvites.remove(player);
    }

}
