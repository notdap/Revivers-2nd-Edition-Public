package net.revivers.reviverstwo.arenas.games.worlds;

import net.revivers.reviverstwo.ReviversTwo;
import net.revivers.reviverstwo.arenas.games.Game;
import net.revivers.reviverstwo.arenas.games.GameManager;
import net.revivers.reviverstwo.arenas.Arena;
import net.revivers.reviverstwo.utilities.RandomText;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.HashMap;

public class WorldManager {

    private static final HashMap<Game, World> gameWorlds = new HashMap<>();

    public static void startUp() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(ReviversTwo.getPlugin(), () -> {
            if (Bukkit.getOnlinePlayers().size() <= ReviversTwo.getConfiguration().getLong("World Manager.Max Players") && !gameWorlds.isEmpty()) {
                for (Game game : new ArrayList<>(gameWorlds.keySet())) {
                    if (GameManager.isGameRegistered(game)) continue;
                    if (gameWorlds.get(game).getPlayers().size() > 0) continue;

                    deleteWorld(gameWorlds.get(game));
                    gameWorlds.remove(game);

                    return;
                }
            }
        }, 0, ReviversTwo.getConfiguration().getLong("World Manager.Queue Rate") * 20L);
    }

    public static void cleanUp() {
        for (World world : Bukkit.getWorlds()) {
            if (world.getWorldFolder().getName().startsWith("dynamite_temp_world")) {
                deleteWorld(world);
            }
        }
    }

    public static World cloneWorld(Game game, Arena arena) {
        String randomWorldName = "dynamite_temp_world-" + arena.getWorld().getName() + "-" + new RandomText(12).get();
        World world = WorldUtils.copyWorld(arena.getWorld(), randomWorldName);
        world.setAutoSave(false);
        gameWorlds.put(game, world);
        return world;
    }

    public static void deleteWorld(World world) {
        Bukkit.getServer().unloadWorld(world, false);
        WorldUtils.deleteWorld(world.getWorldFolder());
    }

}
