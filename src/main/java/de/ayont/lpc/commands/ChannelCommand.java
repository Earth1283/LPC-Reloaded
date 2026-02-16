package de.ayont.lpc.commands;

import de.ayont.lpc.LPC;
import de.ayont.lpc.channels.Channel;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import java.util.Arrays;

public class ChannelCommand implements CommandExecutor {

    private final LPC plugin;

    public ChannelCommand(LPC plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!plugin.getConfig().getBoolean("channels.enabled", false)) {
            plugin.getAdventure().sender(sender).sendMessage(MiniMessage.miniMessage().deserialize(plugin.getConfig().getString("channels.messages.disabled", "<red>Chat channels are disabled.")));
            return true;
        }

        if (!(sender instanceof Player)) {
            plugin.getAdventure().sender(sender).sendMessage(MiniMessage.miniMessage().deserialize(plugin.getConfig().getString("general-messages.only-players", "Only players can use this command.")));
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            // Show current channel
            Channel current = plugin.getChannelManager().getPlayerChannel(player);
            player.sendMessage(MiniMessage.miniMessage().deserialize(plugin.getConfig().getString("channels.messages.current", "<gray>You are currently talking in: <white>{channel}")
                    .replace("{channel}", current != null ? current.getName() : "None")));
            return true;
        }

        String channelName = args[0];
        Channel channel = plugin.getChannelManager().getChannel(channelName);

        if (channel == null) {
            player.sendMessage(MiniMessage.miniMessage().deserialize(plugin.getConfig().getString("channels.messages.not-found", "<red>Channel not found.")));
            return true;
        }

        if (!channel.canJoin(player)) {
            player.sendMessage(MiniMessage.miniMessage().deserialize(plugin.getConfig().getString("channels.messages.no-permission-join", "<red>You do not have permission to join this channel.")));
            return true;
        }

        if (args.length > 1) {
            // Quick message
            String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
            channel.sendMessage(player, message);
        } else {
            // Switch channel
            plugin.getChannelManager().setPlayerChannel(player, channel.getId());
            player.sendMessage(MiniMessage.miniMessage().deserialize(plugin.getConfig().getString("channels.messages.switched", "<green>Switched to channel: <white>{channel}")
                    .replace("{channel}", channel.getName())));
        }

        return true;
    }
}
