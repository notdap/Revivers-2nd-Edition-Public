package net.revivers.reviverstwo.arenas;

import net.revivers.reviverstwo.ReviversTwo;
import org.apache.commons.io.FileUtils;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;

import static org.bukkit.Bukkit.getWorld;

public class ArenaManager
{
	
	private static final Hashtable<String, Arena> arenaList = new Hashtable<>();

	public static void loadArenas() {
		File file = new File(ReviversTwo.getPlugin().getDataFolder() + "/maps");
		boolean wereFoldersCreated = file.mkdir();
		if (wereFoldersCreated) {
			try {
				File defaultMap = new File(ReviversTwo.getPlugin().getDataFolder() + "/maps/example.yml");
				String defaultHatContent =
						"""
								Name: 'Example'
								World: example
								Lobby:
								  X coordinate: 0.0
								  Y coordinate: 0.0
								  Z coordinate: 0.0
								  Yaw: 0.0
								  Pitch: 0.0
								Spawn:
								  X coordinate: 0.0
								  Y coordinate: 0.0
								  Z coordinate: 0.0
								  Yaw: 0.0
								  Pitch: 0.0""";
				FileUtils.writeStringToFile(defaultMap, defaultHatContent, StandardCharsets.UTF_8);
			} catch (Exception ignored) {
				ReviversTwo.getPlugin().getLogger().warning("Could not create default map file.");
			}
		}

		for (File child : file.listFiles()) {
			FileConfiguration data = YamlConfiguration.loadConfiguration(child);

			String arenaId = child.getName().replaceFirst("[.][^.]+$", "");

			Location spawn = new Location(getWorld(data.getString("World")), 0, 0, 0);
			spawn.setWorld(getWorld(data.getString("World")));
			spawn.setX(data.getDouble("Spawn.X coordinate"));
			spawn.setY(data.getDouble("Spawn.Y coordinate"));
			spawn.setZ(data.getDouble("Spawn.Z coordinate"));
			spawn.setYaw((float) data.getDouble("Spawn.Yaw"));
			spawn.setPitch((float) data.getDouble("Spawn.Pitch"));

			Location lobby = new Location(getWorld(data.getString("World")), 0, 0, 0);
			lobby.setX(data.getDouble("Lobby.X coordinate"));
			lobby.setY(data.getDouble("Lobby.Y coordinate"));
			lobby.setZ(data.getDouble("Lobby.Z coordinate"));
			lobby.setYaw((float) data.getDouble("Lobby.Yaw"));
			lobby.setPitch((float) data.getDouble("Lobby.Pitch"));

			ArenaManager.registerArena(
					arenaId,
					data.getString("Name"),
					getWorld(data.getString("World")),
					spawn,
					lobby
			);

			ReviversTwo.getPlugin().getLogger().info("[Dynamite] Loaded arena \"" + data.getString("Name") + "\" (" + child.getName() + ")");
		}
	}

	public static void registerArena(String id, String name, World world, Location spawnLocation, Location lobbyLocation) {
		arenaList.put(id, new Arena(name, world, spawnLocation, lobbyLocation));
	}

	public static List<Arena> getArenas() {
		return new ArrayList<>(arenaList.values());
	}

	public static Arena getRandomArena(Arena avoid) {
		Random rand = new Random();
		List<Arena> arenas = new ArrayList<>(arenaList.values());
		arenas.remove(avoid);
		return arenas.get(rand.nextInt(arenas.size()));
	}

	public static Arena getRandomArena() {
		Random rand = new Random();
		return new ArrayList<>(arenaList.values()).get(rand.nextInt(arenaList.size()));
	}
	
}
