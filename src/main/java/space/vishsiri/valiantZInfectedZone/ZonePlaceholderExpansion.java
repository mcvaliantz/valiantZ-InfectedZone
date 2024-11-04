package space.vishsiri.valiantZInfectedZone;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;

public class ZonePlaceholderExpansion extends PlaceholderExpansion {

    private final ValiantZInfectedZone plugin;
    private final PlayerStatusManager statusManager;

    public ZonePlaceholderExpansion(ValiantZInfectedZone plugin, PlayerStatusManager statusManager) {
        this.plugin = plugin;
        this.statusManager = statusManager;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "valiantz";
    }

    @Override
    public @NotNull String getAuthor() {
        return "VisherRyz"; // Replace with your name or organization
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true; // Ensure the placeholder expansion stays registered
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String identifier) {
        if (player == null) {
            return "";
        }

        // Placeholder: %valiantz_current_status%
        if (identifier.equals("current_status")) {
            int currentStatus = statusManager.getStatus(player.getUniqueId());
            return String.valueOf(currentStatus);
        }

//        // Placeholder: %valiantz_max_status%
//        if (identifier.equals("max_status")) {
//            int maxStatus = statusManager.getMaxStatus(player.getUniqueId());
//            return String.valueOf(maxStatus);
//        }

        // Placeholder: %valiantz_alias_zone%
        if (identifier.equals("alias_zone")) {
            Location loc = player.getLocation();
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager regions = container.get(BukkitAdapter.adapt(loc.getWorld()));

            if (regions != null) {
                BlockVector3 blockVector = BukkitAdapter.asBlockVector(loc);
                ApplicableRegionSet regionSet = regions.getApplicableRegions(blockVector);
                for (ProtectedRegion region : regionSet) {
                    String regionKey = region.getId();
                    if (plugin.getPluginConfig().getConfigurationSection("zone").contains(regionKey)) {
                        return plugin.getPluginConfig().getString("zone." + regionKey + ".alias_name", "Unnamed Zone");
                    }
                }
            }
            return "Outside Zone";
        }

        return null; // Unknown placeholder
    }
}
