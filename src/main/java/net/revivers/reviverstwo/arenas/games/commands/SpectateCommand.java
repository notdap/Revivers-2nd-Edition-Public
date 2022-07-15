package net.revivers.reviverstwo.arenas.games.commands;

import dev.triumphteam.cmd.core.BaseCommand;
import dev.triumphteam.cmd.core.annotation.Command;
import dev.triumphteam.cmd.core.annotation.Default;
import net.revivers.reviverstwo.arenas.games.Game;
import net.revivers.reviverstwo.arenas.games.PlayerHandler;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@Command("spectate")
public class SpectateCommand extends BaseCommand {

    @Default
    public void defaultCommand(CommandSender sender, Player targetPlayer) {
        if (!(sender instanceof Player player)) return;

        if (targetPlayer == null) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cUnknown player."));
            return;
        }

        if (!PlayerHandler.hasPlayerGame(targetPlayer)) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cThe specified player is not in a game."));
            return;
        }

        if (PlayerHandler.hasPlayerGame(player)) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou need to leave the current game you're in before spectating another game."));
            return;
        }

        Game game = PlayerHandler.getPlayerGame(targetPlayer);
        PlayerHandler.setPlayerGame(player, game);
        game.addSpectator(player);

    }

}