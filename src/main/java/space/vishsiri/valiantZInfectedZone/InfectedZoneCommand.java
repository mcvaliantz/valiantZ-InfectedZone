package space.vishsiri.valiantZInfectedZone;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import java.util.UUID;

public class InfectedZoneCommand implements CommandExecutor {

    private final ValiantZInfectedZone plugin;
    private final PlayerStatusManager statusManager;

    public InfectedZoneCommand(ValiantZInfectedZone plugin, PlayerStatusManager statusManager) {
        this.plugin = plugin;
        this.statusManager = statusManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§cUsage: /vzinfected reload | status <player> <add|remove|get|setmax|take> <value>");
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (sender.hasPermission("vzinfectedzone.reload")) {
                plugin.reloadConfig();
                sender.sendMessage("§aValiantZ Infected Zone configuration reloaded successfully!");
                plugin.getLogger().info("Configuration reloaded by " + sender.getName());
            } else {
                sender.sendMessage("§cYou do not have permission to use this command.");
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("status")) {
            if (args.length < 3) {
                sender.sendMessage("§cUsage: /vzinfected status <player> <add|remove|get|setmax|take> <value>");
                return true;
            }

            OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(args[1]);
            if (targetPlayer == null || !targetPlayer.hasPlayedBefore()) {
                sender.sendMessage("§cPlayer not found.");
                return true;
            }

            String action = args[2].toLowerCase();
            UUID playerUUID = targetPlayer.getUniqueId();
            int value = 0;

            if (args.length == 4) {
                try {
                    value = Integer.parseInt(args[3]);
                } catch (NumberFormatException e) {
                    sender.sendMessage("§cValue must be a number.");
                    return true;
                }
            }

            switch (action) {
                case "add":
                    statusManager.addStatus(playerUUID, value);
                    sender.sendMessage("§aAdded " + value + " to " + targetPlayer.getName() + "'s status.");
                    break;
                case "remove":
                    statusManager.removeStatus(playerUUID, value);
                    sender.sendMessage("§aRemoved " + value + " from " + targetPlayer.getName() + "'s status.");
                    break;
                case "get":
                    int currentStatus = statusManager.getStatus(playerUUID);
                    sender.sendMessage("§a" + targetPlayer.getName() + "'s current status: " + currentStatus);
                    break;
                case "setmax":
                    statusManager.setMaxStatus(playerUUID, value);
                    sender.sendMessage("§aSet max status for " + targetPlayer.getName() + " to " + value + ".");
                    break;
                case "take":
                    statusManager.takeStatus(playerUUID, value);
                    sender.sendMessage("§aTook " + value + " from " + targetPlayer.getName() + "'s status.");
                    break;
                default:
                    sender.sendMessage("§cUnknown action: " + action);
                    break;
            }
            return true;
        }

        return false;
    }
}
