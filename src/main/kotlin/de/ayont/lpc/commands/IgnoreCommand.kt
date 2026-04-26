package de.ayont.lpc.commands

import de.ayont.lpc.LPC
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class IgnoreCommand(private val plugin: LPC) : CommandExecutor {
    private val miniMessage = MiniMessage.miniMessage()

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            plugin.adventure?.sender(sender)?.sendMessage(miniMessage.deserialize(plugin.config.getString("general-messages.only-players", "Only players can use this command.")!!))
            return true
        }

        if (!plugin.config.getBoolean("ignore.enabled", false)) {
            sender.sendMessage(miniMessage.deserialize(plugin.config.getString("general-messages.no-permission", "<red>The ignore system is disabled.")!!))
            return true
        }

        if (args.isEmpty()) {
            sender.sendMessage(miniMessage.deserialize(plugin.config.getString("general-messages.usage", "<red>Usage: {usage}")!!
                .replace("{usage}", "/ignore <player>")))
            return true
        }

        val targetName = args[0]
        val target = Bukkit.getPlayer(targetName)
        val offlineTarget = target ?: Bukkit.getOfflinePlayer(targetName)

        if (offlineTarget.uniqueId == sender.uniqueId) {
            sender.sendMessage(miniMessage.deserialize(plugin.config.getString("ignore.messages.cannot-ignore-self", "<red>You cannot ignore yourself!")!!))
            return true
        }

        val isIgnored = plugin.playerSettingsManager.isIgnored(sender.uniqueId, offlineTarget.uniqueId)

        if (isIgnored) {
            plugin.playerSettingsManager.unignorePlayer(sender.uniqueId, offlineTarget.uniqueId)
            val msg = plugin.config.getString("ignore.messages.unignored", "<green>You are no longer ignoring {player}.")!!
            sender.sendMessage(miniMessage.deserialize(msg.replace("{player}", targetName)))
        } else {
            plugin.playerSettingsManager.ignorePlayer(sender.uniqueId, offlineTarget.uniqueId)
            val msg = plugin.config.getString("ignore.messages.ignored", "<red>You are now ignoring {player}.")!!
            sender.sendMessage(miniMessage.deserialize(msg.replace("{player}", targetName)))
        }

        return true
    }
}
