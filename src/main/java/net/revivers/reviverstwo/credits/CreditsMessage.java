package net.revivers.reviverstwo.credits;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Arrays;
import java.util.List;

public class CreditsMessage  implements Listener {

    private List<String> possibleCombinations = Arrays.asList(
            "running on revivers",
            "revivers plugin version",
            "revivers plugin",
            "supersafereviverskey"
    );

    @EventHandler
    public void onAsyncPlayerChatEvent(AsyncPlayerChatEvent event) {
        for (String possibleCombination : possibleCombinations) {
            if (event.getMessage().toLowerCase().contains(possibleCombination)) {
                event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', "&8&l&m-----------------------------"));
                event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', "&4&lRevivers: &c2nd Edition &7(PLUGIN)"));
                event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', "&7 ╠ &cPlugin made by sdap &7(@dap#7998)"));
                event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', "&7 ╚ &cDesigned and made for Revivers &7(revivers.net)"));
                event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', "&8&l&m-----------------------------"));
            }
        }
    }

}
