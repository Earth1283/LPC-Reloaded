package de.ayont.lpc.commands;

import de.ayont.lpc.LPC;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class IgnoreCommand implements CommandExecutor {

    private final LPC plugin;
    private final MiniMessage miniMessage;

    public IgnoreCommand(LPC plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (!plugin.getConfig().getBoolean("ignore.enabled", false)) {
            player.sendMessage(miniMessage.deserialize("<red>The ignore system is disabled."));
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(miniMessage.deserialize("<red>Usage: /ignore <player>"));
            return true;
        }

        String targetName = args[0];
        Player target = Bukkit.getPlayer(targetName);
        OfflinePlayer offlineTarget = target != null ? target : Bukkit.getOfflinePlayer(targetName); // Simple offline check

        if (offlineTarget.getUniqueId().equals(player.getUniqueId())) {
            player.sendMessage(miniMessage.deserialize(plugin.getConfig().getString("ignore.messages.cannot-ignore-self", "<red>You cannot ignore yourself!")));
            return true;
        }

        boolean isIgnored = plugin.isIgnored(player.getUniqueId(), offlineTarget.getUniqueId());

        if (isIgnored) {
            plugin.unignorePlayer(player.getUniqueId(), offlineTarget.getUniqueId());
            String msg = plugin.getConfig().getString("ignore.messages.unignored", "<green>You are no longer ignoring {player}.");
            player.sendMessage(miniMessage.deserialize(msg.replace("{player}", targetName)));
        } else {
            plugin.ignorePlayer(player.getUniqueId(), offlineTarget.getUniqueId());
            String msg = plugin.getConfig().getString("ignore.messages.ignored", "<red>You are now ignoring {player}.");
            player.sendMessage(miniMessage.deserialize(msg.replace("{player}", targetName)));
        }

        return true;
    }
}
