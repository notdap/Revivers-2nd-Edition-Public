package net.revivers.reviverstwo;

import dev.triumphteam.cmd.bukkit.BukkitCommandManager;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;
import net.revivers.reviverstwo.arenas.games.Game;
import net.revivers.reviverstwo.arenas.games.GameManager;
import net.revivers.reviverstwo.arenas.games.commands.DebugCommand;
import net.revivers.reviverstwo.arenas.games.commands.LeaveCommand;
import net.revivers.reviverstwo.arenas.games.commands.SpectateCommand;
import net.revivers.reviverstwo.arenas.games.hats.HatManager;
import net.revivers.reviverstwo.arenas.games.worlds.WorldManager;
import net.revivers.reviverstwo.credits.CreditsCommand;
import net.revivers.reviverstwo.credits.CreditsMessage;
import net.revivers.reviverstwo.events.EventListener;
import net.revivers.reviverstwo.matches.MatchCommand;
import net.revivers.reviverstwo.arenas.ArenaManager;
import net.revivers.reviverstwo.arenas.games.GameEventHandler;
import net.revivers.reviverstwo.utilities.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class ReviversTwo extends JavaPlugin {

    public final String REVISION = "120";
    private static JavaPlugin plugin;
    private static FileConfiguration config;

    private static Chat chat;
    private static Permission permissions;

    private static Metrics metrics;

    @Override
    public void onEnable() {
        try {
            this.getLogger().info("Starting the plugin. Revision Number: " + REVISION);

            // Check for dependencies
            setupDependencies();

            // Initialize variables
            plugin = this;

            // Load data
            saveDefaultConfig();
            config = getConfig();

            // Load files
            ArenaManager.loadArenas();
            HatManager.loadHats();

            // Start the World Manager
            WorldManager.startUp();

            // Add event listeners
            getServer().getPluginManager().registerEvents(new GameEventHandler(), this);
            getServer().getPluginManager().registerEvents(new CreditsMessage(), this);
            getServer().getPluginManager().registerEvents(new EventListener(), this);

            // Load all commands
            BukkitCommandManager<CommandSender> commandManager = BukkitCommandManager.create(this);
            commandManager.registerCommand(new CreditsCommand());
            commandManager.registerCommand(new MatchCommand());
            commandManager.registerCommand(new LeaveCommand());
            commandManager.registerCommand(new DebugCommand());
            commandManager.registerCommand(new SpectateCommand());

            getLogger().info("Successfully started the plugin! Welcome to the Revivers: 2nd Edition plugin.");

            loadbStats();

        } catch (Exception exc) {
            exc.printStackTrace();
            this.getLogger().severe("There was an exception while trying to start the plugin.");
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {

        this.getLogger().info("Disabling & cleaning up...");

        // Kick every player that is in a game
        GameManager.getGames().forEach(Game::destroyGameRemains);
        this.getLogger().info("Destroyed all active games...");

        // Clean up all the temporal worlds
        WorldManager.cleanUp();
        this.getLogger().info("Deleted all temporal worlds...");

        this.getLogger().info("Successfully disabled Revivers: 2nd Edition!");

    }

    private void loadbStats() {
        int pluginId = 13664;

        metrics = new Metrics(this, pluginId);
    }

    private void setupDependencies() {
        if (getServer().getPluginManager().getPlugin("NametagEdit") == null) {
            getLogger().severe("Couldn't find dependency plugin \"NametagEdit\". Please, put it in the plugins folder.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            getLogger().severe("Couldn't find dependency plugin \"Vault\". Please, put it in the plugins folder.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        setupChat();
        setupPermissions();
    }

    // Vault Stuff
    private void setupChat() {
        chat = getServer().getServicesManager().load(Chat.class);
    }
    public static Chat getChat() {
        return chat;
    }

    private void setupPermissions() {
        permissions = getServer().getServicesManager().load(Permission.class);
    }
    public static Permission getPermissions() {
        return permissions;
    }

    // Data shit
    public static FileConfiguration getConfiguration() {
        return config;
    }

    public static Location getLobbyLocation() {
        return new Location(
                Bukkit.getWorld(config.getString("Lobby.World")),
                config.getDouble("Lobby.X coordinate"),
                config.getDouble("Lobby.Y coordinate"),
                config.getDouble("Lobby.Z coordinate"),
                (float) config.getDouble("Lobby.Yaw"),
                (float) config.getDouble("Lobby.Pitch")
        );
    }

    public static JavaPlugin getPlugin()
    {
        return plugin;
    }

}
