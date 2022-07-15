package net.revivers.reviverstwo.matches;

import dev.triumphteam.cmd.bukkit.annotation.Permission;
import dev.triumphteam.cmd.core.BaseCommand;
import dev.triumphteam.cmd.core.annotation.*;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import net.kyori.adventure.text.Component;
import net.revivers.reviverstwo.ReviversTwo;
import net.revivers.reviverstwo.arenas.games.PlayerHandler;
import net.revivers.reviverstwo.arenas.games.hats.Hat;
import net.revivers.reviverstwo.arenas.games.hats.HatManager;
import net.revivers.reviverstwo.arenas.Arena;
import net.revivers.reviverstwo.arenas.ArenaManager;
import net.revivers.reviverstwo.arenas.games.Game;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Command(value = "match", alias = {"m", "party", "p"})
public class MatchCommand extends BaseCommand {

    @Default
    @Permission("revivers.match")
    public void defaultCommand(CommandSender sender) {

        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7- &e/match leave"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7- &e/match message"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7- &e/match list"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7- &e/match hat"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7- &e/match promote (Player)"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7- &e/match invite (Player)"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7- &e/match kick (Player)"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7- &e/match teleport"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7- &e/match disband"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7- &e/match start"));
        if (ReviversTwo.getPermissions().has(sender, "revivers.match.custommap"))
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7- &e/match map"));
        if (ReviversTwo.getPermissions().has(sender, "revivers.match.custombp"))
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7- &e/match bp (0-100)"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));

    }

    @SubCommand(value = "leave", alias = {"exit", "l"})
    public void leaveSubCommand(CommandSender sender) {
        if (!(sender instanceof Player playerSender)) return;

        if (!MatchManager.hasPlayerMatch(playerSender)) {
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou aren't in a match."));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
            return;
        }

        Match match = MatchManager.getPlayerMatch(playerSender);
        match.leave(playerSender);
    }

    @SubCommand(value = "message", alias = {"m", "chat", "c"})
    public void messageSubCommand(CommandSender sender, List<String> args) {
        if (!(sender instanceof Player playerSender)) return;

        if (!MatchManager.hasPlayerMatch(playerSender)) {
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou aren't in a match."));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
            return;
        }

        if (args.isEmpty()) {
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cThe message can't be blank."));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
            return;
        }

        Match match = MatchManager.getPlayerMatch(playerSender);
        StringBuilder message = new StringBuilder();
        for (String arg : args) {
            message.append(arg).append(" ");
        }

        for (Player player : match.getPlayers()) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&lMatch &7≫ " + ReviversTwo.getChat().getPlayerPrefix(playerSender) + playerSender.getName() + "&7: ") + message);
        }
    }

    @SubCommand(value = "list", alias = "ls")
    public void listSubCommand(CommandSender sender) {
        if (!(sender instanceof Player playerSender)) return;

        if (!MatchManager.hasPlayerMatch(playerSender)) {
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou aren't in a match."));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
            return;
        }

        Match match = MatchManager.getPlayerMatch(playerSender);
        Player matchLeader = match.getLeader();

        int loopCount = 0;
        StringBuilder players = new StringBuilder();
        for (Player player : match.getPlayers()) {
            loopCount++;
            if (player == matchLeader) continue;

            if (loopCount == match.getPlayers().size()) {
                players.append(ReviversTwo.getChat().getPlayerPrefix(player)).append(player.getName());
            } else {
                players.append(ReviversTwo.getChat().getPlayerPrefix(player)).append(player.getName()).append("&e, ");
            }
        }

        playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
        playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&eMatch Leader: &r" + ReviversTwo.getChat().getPlayerPrefix(matchLeader) + matchLeader.getName()));
        playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e"));
        playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&ePlayers: &r" + players));
        playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
    }

    @SubCommand(value = "sethat", alias = {"sethat", "pickhat", "hat", "h"})
    public void setHatSubCommand(CommandSender sender) {
        if (!(sender instanceof Player playerSender)) return;

        if (!MatchManager.hasPlayerMatch(playerSender)) {
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou aren't in a match."));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
            return;
        }

        openHatsGui(playerSender);
    }

    @SubCommand(value = "promote", alias = "prom")
    public void promoteSubCommand(CommandSender sender, Player promotedPlayer) {
        if (!(sender instanceof Player playerSender)) return;

        if (!MatchManager.hasPlayerMatch(playerSender)) {
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou aren't in a match."));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
            return;
        }

        Match match = MatchManager.getPlayerMatch(playerSender);
        if (match.getLeader() != playerSender) {
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cOnly the leader of the match can promote players."));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
            return;
        }

        if (promotedPlayer == null) {
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cUnknown player. Please, enter a valid player username."));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
            return;
        }

        if (promotedPlayer == playerSender) {
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou can't promote yourself."));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
            return;
        }

        if (!match.getPlayers().contains(promotedPlayer)) {
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cThe specified player is not in your match."));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
            return;
        }

        match.promote(promotedPlayer);
    }

    @SubCommand(value = "invite", alias = {"add", "i", "inv"})
    public void inviteSubCommand(CommandSender sender, Player invitedPlayer) {
        if (!(sender instanceof Player playerSender)) return;

        if (MatchManager.getPlayerMatch(playerSender) == null)
            MatchManager.addPlayerMatch(playerSender, new Match(playerSender));

        Match match = MatchManager.getPlayerMatch(playerSender);

        if (match.getLeader() != playerSender) {
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cOnly the leader of the match can invite players."));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
            return;
        }

        if (invitedPlayer == null) {
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cUnknown player. Please, enter a valid player username."));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
            return;
        }

        if (MatchManager.hasPlayerMatch(invitedPlayer)) {
            Match invitedPlayerMatch = MatchManager.getPlayerMatch(invitedPlayer);
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
            if (invitedPlayerMatch != match) {
                playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cThe specified player is in another match."));
            } else {
                playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cThe specified player is already in your match."));
            }
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
            return;
        }

        if (MatchManager.hasPlayerMatchInvite(invitedPlayer)) {
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cThe specified player already has a pending match invite."));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
            return;
        }

        if (match.getPlayers().size() >= 8) {
            if (!ReviversTwo.getPermissions().has(playerSender, "revivers.match.big")) {
                playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
                playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou don't have permission to create matches with more than 8 players."));
                playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
                return;
            }
        }

        match.invite(invitedPlayer, playerSender);
    }

    @SubCommand(value = "kick", alias = {"k", "remove", "r"})
    public void kickSubCommand(CommandSender sender, Player kickPlayer) {
        if (!(sender instanceof Player playerSender)) return;

        if (!MatchManager.hasPlayerMatch(playerSender)) {
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou aren't in a match."));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
            return;
        }

        Match match = MatchManager.getPlayerMatch(playerSender);
        if (match.getLeader() != playerSender) {
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cOnly the leader of the match can kick players."));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
            return;
        }

        if (kickPlayer == null) {
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cUnknown player. Please, enter a valid player username."));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
            return;
        }

        if (kickPlayer == playerSender) {
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou can't kick yourself."));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
            return;
        }

        if (!match.getPlayers().contains(kickPlayer)) {
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cThe specified player is not in your match."));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
            return;
        }

        match.kick(kickPlayer);
    }

    @SubCommand(value = "teleport", alias = {"tpall", "tp", "bring", "bringall"})
    public void teleportSubCommand(CommandSender sender) {
        if (!(sender instanceof Player playerSender)) return;

        if (!MatchManager.hasPlayerMatch(playerSender)) {
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou aren't in a match."));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
            return;
        }

        Match match = MatchManager.getPlayerMatch(playerSender);
        if (match.getLeader() != playerSender) {
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cOnly the Match Leader can teleport all players to the game."));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
            return;
        }

        if (!PlayerHandler.hasPlayerGame(playerSender)) {
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou need to be in a game to use this command."));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
            return;
        }
        Game game = PlayerHandler.getPlayerGame(playerSender);

        for (Player player : match.getPlayers()) {
            if (!PlayerHandler.hasPlayerGame(player)) {
                if (game.getRound() == 0)
                    game.setMaxPlayers(game.getMaxPlayers() + 1);
                game.join(player);
                continue;
            }

            if (PlayerHandler.getPlayerGame(player) != game) {
                PlayerHandler.getPlayerGame(player).leave(player, false);
                if (game.getRound() == 0)
                    game.setMaxPlayers(game.getMaxPlayers() + 1);
                game.join(player);
            }
        }
    }

    @SubCommand(value = "disband", alias = {"disb", "db"})
    public void disbandSubCommand(CommandSender sender) {
        if (!(sender instanceof Player playerSender)) return;

        if (!MatchManager.hasPlayerMatch(playerSender)) {
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou aren't in a match."));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
            return;
        }

        Match match = MatchManager.getPlayerMatch(playerSender);
        if (match.getLeader() != playerSender) {
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cThe match can only be disbanded by the Match Leader."));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
            return;
        }

        match.disband();
    }

    @SubCommand(value = "startgame", alias = {"start", "s", "warp", "sg"})
    public void startGameSubCommand(CommandSender sender) {
        if (!(sender instanceof Player playerSender)) return;

        if (!MatchManager.hasPlayerMatch(playerSender)) {
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou aren't in a match."));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
            return;
        }

        Match match = MatchManager.getPlayerMatch(playerSender);
        if (match.getLeader() != playerSender) {
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cOnly the leader of the match can start a game."));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
            return;
        }

        if (match.getPlayers().size() < 2) {
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cThere's not enough players to start the game."));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
            return;
        }

        if (match.getLastGameStartTime() + 15 > Instant.now().getEpochSecond()) {
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou must wait before starting another game."));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
            return;
        }

        match.start();
    }

    @SubCommand(value = "mapselector", alias = {"arena", "maps", "m", "map", "setmap", "selectmap"})
    @Permission("revivers.match.custommap")
    public void mapSelectorSubCommand(CommandSender sender) {
        if (!(sender instanceof Player playerSender)) return;

        if (!MatchManager.hasPlayerMatch(playerSender)) {
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou aren't in a match."));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
            return;
        }

        Match match = MatchManager.getPlayerMatch(playerSender);
        if (match.getLeader() != playerSender) {
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cOnly the leader of the match can select the map."));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
            return;
        }

        ArrayList<Integer> skippedSlots = new ArrayList<>(Arrays.asList(0, 9, 18, 27, 1, 10, 19, 28));

        Gui mapGui = Gui.gui()
                .disableAllInteractions()
                .title(Component.text("Select a map:"))
                .rows(4)
                .create();
        int count = 0;
        for (Arena arena : ArenaManager.getArenas()) {
            count++;
            while (skippedSlots.contains(count)) count++;

            ItemStack item = new ItemStack(Material.PAPER, 1);

            ItemMeta itemMeta = item.getItemMeta();
            itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&a" + arena.getName()));
            ArrayList<String> lore = new ArrayList<>();
            lore.add(ChatColor.translateAlternateColorCodes('&', "&7Click this item to"));
            lore.add(ChatColor.translateAlternateColorCodes('&', "&7select this map."));

            if (match.getSelectedMap() != null) {
                if (match.getSelectedMap().equals(arena)) {
                    itemMeta.addEnchant(Enchantment.SILK_TOUCH, 1, true);
                    itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    lore.add(ChatColor.translateAlternateColorCodes('&', "&7"));
                    lore.add(ChatColor.translateAlternateColorCodes('&', "&a&lSELECTED"));
                }
            }

            itemMeta.setLore(lore);
            item.setItemMeta(itemMeta);

            GuiItem guiItem = ItemBuilder.from(item).asGuiItem(event -> {
                match.setSelectedMap(arena);
                mapGui.close(playerSender);
                for (Player player : match.getPlayers()) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&eThe match map has been set to &a" + arena.getName() + "&e by the Match Leader."));
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
                }
            });

            mapGui.setItem(count, guiItem);
        }

        // Random Maps item
        ItemStack randomMapItem = new ItemStack(Material.BOOK, 1);
        ItemMeta randomMapItemMeta = randomMapItem.getItemMeta();
        randomMapItemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&eUse Random Maps"));
        ArrayList<String> randomMapLore = new ArrayList<>();
        randomMapLore.add(ChatColor.translateAlternateColorCodes('&', "&7Click this item to"));
        randomMapLore.add(ChatColor.translateAlternateColorCodes('&', "&7have maps be random."));
        randomMapItemMeta.setLore(randomMapLore);
        if (match.getSelectedMap() == null) {
            randomMapItemMeta.addEnchant(Enchantment.SILK_TOUCH, 1, true);
            randomMapItemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        randomMapItem.setItemMeta(randomMapItemMeta);
        GuiItem randomMapGuiItem = ItemBuilder.from(randomMapItem).asGuiItem(event -> {
            match.setSelectedMap(null);
            mapGui.close(playerSender);
            for (Player player : match.getPlayers()) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&eThe match map has been set to &a&lRANDOM&e by the Match Leader."));
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
            }
        });
        mapGui.setItem(9, randomMapGuiItem);

        // Close GUI item
        ItemStack closeGuiItem = new ItemStack(Material.BARRIER, 1);
        ItemMeta closeGuiItemMeta = closeGuiItem.getItemMeta();
        closeGuiItemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&cClose"));
        ArrayList<String> closeGuiLore = new ArrayList<>();
        closeGuiLore.add(ChatColor.translateAlternateColorCodes('&', "&7Click this item to"));
        closeGuiLore.add(ChatColor.translateAlternateColorCodes('&', "&7close this menu."));
        closeGuiItemMeta.setLore(closeGuiLore);
        closeGuiItem.setItemMeta(closeGuiItemMeta);
        GuiItem closeGuiGuiItem = ItemBuilder.from(closeGuiItem).asGuiItem(event -> mapGui.close(playerSender));
        mapGui.setItem(18, closeGuiGuiItem);

        // Fill with glass panes
        ItemStack blackStainedGlassPaneItem = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 15);
        ItemMeta blackStainedGlassPaneMeta = blackStainedGlassPaneItem.getItemMeta();
        blackStainedGlassPaneMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&8"));
        blackStainedGlassPaneItem.setItemMeta(blackStainedGlassPaneMeta);
        for (int slot = 1; slot < 29; slot += 9) {
            mapGui.setItem(slot, ItemBuilder.from(blackStainedGlassPaneItem).asGuiItem());
        }

        mapGui.open(playerSender);
    }

    @SubCommand(value = "setblastprotection", alias = {"setbp", "setblast", "setprotection", "bp", "blast", "protection"})
    @Permission("revivers.match.custombp")
    public void setBlastProtectionSubCommand(CommandSender sender, Integer blastProtection) {
        if (!(sender instanceof Player playerSender)) return;

        if (!MatchManager.hasPlayerMatch(playerSender)) {
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou aren't in a match."));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
            return;
        }

        Match match = MatchManager.getPlayerMatch(playerSender);
        if (match.getLeader() != playerSender) {
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cOnly the leader of the match can adjust the blast protection %."));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
            return;
        }

        if (blastProtection == null) {
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cThe provided number is invalid."));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
            return;
        }
        
        if (blastProtection > 100 || blastProtection < 0) {
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cThe provided number is out of bounds. Percentages may only range from 0 to 100."));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
            return;
        }

        match.setBlastProtection(blastProtection);
    }

    @SubCommand(value = "accept", alias = {"acc", "a"})
    public void acceptSubCommand(CommandSender sender) {
        if (!(sender instanceof Player playerSender)) return;

        if (!MatchManager.hasPlayerMatchInvite(playerSender)) {
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou don't have any pending match invite."));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
            return;
        }

        Match match = MatchManager.getPlayerMatchInvite(playerSender);
        MatchManager.removePlayerMatchInvite(playerSender);
        if (match.isDisbanded()) {
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cThe match was disbanded before you joined."));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
            return;
        }

        match.add(playerSender);
    }

    @SubCommand(value = "revivers", alias = {"rvrs", "credits", "creds", "plugin"})
    public void reviversSubCommand(CommandSender sender) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8&l&m-----------------------------"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&4&lRevivers: &c2nd Edition &7(PLUGIN)"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7 ╠ &cPlugin made by sdap &7(@dap#7998)"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7 ╚ &cDesigned and made for Revivers &7(revivers.net)"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8&l&m-----------------------------"));
    }

    private void openHatsGui(Player player) {
        Match match = MatchManager.getPlayerMatch(player);

        Gui gui = Gui.gui()
                .disableAllInteractions()
                .title(Component.text("Hat Selector"))
                .rows(3)
                .create();

        // Switch to default hat
        Hat defaultHat = HatManager.getHats().get(ReviversTwo.getConfiguration().getString("Default Hat"));

        ItemStack defaultHatItem = new ItemStack(Material.BARRIER, 1);
        ItemMeta defaultHatItemMeta = defaultHatItem.getItemMeta();

        defaultHatItemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&7Default Hat &7(" + defaultHat.name() + "&7)"));

        List<String> defaultHatLore = new ArrayList<>();
        defaultHatLore.add(ChatColor.translateAlternateColorCodes('&', "&7"));
        defaultHat.lore().forEach((loreLine) -> defaultHatLore.add(ChatColor.translateAlternateColorCodes('&', loreLine)));
        defaultHatLore.add(ChatColor.translateAlternateColorCodes('&', "&7"));
        if (match.getPlayerHat(player) == defaultHat)
            defaultHatLore.add(ChatColor.translateAlternateColorCodes('&', "&a&lSELECTED"));
        defaultHatItemMeta.setLore(defaultHatLore);

        defaultHatItem.setItemMeta(defaultHatItemMeta);

        GuiItem defaultHatGuiItem = ItemBuilder.from(defaultHatItem).asGuiItem(event -> {
            if (match.getPlayerHat(player) != defaultHat) {
                match.setPlayerHat(player, defaultHat);
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&eYour new hat for this match is: &a" + defaultHat.name() + "&e."));
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
            } else {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&eYou are already using this hat."));
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
            }
            gui.close(player);
        });

        gui.setItem(11, defaultHatGuiItem);

        // Open Default Hats
        ItemStack defaultHatsItem = new ItemStack(Material.GOLD_BLOCK, 1);
        ItemMeta defaultHatsItemMeta = defaultHatsItem.getItemMeta();

        defaultHatsItemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&eBasic Hats"));

        List<String> defaultHatsLore = new ArrayList<>();
        defaultHatsLore.add(ChatColor.translateAlternateColorCodes('&', "&7Click here to show"));
        defaultHatsLore.add(ChatColor.translateAlternateColorCodes('&', "&7the basic hats."));
        defaultHatsItemMeta.setLore(defaultHatsLore);

        defaultHatsItem.setItemMeta(defaultHatsItemMeta);

        GuiItem defaultHatsGuiItem = ItemBuilder.from(defaultHatsItem).asGuiItem(event -> {

            Gui defaultHatsGui = Gui.gui()
                    .disableAllInteractions()
                    .title(Component.text("Select a Hat:"))
                    .rows(3)
                    .create();

            ArrayList<Integer> skippedSlots = new ArrayList<>(Arrays.asList(0, 9, 18, 1, 10, 19));
            int count = 0;

            for (Hat hat : HatManager.getHats().values()) {
                if (hat.permission() != null) continue;
                if (hat.equals(defaultHat)) continue;

                count++;
                while (skippedSlots.contains(count)) count++;

                ItemStack hatItem = hat.item();

                if (match.getPlayerHat(player) == hat) {
                    ItemMeta hatItemMeta = hatItem.getItemMeta();
                    List<String> hatLore = new ArrayList<>(hatItemMeta.getLore());
                    hatLore.add(ChatColor.translateAlternateColorCodes('&', "&a&lSELECTED"));
                    hatItemMeta.setLore(hatLore);
                    hatItem.setItemMeta(hatItemMeta);
                }

                GuiItem guiItem = ItemBuilder.from(hatItem).asGuiItem(hatEvent -> {
                    if (match.getPlayerHat(player) != hat) {
                        match.setPlayerHat(player, hat);
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&eYour new hat for this match is: &a" + hat.name() + "&e."));
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
                    } else {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&eYou are already using this hat."));
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
                    }
                    defaultHatsGui.close(player);
                });

                defaultHatsGui.setItem(count, guiItem);
            }

            // Go back arrow
            ItemStack closeGuiItem = new ItemStack(Material.ARROW, 1);
            ItemMeta closeGuiItemMeta = closeGuiItem.getItemMeta();
            closeGuiItemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&7Go back"));
            ArrayList<String> closeGuiLore = new ArrayList<>();
            closeGuiLore.add(ChatColor.translateAlternateColorCodes('&', "&7Click this item to go back"));
            closeGuiLore.add(ChatColor.translateAlternateColorCodes('&', "&7to the hats main menu."));
            closeGuiItemMeta.setLore(closeGuiLore);
            closeGuiItem.setItemMeta(closeGuiItemMeta);
            GuiItem closeGuiGuiItem = ItemBuilder.from(closeGuiItem).asGuiItem(arrowEvent -> openHatsGui(player));
            defaultHatsGui.setItem(9, closeGuiGuiItem);

            // Fill with glass panes
            ItemStack blackStainedGlassPaneItem = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 15);
            ItemMeta blackStainedGlassPaneMeta = blackStainedGlassPaneItem.getItemMeta();
            blackStainedGlassPaneMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&8"));
            blackStainedGlassPaneItem.setItemMeta(blackStainedGlassPaneMeta);
            for (int slot = 1; slot < 20; slot += 9) {
                defaultHatsGui.setItem(slot, ItemBuilder.from(blackStainedGlassPaneItem).asGuiItem());
            }

            defaultHatsGui.open(player);
        });

        gui.setItem(13, defaultHatsGuiItem);

        // Open Special Hats
        ItemStack specialHatsItem = new ItemStack(Material.DIAMOND_BLOCK, 1);
        ItemMeta specialHatsItemMeta = specialHatsItem.getItemMeta();

        specialHatsItemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&bSpecial Hats"));

        List<String> specialHatsLore = new ArrayList<>();
        specialHatsLore.add(ChatColor.translateAlternateColorCodes('&', "&7Click here to show"));
        specialHatsLore.add(ChatColor.translateAlternateColorCodes('&', "&7the special hats."));
        specialHatsItemMeta.setLore(specialHatsLore);

        specialHatsItem.setItemMeta(specialHatsItemMeta);

        GuiItem specialHatsItemGui = ItemBuilder.from(specialHatsItem).asGuiItem(event -> {

            Gui specialHatsGui = Gui.gui()
                    .disableAllInteractions()
                    .title(Component.text("Select an Special Hat:"))
                    .rows(3)
                    .create();

            ArrayList<Integer> skippedSlots = new ArrayList<>(Arrays.asList(0, 9, 18, 1, 10, 19));
            int count = 0;

            for (Hat hat : HatManager.getHats().values()) {
                if (hat.permission() == null) continue;
                if (hat.equals(defaultHat)) continue;

                if (!ReviversTwo.getPermissions().has(player, "revivers.match.hats." + hat.permission()))
                    continue;

                count++;
                while (skippedSlots.contains(count)) count++;

                ItemStack hatItem = hat.item();

                if (match.getPlayerHat(player) == hat) {
                    ItemMeta hatItemMeta = hatItem.getItemMeta();
                    List<String> hatLore = new ArrayList<>(hatItemMeta.getLore());
                    hatLore.add(ChatColor.translateAlternateColorCodes('&', "&a&lSELECTED"));
                    hatItemMeta.setLore(hatLore);
                    hatItem.setItemMeta(hatItemMeta);
                }

                GuiItem guiItem = ItemBuilder.from(hatItem).asGuiItem(hatEvent -> {
                    if (match.getPlayerHat(player) != hat) {
                        match.setPlayerHat(player, hat);
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&eYour new hat for this match is: &a" + hat.name() + "&e."));
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
                    } else {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&eYou are already using this hat."));
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&m-----------------------------"));
                    }
                    specialHatsGui.close(player);
                });

                specialHatsGui.setItem(count, guiItem);
            }

            // Go back arrow
            ItemStack closeGuiItem = new ItemStack(Material.ARROW, 1);
            ItemMeta closeGuiItemMeta = closeGuiItem.getItemMeta();
            closeGuiItemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&7Go back"));
            ArrayList<String> closeGuiLore = new ArrayList<>();
            closeGuiLore.add(ChatColor.translateAlternateColorCodes('&', "&7Click this item to go back"));
            closeGuiLore.add(ChatColor.translateAlternateColorCodes('&', "&7to the hats main menu."));
            closeGuiItemMeta.setLore(closeGuiLore);
            closeGuiItem.setItemMeta(closeGuiItemMeta);
            GuiItem closeGuiGuiItem = ItemBuilder.from(closeGuiItem).asGuiItem(arrowEvent -> openHatsGui(player));
            specialHatsGui.setItem(9, closeGuiGuiItem);

            // Fill with glass panes
            ItemStack blackStainedGlassPaneItem = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 15);
            ItemMeta blackStainedGlassPaneMeta = blackStainedGlassPaneItem.getItemMeta();
            blackStainedGlassPaneMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&8"));
            blackStainedGlassPaneItem.setItemMeta(blackStainedGlassPaneMeta);
            for (int slot = 1; slot < 20; slot += 9) {
                specialHatsGui.setItem(slot, ItemBuilder.from(blackStainedGlassPaneItem).asGuiItem());
            }

            specialHatsGui.open(player);
        });

        gui.setItem(15, specialHatsItemGui);

        gui.open(player);
    }

}