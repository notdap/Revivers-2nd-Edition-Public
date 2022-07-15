package net.revivers.reviverstwo.arenas;

import org.bukkit.Location;
import org.bukkit.World;

public class Arena {

	private final World world;
	private final Location spawnLocation;
	private final Location lobbyLocation;

	private final String name;
	
	public Arena(String name, World world, Location spawnLocation, Location lobbyLocation) {
		this.world = world;
		this.spawnLocation = spawnLocation;
		this.lobbyLocation = lobbyLocation;

		this.name = name;
	}

	public World getWorld()
	{
		return world;
	}
	public Location getSpawnLocation() {
		return spawnLocation;
	}
	public Location getLobbyLocation() {
		return lobbyLocation;
	}

	public String getName() {
		return name;
	}
	
}
