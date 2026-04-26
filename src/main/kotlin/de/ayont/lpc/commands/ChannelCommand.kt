package de.ayont.lpc.commands

import de.ayont.lpc.LPC
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class ChannelCommand(private val plugin: LPC) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!plugin.config.getBoolean("channels.enabled", false)) {
            plugin.adventure?.sender(sender)?.sendMessage(MiniMessage.miniMessage().deserialize(plugin.config.getString("channels.messages.disabled", "<red>Chat channels are disabled.")!!))
            return true
        }

        if (sender !is Player) {
            plugin.adventure?.sender(sender)?.sendMessage(MiniMessage.miniMessage().deserialize(plugin.config.getString("general-messages.only-players", "Only players can use this command.")!!))
            return true
        }

        val channelManager = plugin.channelManager ?: return true

        if (args.isEmpty()) {
            // Show current channel
            val current = channelManager.getPlayerChannel(sender)
            sender.sendMessage(MiniMessage.miniMessage().deserialize(plugin.config.getString("channels.messages.current", "<gray>You are currently talking in: <white>{channel}")!!
                .replace("{channel}", current?.getName() ?: "None")))
            return true
        }

        val channelName = args[0]
        val channel = channelManager.getChannel(channelName)

        if (channel == null) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize(plugin.config.getString("channels.messages.not-found", "<red>Channel not found.")!!))
            return true
        }

        if (!channel.canJoin(sender)) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize(plugin.config.getString("channels.messages.no-permission-join", "<red>You do not have permission to join this channel.")!!))
            return true
        }

        if (args.size > 1) {
            // Quick message
            val message = args.sliceArray(1 until args.size).joinToString(" ")
            channel.sendMessage(sender, message)
        } else {
            // Switch channel
            channelManager.setPlayerChannel(sender, channel.getId())
            sender.sendMessage(MiniMessage.miniMessage().deserialize(plugin.config.getString("channels.messages.switched", "<green>Switched to channel: <white>{channel}")!!
                .replace("{channel}", channel.getName())))
        }

        return true
    }
}
