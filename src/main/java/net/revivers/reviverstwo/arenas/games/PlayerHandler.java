package net.revivers.reviverstwo.arenas.games;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class PlayerHandler {

	private static final Map<Player, Game> playerGameManager = new HashMap<>();
	
	public static void setPlayerGame(Player player, Game game) throws IllegalArgumentException {
		if (playerGameManager.containsKey(player)) throw new IllegalArgumentException("Player is already in game");
		playerGameManager.put(player, game);
	}
	
	public static Game getPlayerGame(Player player) {
		return playerGameManager.get(player);
	}
	
	public static void removePlayerGame(Player player) {
		if (!playerGameManager.containsKey(player)) throw new IllegalArgumentException("Player is not in game");
		playerGameManager.remove(player);
	}

	public static boolean hasPlayerGame(Player player) {
		return playerGameManager.containsKey(player);
	}
	
}
