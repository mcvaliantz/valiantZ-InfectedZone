package space.vishsiri.valiantZInfectedZone;

import java.util.UUID;

public class PlayerStatusManager {
    private final PlayerDataManager dataManager;

    public PlayerStatusManager(PlayerDataManager dataManager) {
        this.dataManager = dataManager;
    }

    public int getStatus(UUID playerUUID) {
        return dataManager.getPlayerStatus(playerUUID);
    }

    public void setStatus(UUID playerUUID, int value) {
        int max = getMaxStatus(playerUUID);
        if (value > max) {
            value = max; // Cap the status at max if it exceeds
        }
        dataManager.setPlayerStatus(playerUUID, value);
    }

    public void addStatus(UUID playerUUID, int value) {
        int newStatus = getStatus(playerUUID) + value;
        setStatus(playerUUID, newStatus); // Will automatically cap at max in setStatus()
    }

    public void removeStatus(UUID playerUUID, int value) {
        int newStatus = getStatus(playerUUID) - value;
        if (newStatus < 0) {
            newStatus = 0; // Ensure status does not go below 0
        }
        setStatus(playerUUID, newStatus);
    }

    public void takeStatus(UUID playerUUID, int value) {
        removeStatus(playerUUID, value);
    }

    public int getMaxStatus(UUID playerUUID) {
        return dataManager.getPlayerMaxStatus(playerUUID);
    }

    public void setMaxStatus(UUID playerUUID, int value) {
        dataManager.setPlayerMaxStatus(playerUUID, value);
    }
}
