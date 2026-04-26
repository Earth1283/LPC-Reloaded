package de.ayont.lpc.commands

import de.ayont.lpc.LPC
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

class LPCCommand(private val plugin: LPC) : CommandExecutor, TabCompleter {

    override fun onCommand(sender: CommandSender, command: Command, s: String, args: Array<out String>): Boolean {
        if (args.size == 1 && "reload" == args[0]) {
            if (!sender.hasPermission("lpc.reload")) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize(plugin.config.getString("general-messages.no-permission", "<red>You do not have permission to do that!")!!))
                return true
            }
            plugin.reloadConfig()
            val rawReloadMessage = plugin.config.getString("reload-message", "<green>Reloaded LPC Configuration!</green>")!!
            val message = MiniMessage.miniMessage().deserialize(rawReloadMessage)

            plugin.adventure?.sender(sender)?.sendMessage(message)
            return true
        }

        if (args.size == 1 && "bubbles" == args[0]) {
            if (sender !is Player) {
                plugin.adventure?.sender(sender)?.sendMessage(MiniMessage.miniMessage().deserialize(plugin.config.getString("general-messages.only-players", "Only players can use this command.")!!))
                return true
            }
            if (!sender.hasPermission("lpc.bubbles.toggle")) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize(plugin.config.getString("general-messages.no-permission", "<red>You do not have permission to do that!")!!))
                return true
            }

            val enabled = !plugin.playerSettingsManager.isChatBubblesEnabled(sender.uniqueId)
            plugin.playerSettingsManager.setChatBubblesEnabled(sender.uniqueId, enabled)
            plugin.chatBubbleManager?.updateVisibilityForPlayer(sender)

            val status = if (enabled) "<green>enabled" else "<red>disabled"
            val msg = plugin.config.getString("chat-bubbles.messages.toggle", "<gray>Chat bubbles are now {status}<gray>.")!!
                .replace("{status}", status)
            sender.sendMessage(MiniMessage.miniMessage().deserialize(msg))
            return true
        }
        return false
    }

    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): List<String> {
        if (args.size == 1) {
            val suggestions = mutableListOf<String>()
            if (sender.hasPermission("lpc.reload")) suggestions.add("reload")
            if (sender.hasPermission("lpc.bubbles.toggle")) suggestions.add("bubbles")
            return suggestions
        }

        return mutableListOf()
    }
}
