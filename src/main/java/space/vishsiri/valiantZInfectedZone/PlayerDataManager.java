package space.vishsiri.valiantZInfectedZone;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class PlayerDataManager {

    private final ValiantZInfectedZone plugin;

    public PlayerDataManager(ValiantZInfectedZone plugin) {
        this.plugin = plugin;
    }

    // Get the data file for a player
    private File getPlayerFile(UUID playerUUID) {
        File dataFolder = new File(plugin.getDataFolder(), "data");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        return new File(dataFolder, playerUUID.toString() + ".yml");
    }

    // Load player data
    public FileConfiguration getPlayerData(UUID playerUUID) {
        File playerFile = getPlayerFile(playerUUID);
        if (!playerFile.exists()) {
            createDefaultPlayerData(playerUUID);
        }
        return YamlConfiguration.loadConfiguration(playerFile);
    }

    // Create default data for a new player
    private void createDefaultPlayerData(UUID playerUUID) {
        File playerFile = getPlayerFile(playerUUID);
        FileConfiguration playerData = YamlConfiguration.loadConfiguration(playerFile);

        playerData.set("status", 10); // Default status value
        playerData.set("max", 100);   // Default max value

        try {
            playerData.save(playerFile);
        } catch (IOException e) {
            Bukkit.getLogger().severe("Could not save data for player " + playerUUID);
            e.printStackTrace();
        }
    }

    // Save player data
    public void savePlayerData(UUID playerUUID, FileConfiguration playerData) {
        try {
            playerData.save(getPlayerFile(playerUUID));
        } catch (IOException e) {
            Bukkit.getLogger().severe("Could not save data for player " + playerUUID);
            e.printStackTrace();
        }
    }

    // Update status
    public void setPlayerStatus(UUID playerUUID, int status) {
        FileConfiguration playerData = getPlayerData(playerUUID);
        playerData.set("status", status);
        savePlayerData(playerUUID, playerData);
    }

    // Update max status
    public void setPlayerMaxStatus(UUID playerUUID, int max) {
        FileConfiguration playerData = getPlayerData(playerUUID);
        playerData.set("max", max);
        savePlayerData(playerUUID, playerData);
    }

    // Get status
    public int getPlayerStatus(UUID playerUUID) {
        return getPlayerData(playerUUID).getInt("status", 10); // Default to 10 if not found
    }

    // Get max status
    public int getPlayerMaxStatus(UUID playerUUID) {
        return getPlayerData(playerUUID).getInt("max", 100); // Default to 100 if not found
    }
}
