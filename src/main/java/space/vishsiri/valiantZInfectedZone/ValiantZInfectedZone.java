package space.vishsiri.valiantZInfectedZone;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class ValiantZInfectedZone extends JavaPlugin {

    private PlayerDataManager playerDataManager;
    private PlayerStatusManager playerStatusManager;

    @Override
    public void onEnable() {
        // Save the default configuration file
        saveDefaultConfig();
            // Debug: Log default status
            getLogger().info("Default status loaded: " + getConfig().getInt("default_status"));

            // Debug: Log zone configuration
            for (String regionKey : getConfig().getConfigurationSection("zone").getKeys(false)) {
                getLogger().info("Loaded region: " + regionKey);
                String alias = getConfig().getString("zone." + regionKey + ".alias_name", "No Alias");
                getLogger().info("Alias for " + regionKey + ": " + alias);
            }
        getLogger().info("Infected Zone Plugin enabled!");

        // Initialize PlayerDataManager and PlayerStatusManager
        this.playerDataManager = new PlayerDataManager(this);
        this.playerStatusManager = new PlayerStatusManager(playerDataManager);

        // Register event listeners
        getServer().getPluginManager().registerEvents(new ZoneListener(this, playerStatusManager), this);

        // Register the command executor
        this.getCommand("vzinfected").setExecutor(new InfectedZoneCommand(this, playerStatusManager));

        // Register the custom PlaceholderAPI expansion if PlaceholderAPI is installed
        if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new ZonePlaceholderExpansion(this, playerStatusManager).register();
            getLogger().info("PlaceholderAPI expansion registered!");
        } else {
            getLogger().warning("PlaceholderAPI not found. Placeholders will not work.");
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("Infected Zone Plugin disabled!");
    }

    // Accessor method to get the plugin configuration
    public FileConfiguration getPluginConfig() {
        return this.getConfig();
    }
}
