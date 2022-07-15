package net.revivers.reviverstwo.arenas.games.commands;

import dev.triumphteam.cmd.core.BaseCommand;
import dev.triumphteam.cmd.core.annotation.Command;
import dev.triumphteam.cmd.core.annotation.Default;
import net.revivers.reviverstwo.arenas.games.PlayerHandler;
import net.revivers.reviverstwo.arenas.games.Game;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@Command(value = "leave", alias = "l")
public class LeaveCommand extends BaseCommand {

    @Default
    public void defaultCommand(CommandSender sender) {

        if (!(sender instanceof Player player)) return;

        // Check if the player is in a game
        if (PlayerHandler.getPlayerGame(player) == null) return;

        Game game = PlayerHandler.getPlayerGame(player);
        game.leave(player, false);

    }

}
