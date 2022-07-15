package net.revivers.reviverstwo.events;

import dev.cobblesword.nachospigot.knockback.KnockbackConfig;
import dev.cobblesword.nachospigot.knockback.KnockbackProfile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class EventListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        KnockbackProfile profile = KnockbackConfig.getKbProfileByName("Hypixel");
        event.getPlayer().setKnockbackProfile(profile);
    }

}
