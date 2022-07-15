package net.revivers.reviverstwo.arenas.games.commands;

import dev.triumphteam.cmd.bukkit.annotation.Permission;
import dev.triumphteam.cmd.core.BaseCommand;
import dev.triumphteam.cmd.core.annotation.*;
import net.revivers.reviverstwo.arenas.games.Game;
import net.revivers.reviverstwo.arenas.games.PlayerHandler;
import net.revivers.reviverstwo.matches.Match;
import net.revivers.reviverstwo.matches.MatchManager;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@Command(value = "debug", alias = {"d", "debugtools", "dt", "dtools"})

public class DebugCommand extends BaseCommand {

    @Default
    @Permission("revivers.debug")
    public void defaultCommand(CommandSender sender) {

        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2&l&m-----------------------------"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7- &a/debug tag (Player)"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7- &a/debug untag (Player)"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7- &a/debug give (To) [(From)]"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7- &a/debug kill (Player)"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2&l&m-----------------------------"));

    }

    @SubCommand(value = "tag", alias = "t")
    public void tagSubCommand(CommandSender sender, Player toPlayer) {
        if (!(sender instanceof Player playerSender)) return;

        if (!MatchManager.hasPlayerMatch(playerSender)) {
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2&l&m-----------------------------"));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8You aren't in a match."));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2&l&m-----------------------------"));
            return;
        }

        Match match = MatchManager.getPlayerMatch(playerSender);
        if (match.getLeader() != playerSender) {
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2&l&m-----------------------------"));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8Only the leader of the match can use the debug tools."));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2&l&m-----------------------------"));
            return;
        }

        if (toPlayer == null) {
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2&l&m-----------------------------"));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8Unknown player. Please, enter a valid player username."));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2&l&m-----------------------------"));
            return;
        }

        if (!match.getPlayers().contains(toPlayer)) {
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2&l&m-----------------------------"));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8The target must be in your match."));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2&l&m-----------------------------"));
            return;
        }

        if (!PlayerHandler.hasPlayerGame(toPlayer)) {
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2&l&m-----------------------------"));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8The target must be in a game."));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2&l&m-----------------------------"));
            return;
        }

        Game senderGame = PlayerHandler.getPlayerGame(playerSender);
        Game toPlayerGame = PlayerHandler.getPlayerGame(toPlayer);
        if (senderGame != toPlayerGame) {
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2&l&m-----------------------------"));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8You need to be in the same game as your target."));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2&l&m-----------------------------"));
            return;
        }

        if (senderGame.getRound() == 0) {
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2&l&m-----------------------------"));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8Please, wait for the game to start."));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2&l&m-----------------------------"));
            return;
        }

        if (senderGame.getSpectators().contains(toPlayer)) {
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2&l&m-----------------------------"));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8&l" + toPlayer.getDisplayName() + "&8 is an spectator."));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2&l&m-----------------------------"));
            return;
        }

        if (senderGame.isIt(toPlayer)) {
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2&l&m-----------------------------"));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7Error. &7&l" + toPlayer.getDisplayName() + "&7 is already IT."));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2&l&m-----------------------------"));
            return;
        }

        playerSender.playSound(playerSender.getLocation(), Sound.LEVEL_UP, 100, 2);
        senderGame.tag(toPlayer);

    }

    @SubCommand(value = "untag", alias = {"u", "ut"})
    public void untagSubCommand(CommandSender sender, Player toPlayer) {
        if (!(sender instanceof Player playerSender)) return;

        if (!MatchManager.hasPlayerMatch(playerSender)) {
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2&l&m-----------------------------"));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8You aren't in a match."));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2&l&m-----------------------------"));
            return;
        }

        Match match = MatchManager.getPlayerMatch(playerSender);
        if (match.getLeader() != playerSender) {
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2&l&m-----------------------------"));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8Only the leader of the match can use the debug tools."));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2&l&m-----------------------------"));
            return;
        }

        if (toPlayer == null) {
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2&l&m-----------------------------"));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8Unknown player. Please, enter a valid player username."));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2&l&m-----------------------------"));
            return;
        }

        if (!match.getPlayers().contains(toPlayer)) {
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2&l&m-----------------------------"));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8The target must be in your match."));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2&l&m-----------------------------"));
            return;
        }

        if (!PlayerHandler.hasPlayerGame(toPlayer)) {
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2&l&m-----------------------------"));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8The target must be in a game."));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2&l&m-----------------------------"));
            return;
        }

        Game senderGame = PlayerHandler.getPlayerGame(playerSender);
        Game toPlayerGame = PlayerHandler.getPlayerGame(toPlayer);
        if (senderGame != toPlayerGame) {
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2&l&m-----------------------------"));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8You need to be in the same game as your target."));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2&l&m-----------------------------"));
            return;
        }

        if (senderGame.getRound() == 0) {
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2&l&m-----------------------------"));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8Please, wait for the game to start."));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2&l&m-----------------------------"));
            return;
        }

        if (senderGame.getSpectators().contains(toPlayer)) {
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2&l&m-----------------------------"));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8&l" + toPlayer.getDisplayName() + "&8 is an spectator."));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2&l&m-----------------------------"));
            return;
        }

        if (!senderGame.isIt(toPlayer)) {
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2&l&m-----------------------------"));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7Error. &7&l" + toPlayer.getDisplayName() + "&7 is not IT."));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2&l&m-----------------------------"));
            return;
        }

        playerSender.playSound(playerSender.getLocation(), Sound.LEVEL_UP, 100, 2);
        senderGame.untag(toPlayer);

    }

    @SubCommand(value = "give", alias = {"g", "transfer", "tr"})
    public void giveSubCommand(CommandSender sender, Player toPlayer, @Optional Player fromPlayer) {
        if (!(sender instanceof Player playerSender)) return;

        if (!MatchManager.hasPlayerMatch(playerSender)) {
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2&l&m-----------------------------"));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8You aren't in a match."));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2&l&m-----------------------------"));
            return;
        }

        Match match = MatchManager.getPlayerMatch(playerSender);
        if (match.getLeader() != playerSender) {
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2&l&m-----------------------------"));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8Only the leader of the match can use the debug tools."));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2&l&m-----------------------------"));
            return;
        }

        if (fromPlayer == null)
            fromPlayer = playerSender;

        if (toPlayer == null) {
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2&l&m-----------------------------"));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8Unknown player. Please, enter a valid player username."));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2&l&m-----------------------------"));
            return;
        }

        if (!match.getPlayers().contains(fromPlayer) || !match.getPlayers().contains(toPlayer)) {
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2&l&m-----------------------------"));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8All of the players need to be in your same match."));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2&l&m-----------------------------"));
            return;
        }

        if (!PlayerHandler.hasPlayerGame(toPlayer) || !PlayerHandler.hasPlayerGame(fromPlayer)) {
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2&l&m-----------------------------"));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8All of the players need to be in a game."));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2&l&m-----------------------------"));
            return;
        }

        Game senderGame = PlayerHandler.getPlayerGame(playerSender);
        Game fromPlayerGame = PlayerHandler.getPlayerGame(fromPlayer);
        Game toPlayerGame = PlayerHandler.getPlayerGame(toPlayer);
        if (toPlayerGame != fromPlayerGame) {
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2&l&m-----------------------------"));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8Both players need to be in the same game."));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2&l&m-----------------------------"));
            return;
        }
        if (senderGame != fromPlayerGame) {
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2&l&m-----------------------------"));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8You need to be in the same game as your targets."));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2&l&m-----------------------------"));
            return;
        }

        if (senderGame.getRound() == 0) {
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2&l&m-----------------------------"));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8Please, wait for the game to start."));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2&l&m-----------------------------"));
            return;
        }

        if (senderGame.getSpectators().contains(fromPlayer)) {
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2&l&m-----------------------------"));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8&l" + fromPlayer.getDisplayName() + "&8 is an spectator."));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2&l&m-----------------------------"));
            return;
        }

        if (senderGame.getSpectators().contains(toPlayer)) {
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2&l&m-----------------------------"));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8&l" + toPlayer.getDisplayName() + "&8 is an spectator."));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2&l&m-----------------------------"));
            return;
        }

        if (!senderGame.isIt(fromPlayer)) {
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2&l&m-----------------------------"));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7Error. &7&l" + fromPlayer.getDisplayName() + "&7 is not IT."));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2&l&m-----------------------------"));
            return;
        }

        if (senderGame.isIt(toPlayer)) {
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2&l&m-----------------------------"));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7Error. &7&l" + fromPlayer.getDisplayName() + "&7 is already IT."));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2&l&m-----------------------------"));
            return;
        }

        playerSender.playSound(playerSender.getLocation(), Sound.LEVEL_UP, 100, 2);
        senderGame.tag(fromPlayer, toPlayer);

    }

    @SubCommand(value = "kill", alias = {"k", "remove", "r"})
    @Suggestion(value = "", strict = true)
    public void killSubCommand(CommandSender sender, Player toPlayer) {
        if (!(sender instanceof Player playerSender)) return;

        if (!MatchManager.hasPlayerMatch(playerSender)) {
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2&l&m-----------------------------"));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8You aren't in a match."));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2&l&m-----------------------------"));
            return;
        }

        Match match = MatchManager.getPlayerMatch(playerSender);
        if (match.getLeader() != playerSender) {
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2&l&m-----------------------------"));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8Only the leader of the match can use the debug tools."));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2&l&m-----------------------------"));
            return;
        }

        if (toPlayer == null) {
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2&l&m-----------------------------"));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8Unknown player. Please, enter a valid player username."));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2&l&m-----------------------------"));
            return;
        }

        if (!match.getPlayers().contains(toPlayer)) {
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2&l&m-----------------------------"));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8The target must be in your match."));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2&l&m-----------------------------"));
            return;
        }

        if (!PlayerHandler.hasPlayerGame(toPlayer)) {
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2&l&m-----------------------------"));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8The target must be in a game."));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2&l&m-----------------------------"));
            return;
        }

        Game senderGame = PlayerHandler.getPlayerGame(playerSender);
        Game toPlayerGame = PlayerHandler.getPlayerGame(toPlayer);
        if (senderGame != toPlayerGame) {
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2&l&m-----------------------------"));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8You need to be in the same game as your target."));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2&l&m-----------------------------"));
            return;
        }

        if (senderGame.getRound() == 0) {
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2&l&m-----------------------------"));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8Please, wait for the game to start."));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2&l&m-----------------------------"));
            return;
        }

        if (senderGame.getSpectators().contains(toPlayer)) {
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2&l&m-----------------------------"));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8&l" + toPlayer.getDisplayName() + "&8 is an spectator."));
            playerSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2&l&m-----------------------------"));
            return;
        }

        playerSender.playSound(playerSender.getLocation(), Sound.LEVEL_UP, 100, 2);
        senderGame.kill(toPlayer);

    }

}
