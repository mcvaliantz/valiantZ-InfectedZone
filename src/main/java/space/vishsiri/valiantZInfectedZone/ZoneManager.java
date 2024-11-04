package space.vishsiri.valiantZInfectedZone;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Set;

public class ZoneManager {
    private final ValiantZInfectedZone plugin;

    public ZoneManager(ValiantZInfectedZone plugin) {
        this.plugin = plugin;
    }

    public Set<ProtectedRegion> getRegionsAtLocation(Location location) {
        if (location == null || location.getWorld() == null) {
            plugin.getLogger().warning("Location or world is null when fetching regions.");
            return null;
        }

        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regions = container.get(BukkitAdapter.adapt(location.getWorld()));
        if (regions == null) {
            plugin.getLogger().warning("RegionManager is null for world: " + location.getWorld().getName());
            return null;
        }

        BlockVector3 blockVector = BukkitAdapter.asBlockVector(location);
        ApplicableRegionSet regionSet = regions.getApplicableRegions(blockVector);
//        plugin.getLogger().info("Found " + regionSet.size() + " regions at location: " + location);

        return regionSet.getRegions();
    }

    public boolean isConfiguredRegion(String regionKey) {
        boolean isConfigured = plugin.getPluginConfig().getConfigurationSection("zone").contains(regionKey);
        if (!isConfigured) {
            plugin.getLogger().info("Region " + regionKey + " is not configured in the plugin.");
        }
        return isConfigured;
    }

    public void applyEffects(Player player, String regionKey) {
        if (plugin.getPluginConfig().getConfigurationSection("zone." + regionKey + ".effect") == null) {
            plugin.getLogger().info("No effects configured for region: " + regionKey);
            return;
        }

        plugin.getPluginConfig().getConfigurationSection("zone." + regionKey + ".effect").getKeys(false).forEach(effectKey -> {
            String potionType = plugin.getPluginConfig().getString("zone." + regionKey + ".effect." + effectKey + ".potion");
            int level = plugin.getPluginConfig().getInt("zone." + regionKey + ".effect." + effectKey + ".level");
            int duration = plugin.getPluginConfig().getInt("zone." + regionKey + ".effect." + effectKey + ".duration", 100); // Configurable duration

            PotionEffectType effectType = PotionEffectType.getByName(potionType);
            if (effectType != null) {
                player.addPotionEffect(new PotionEffect(effectType, duration, level - 1, true, false));
                plugin.getLogger().info("Applied " + potionType + " to " + player.getName() + " for " + duration + " ticks at level " + level);
            } else {
                plugin.getLogger().warning("Invalid potion type: " + potionType + " in region: " + regionKey);
            }
        });
    }
}
