package de.ayont.lpc.commands;

import de.ayont.lpc.LPC;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import de.ayont.lpc.LPC;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LPCCommand implements CommandExecutor, TabCompleter {

    private final LPC plugin;

    public LPCCommand(LPC plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length == 1 && "reload".equals(args[0])) {
            if (!sender.hasPermission("lpc.reload")) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>You do not have permission to reload the config."));
                return true;
            }
            plugin.reloadConfig();
            String rawReloadMessage = plugin.getConfig().getString("reload-message", "<green>Reloaded LPC Configuration!</green>");
            Component message = MiniMessage.miniMessage().deserialize(rawReloadMessage);

            if (plugin.getServer().getName().toLowerCase().contains("paper")) {
                sender.sendMessage(message);
            } else {
                sender.sendMessage(LPC.getLegacySerializer().serialize(message));
            }
            return true;
        }

        if (args.length == 1 && "bubbles".equals(args[0])) {
            if (!(sender instanceof org.bukkit.entity.Player)) {
                sender.sendMessage("This command can only be used by players.");
                return true;
            }
            org.bukkit.entity.Player player = (org.bukkit.entity.Player) sender;
            if (!player.hasPermission("lpc.bubbles.toggle")) {
                player.sendMessage(MiniMessage.miniMessage().deserialize("<red>You do not have permission to toggle chat bubbles."));
                return true;
            }

            boolean enabled = !plugin.isChatBubblesEnabled(player.getUniqueId());
            plugin.setChatBubblesEnabled(player.getUniqueId(), enabled);
            plugin.getChatBubbleManager().updateVisibilityForPlayer(player);

            String status = enabled ? "<green>enabled" : "<red>disabled";
            player.sendMessage(MiniMessage.miniMessage().deserialize("<gray>Chat bubbles are now " + status + "<gray>."));
            return true;
        }
        return false;
    }


    public List<String> onTabComplete(final @NotNull CommandSender sender, final @NotNull Command command, final @NotNull String alias, final String[] args) {
        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>();
            if (sender.hasPermission("lpc.reload")) suggestions.add("reload");
            if (sender.hasPermission("lpc.bubbles.toggle")) suggestions.add("bubbles");
            return suggestions;
        }

        return new ArrayList<>();
    }
}
