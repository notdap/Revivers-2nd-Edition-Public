package net.revivers.reviverstwo.credits;

import dev.triumphteam.cmd.core.BaseCommand;
import dev.triumphteam.cmd.core.annotation.Command;
import dev.triumphteam.cmd.core.annotation.Default;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

@Command(value = "revivers", alias = {"reviversplugin", "reviverstwo"})
public class CreditsCommand extends BaseCommand {

    @Default
    public void defaultCommand(CommandSender sender) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8&l&m-----------------------------"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&4&lRevivers: &c2nd Edition &7(PLUGIN)"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7 ╠ &cPlugin made by sdap &7(@dap#7998)"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7 ╚ &cDesigned and made for Revivers &7(revivers.net)"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8&l&m-----------------------------"));
    }

}