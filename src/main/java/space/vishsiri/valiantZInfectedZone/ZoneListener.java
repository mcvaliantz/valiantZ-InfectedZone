package space.vishsiri.valiantZInfectedZone;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ZoneListener implements Listener {
    private final ValiantZInfectedZone plugin;
    private final ZoneManager zoneManager;
    private final PlayerStatusManager statusManager;
    private final Map<UUID, BukkitRunnable> playerTasks = new HashMap<>();
    private final Map<UUID, String> playerCurrentZone = new HashMap<>();
    private final Map<UUID, Long> playerEntryTimes = new HashMap<>();

    public ZoneListener(ValiantZInfectedZone plugin, PlayerStatusManager statusManager) {
        this.plugin = plugin;
        this.zoneManager = new ZoneManager(plugin);
        this.statusManager = statusManager;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location toLocation = event.getTo();

        if (toLocation == null || toLocation.getWorld() == null) {
            return;
        }

        // Get applicable regions sorted by priority (highest first)
        Set<ProtectedRegion> regions = zoneManager.getRegionsAtLocation(toLocation);
        if (regions == null || regions.isEmpty()) {
            handleExitZone(player);
            return;
        }

        // Find the region with the highest priority from the configuration
        ProtectedRegion highestPriorityRegion = regions.stream()
                .filter(region -> zoneManager.isConfiguredRegion(region.getId()))
                .max((region1, region2) -> {
                    int priority1 = plugin.getPluginConfig().getInt("zone." + region1.getId() + ".priority", 0);
                    int priority2 = plugin.getPluginConfig().getInt("zone." + region2.getId() + ".priority", 0);
                    return Integer.compare(priority1, priority2);
                })
                .orElse(null);

        if (highestPriorityRegion != null) {
            String regionKey = highestPriorityRegion.getId();
            if (!regionKey.equals(playerCurrentZone.get(player.getUniqueId()))) {
                // Player has entered a new region
                handleEntryZone(player, regionKey);
            }
        } else {
            handleExitZone(player);
        }
    }

    private void handleEntryZone(Player player, String regionKey) {
        String type = plugin.getPluginConfig().getString("zone." + regionKey + ".type", "unknown");
        String aliasName = plugin.getPluginConfig().getString("zone." + regionKey + ".alias_name", "Unnamed Zone");
        int delay = plugin.getPluginConfig().getInt("zone." + regionKey + ".player_status.delay", 20);
        int statusValue = plugin.getPluginConfig().getInt("zone." + regionKey + ".player_status.value", 0);

        playerCurrentZone.put(player.getUniqueId(), regionKey); // Track the current zone

        if ("infected".equalsIgnoreCase(type)) {
            if (!playerEntryTimes.containsKey(player.getUniqueId())) {
                playerEntryTimes.put(player.getUniqueId(), System.currentTimeMillis());
            }
            player.sendMessage("§cคุณทำการเข้าโซนติดเชื้อ: " + aliasName);
            startStatusChangeTask(player, aliasName, statusValue, delay, regionKey, false);
        } else if ("safe".equalsIgnoreCase(type)) {
            if (!playerEntryTimes.containsKey(player.getUniqueId())) {
                playerEntryTimes.put(player.getUniqueId(), System.currentTimeMillis());
            }
            player.sendMessage("§aคุณทำการเข้าเซฟโซน: " + aliasName);
            startStatusChangeTask(player, aliasName, statusValue, delay, regionKey, true);
        }
    }

    private void handleExitZone(Player player) {
        UUID playerUUID = player.getUniqueId();
        if (playerCurrentZone.containsKey(playerUUID)) {
            String regionKey = playerCurrentZone.get(playerUUID);
            String aliasName = plugin.getPluginConfig().getString("zone." + regionKey + ".alias_name", "Unnamed Zone");
            player.sendMessage("§eคุณได้ทำการออกโซน: " + aliasName);
            stopTaskForPlayer(playerUUID);
            playerCurrentZone.remove(playerUUID);
            playerEntryTimes.remove(playerUUID);
        }
    }

    private void startStatusChangeTask(Player player, String aliasName, int statusValue, int delay, String regionKey, boolean isSafeZone) {
        UUID playerUUID = player.getUniqueId();

        if (playerTasks.containsKey(playerUUID)) {
            return; // Task already running for this player
        }

        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    this.cancel();
                    playerTasks.remove(playerUUID);
                    return;
                }

                if (isSafeZone) {
                    statusManager.addStatus(playerUUID, Math.abs(statusValue));
//                    player.sendMessage("§aYour status has been increased by " + Math.abs(statusValue) + " in the safe zone (" + aliasName + ").");
                } else {
                    statusManager.takeStatus(playerUUID, Math.abs(statusValue));
//                    player.sendMessage("§cYour status has been decreased by " + Math.abs(statusValue) + " in the infected zone (" + aliasName + ").");

                    // Check if the player's status is 0 and apply an effect (only for infected zones)
                    if (statusManager.getStatus(playerUUID) <= 0) {
                        applyZeroStatusEffect(player);
                        this.cancel();
                        playerTasks.remove(playerUUID);
                    }
                }
            }
        };

        task.runTaskTimer(plugin, delay, delay); // Start the task with an initial delay
        playerTasks.put(playerUUID, task);
    }

    private void stopTaskForPlayer(UUID playerUUID) {
        if (playerTasks.containsKey(playerUUID)) {
            playerTasks.get(playerUUID).cancel();
            playerTasks.remove(playerUUID);
        }
        playerEntryTimes.remove(playerUUID); // Remove entry time when the task stops
    }

    private void applyZeroStatusEffect(Player player) {
        // Example effect when status reaches 0 (e.g., blindness for 5 seconds)
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 1, true, false));
        player.sendMessage("§4Your status has reached zero! A severe effect has been applied.");
    }
}
