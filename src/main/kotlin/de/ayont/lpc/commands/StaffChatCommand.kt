package de.ayont.lpc.commands

import de.ayont.lpc.LPC
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class StaffChatCommand(private val plugin: LPC) : CommandExecutor {

    private val miniMessage = MiniMessage.miniMessage()

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            plugin.adventure?.sender(sender)?.sendMessage(miniMessage.deserialize(plugin.config.getString("general-messages.only-players", "Only players can use this command.")!!))
            return true
        }

        if (!sender.hasPermission("lpc.staffchat")) {
            sender.sendMessage(miniMessage.deserialize(plugin.config.getString("general-messages.no-permission", "<red>You do not have permission to use this command.")!!))
            return true
        }

        if (!plugin.config.getBoolean("staff-chat.enabled", false)) {
             sender.sendMessage(miniMessage.deserialize(plugin.config.getString("staff-chat.messages.disabled", "<red>Staff chat is disabled.")!!))
             return true
        }

        if (args.isEmpty()) {
            sender.sendMessage(miniMessage.deserialize(plugin.config.getString("general-messages.usage", "<red>Usage: {usage}")!!
                .replace("{usage}", "/sc <message>")))
            return true
        }

        val message = args.joinToString(" ")
        val format = plugin.config.getString("staff-chat.format", "<red>[Staff] {name}: <white>{message}")!!
            .replace("{name}", sender.name)
            .replace("{message}", message)

        val component = miniMessage.deserialize(format)
        
        // Broadcast to staff
        Bukkit.getOnlinePlayers().stream()
            .filter { p -> p.hasPermission("lpc.staffchat") }
            .forEach { p -> plugin.adventure?.player(p)?.sendMessage(component) }
        
        // Also send to console
        plugin.adventure?.console()?.sendMessage(component)

        return true
    }
}
