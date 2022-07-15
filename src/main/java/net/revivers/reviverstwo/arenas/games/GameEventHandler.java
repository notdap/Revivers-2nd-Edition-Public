package net.revivers.reviverstwo.arenas.games;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import net.kyori.adventure.text.Component;
import net.revivers.reviverstwo.ReviversTwo;
import net.revivers.reviverstwo.matches.Match;
import net.revivers.reviverstwo.matches.MatchManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class GameEventHandler implements Listener {

	private final Map<Player, Integer> pendingLeave = new HashMap<>();

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {

		// Check if action is right click
		if (!event.getAction().equals(Action.RIGHT_CLICK_AIR) && !event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;

		// Check if the player is in a game
		if (PlayerHandler.getPlayerGame(event.getPlayer()) == null) return;

		Player player = event.getPlayer();
		Game game = PlayerHandler.getPlayerGame(player);

		// Check if player is either in Lobby or is Spectator
		if (!game.getSpectators().contains(player) && game.getRound() != 0) return;

		// Check if right-clicked item was the leave bed.
		if (player.getItemInHand().getType() == Material.BED) {
			ItemStack item = player.getItemInHand();
			if (item.getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', "&c&lReturn to Lobby &7(Right Click)"))) {

				if (pendingLeave.containsKey(player)) {

					player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&lTeleport cancelled!"));
					Bukkit.getScheduler().cancelTask(pendingLeave.get(player));
					pendingLeave.remove(player);

				} else {
					player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a&lTeleporting you to the lobby in 3 seconds... Right-click again to cancel the teleport!"));

					int taskId = Bukkit.getScheduler().scheduleSyncDelayedTask(ReviversTwo.getPlugin(), () -> {
						game.leave(player, false);
						pendingLeave.remove(player);
					}, 3 * 20);

					pendingLeave.put(player, taskId);
				}

			}
		} else if (player.getItemInHand().getType() == Material.COMPASS) {
			ItemStack item = player.getItemInHand();
			if (item.getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', "&a&lTeleporter &7(Right Click)"))) {

				AtomicInteger oldPlayerSize = new AtomicInteger(Integer.MAX_VALUE);
				AtomicInteger taskId = new AtomicInteger();
				AtomicBoolean hasBeenOpened = new AtomicBoolean(false);
				AtomicReference<PaginatedGui> gui = new AtomicReference<>(Gui.paginated()
						.disableAllInteractions()
						.title(Component.text("Teleporter"))
						.pageSize(26)
						.rows(4)
						.create());

				gui.get().setItem(29, ItemBuilder.from(Material.ARROW).name(Component.text(ChatColor.translateAlternateColorCodes('&', "&aPrevious"))).asGuiItem(guiEvent -> gui.get().previous()));
				gui.get().setItem(33, ItemBuilder.from(Material.ARROW).name(Component.text(ChatColor.translateAlternateColorCodes('&', "&aNext"))).asGuiItem(guiEvent -> gui.get().next()));

				taskId.set(Bukkit.getScheduler().scheduleSyncRepeatingTask(ReviversTwo.getPlugin(), () -> {

					if (PlayerHandler.getPlayerGame(player) == null || PlayerHandler.getPlayerGame(player) != game) {
						gui.get().close(player);
						Bukkit.getScheduler().cancelTask(taskId.get());
						return;
					}

					if (game.getPlayers().size() != oldPlayerSize.get()) {

						oldPlayerSize.set(game.getPlayers().size());

						if (!hasBeenOpened.get()) {
							gui.get().open(player);
							hasBeenOpened.set(true);
						} else {
							gui.get().clearPageItems();
							gui.get().setItem(29, ItemBuilder.from(Material.ARROW).name(Component.text(ChatColor.translateAlternateColorCodes('&', "&aPrevious"))).asGuiItem(guiEvent -> gui.get().previous()));
							gui.get().setItem(33, ItemBuilder.from(Material.ARROW).name(Component.text(ChatColor.translateAlternateColorCodes('&', "&aNext"))).asGuiItem(guiEvent -> gui.get().next()));
						}

						for (Player gamePlayer : game.getPlayers().keySet()) {
							ItemStack gamePlayerSkull = new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);
							SkullMeta meta = (SkullMeta) gamePlayerSkull.getItemMeta();
							meta.setOwner(gamePlayer.getName());
							meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&a" + gamePlayer.getName()));
							ArrayList<String> gamePlayerSkullLore = new ArrayList<>();
							gamePlayerSkullLore.add(ChatColor.translateAlternateColorCodes('&', "&7Click this item to"));
							gamePlayerSkullLore.add(ChatColor.translateAlternateColorCodes('&', "&7teleport to the player."));
							meta.setLore(gamePlayerSkullLore);
							gamePlayerSkull.setItemMeta(meta);

							GuiItem guiItem = ItemBuilder.from(gamePlayerSkull).asGuiItem(guiEvent -> {
								gui.get().close(player);
								player.teleport(gamePlayer);
							});

							gui.get().addItem(guiItem);
						}

						gui.get().update();
					}

				}, 0, 15));

			}

		}  else if (player.getItemInHand().getType() == Material.PAPER) {
			ItemStack item = player.getItemInHand();
			if (item.getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', "&b&lPlay Again &7(Right Click)"))) {
				player.performCommand("m start");
			}
		}

	}

	@EventHandler
	public void onPlayerDamage(EntityDamageByEntityEvent event) {

		// Check if entities are players
		if (!(event.getDamager() instanceof Player) || !(event.getEntity() instanceof Player)) return;

		// Check if the players are in a game
		if (PlayerHandler.getPlayerGame((Player) event.getDamager()) == null || PlayerHandler.getPlayerGame((Player) event.getEntity()) == null) return;

		// Check if players are in the same game
		if (PlayerHandler.getPlayerGame((Player) event.getDamager()) != PlayerHandler.getPlayerGame((Player) event.getEntity())) return;

		Game game = PlayerHandler.getPlayerGame((Player) event.getEntity());

		// Check if player is spectator
		if (game.getSpectators().contains((Player) event.getEntity()) || game.getSpectators().contains((Player) event.getDamager())) {
			event.setCancelled(true);
			return;
		}

		// Check if game has started
		if (game.getRound() == 0) event.setCancelled(true);

		event.setDamage(0);
		game.tag((Player) event.getDamager(), (Player) event.getEntity());

	}

	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {

		// Check if entity is a player
		if (!(event.getEntity() instanceof Player)) return;

		// Check if the players are in a game
		if (PlayerHandler.getPlayerGame((Player) event.getEntity()) != null) {
			Game game = PlayerHandler.getPlayerGame((Player) event.getEntity());
			if (game.getRound() == 0 || game.getSpectators().contains((Player) event.getEntity()))
				event.setCancelled(true);
		}

		// Cancel if it's fall damage
		if (event.getCause() == EntityDamageEvent.DamageCause.FALL)
			event.setCancelled(true);

		event.setDamage(0);

	}

	@EventHandler
	public void onFoodLevelChange(FoodLevelChangeEvent event) {
		event.setFoodLevel(20);
		event.setCancelled(true);
	}

	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent event) {

		// Check if the player is in a game
		if (PlayerHandler.getPlayerGame(event.getPlayer()) == null) return;

		event.setCancelled(true);

	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {

		// Check if the player is in a game
		if (PlayerHandler.getPlayerGame((Player) event.getWhoClicked()) == null) return;

		event.setCancelled(true);

	}

	@EventHandler
	public void onInventoryDrag(InventoryDragEvent event) {

		// Check if the player is in a game
		if (PlayerHandler.getPlayerGame((Player) event.getWhoClicked()) == null) return;

		event.setCancelled(true);

	}

	@EventHandler
	public void onInventoryOpen(InventoryOpenEvent event) {

		// Check if the player is in a game
		if (PlayerHandler.getPlayerGame((Player) event.getPlayer()) != null) {
			Game game = PlayerHandler.getPlayerGame((Player) event.getPlayer());
			if (game.getRound() == 0) return;
			if (game.getSpectators().contains((Player) event.getPlayer())) return;
		} else {
			return;
		}

		event.setCancelled(true);

	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {

		// Check if the player is in a match
		if (MatchManager.hasPlayerMatch(event.getPlayer())) {
			Match match = MatchManager.getPlayerMatch(event.getPlayer());
			MatchManager.removePlayerMatch(event.getPlayer());

			for (Player loopPlayer : match.getPlayers()) {
				loopPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
				loopPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', ReviversTwo.getChat().getPlayerPrefix(event.getPlayer()) + event.getPlayer().getName() + "&e has been kicked from the match since they left."));
				loopPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
			}
		}
		MatchManager.removePlayerMatchInvite(event.getPlayer());

		// Check if the player is in a game
		if (PlayerHandler.getPlayerGame(event.getPlayer()) == null) return;

		Game game = PlayerHandler.getPlayerGame(event.getPlayer());

		game.leave(event.getPlayer(), false);

	}

	@EventHandler
	public void onPlayerKick(PlayerKickEvent event) {

		// Check if the player is in a match
		if (MatchManager.hasPlayerMatch(event.getPlayer())) {
			Match match = MatchManager.getPlayerMatch(event.getPlayer());
			MatchManager.removePlayerMatch(event.getPlayer());

			for (Player loopPlayer : match.getPlayers()) {
				loopPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
				loopPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', ReviversTwo.getChat().getPlayerPrefix(event.getPlayer()) + event.getPlayer().getName() + "&e has been kicked from the match since they left."));
				loopPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
			}
		}
		MatchManager.removePlayerMatchInvite(event.getPlayer());

		// Check if the player is in a game
		if (PlayerHandler.getPlayerGame(event.getPlayer()) == null) return;

		Game game = PlayerHandler.getPlayerGame(event.getPlayer());

		game.leave(event.getPlayer(), false);

	}

}
