package de.ayont.lpc.commands

import de.ayont.lpc.LPC
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class SocialSpyCommand(private val plugin: LPC) : CommandExecutor {
    private val miniMessage = MiniMessage.miniMessage()

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            plugin.adventure?.sender(sender)?.sendMessage(miniMessage.deserialize(plugin.config.getString("general-messages.only-players", "Only players can use this command.")!!))
            return true
        }

        if (!sender.hasPermission("lpc.socialspy")) {
            sender.sendMessage(miniMessage.deserialize(plugin.config.getString("general-messages.no-permission", "<red>You do not have permission to use this command.")!!))
            return true
        }

        val newState = !plugin.privateMessageManager.isSocialSpy(sender.uniqueId)
        plugin.privateMessageManager.setSocialSpy(sender.uniqueId, newState)

        if (newState) {
            sender.sendMessage(miniMessage.deserialize(plugin.config.getString("social-spy.messages.enabled", "<green>Social Spy enabled.")!!))
        } else {
            sender.sendMessage(miniMessage.deserialize(plugin.config.getString("social-spy.messages.disabled", "<red>Social Spy disabled.")!!))
        }

        return true
    }
}
