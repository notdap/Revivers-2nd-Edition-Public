package net.revivers.reviverstwo.arenas.games.hats;

import net.revivers.reviverstwo.ReviversTwo;
import org.apache.commons.io.FileUtils;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;

public class HatManager {

    private static LinkedHashMap<String, Hat> hats = new LinkedHashMap<>();

    public static void loadHats() {
        File file = new File(ReviversTwo.getPlugin().getDataFolder() + "/hats");
        boolean wereFoldersCreated = file.mkdir();
        if (wereFoldersCreated) {
            try {
                File defaultHat = new File(ReviversTwo.getPlugin().getDataFolder() + "/hats/default.yml");
                String defaultHatContent =
                        """
                                Name: '&7No Hat'
                                Lore:
                                 - '&7Default hat.'
                                Material: air""";
                FileUtils.writeStringToFile(defaultHat, defaultHatContent, StandardCharsets.UTF_8);
            } catch (Exception ignored) {
                ReviversTwo.getPlugin().getLogger().warning("Could not create default hat");
            }
        }
        for (File child : file.listFiles()) {
            FileConfiguration data = YamlConfiguration.loadConfiguration(child);

            String hatId = child.getName().replaceFirst("[.][^.]+$", "");

            Hat hat = new Hat(hatId);
            hat.name(data.getString("Name"));
            hat.lore(data.getStringList("Lore"));
            if (data.get("Data") != null)
                hat.data(data.getInt("Data"));
            if (data.get("Permission") != null)
                hat.permission(data.getString("Permission"));
            hat.material(Material.valueOf(data.getString("Material").toUpperCase()));
            hat.enchanted(data.getBoolean("Enchanted"));

            hats.put(hatId, hat);

            ReviversTwo.getPlugin().getLogger().info("[Dynamite] Loaded hat \"" + data.getString("Name") + "\" (" + child.getName() + ")");
        }

        Hat defaultHat = hats.get(ReviversTwo.getConfiguration().getString("Default Hat"));
        hats.remove(defaultHat.id());

        LinkedHashMap<String, Hat> reorder = new LinkedHashMap<>();
        reorder.put(defaultHat.id(), defaultHat);
        reorder.putAll(hats);

        hats = reorder;

    }

    public static LinkedHashMap<String, Hat> getHats() {
        return hats;
    }

}
