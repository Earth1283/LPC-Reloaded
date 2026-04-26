package de.ayont.lpc.commands

import de.ayont.lpc.LPC
import me.clip.placeholderapi.PlaceholderAPI
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import java.util.*
import java.util.stream.Collectors

class PrivateMessageCommand(private val plugin: LPC) : CommandExecutor, TabCompleter {
    private val miniMessage = MiniMessage.miniMessage()
    private val adventure = plugin.adventure!!

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            adventure.sender(sender).sendMessage(miniMessage.deserialize(plugin.config.getString("general-messages.only-players", "Only players can use this command.")!!))
            return true
        }

        var targetUuid: UUID? = null
        var target: Player? = null
        var message = ""

        if (command.name.equals("msg", ignoreCase = true)) {
            if (!sender.hasPermission("lpc.msg")) {
                sender.sendMessage(miniMessage.deserialize(plugin.config.getString("general-messages.no-permission", "<red>You do not have permission to use this command.")!!))
                return true
            }
            if (args.size < 2) {
                sender.sendMessage(miniMessage.deserialize(plugin.config.getString("general-messages.usage", "<red>Usage: {usage}")!!
                    .replace("{usage}", "/msg <player> <message>")))
                return true
            }
            target = Bukkit.getPlayer(args[0])
            if (target == null) {
                sender.sendMessage(miniMessage.deserialize(plugin.config.getString("general-messages.player-not-found", "Player not found.")!!))
                return true
            }
            targetUuid = target.uniqueId
            message = args.sliceArray(1 until args.size).joinToString(" ")

        } else if (command.name.equals("reply", ignoreCase = true)) {
            if (!sender.hasPermission("lpc.reply")) {
                sender.sendMessage(miniMessage.deserialize(plugin.config.getString("general-messages.no-permission", "<red>You do not have permission to use this command.")!!))
                return true
            }
            if (args.isEmpty()) {
                sender.sendMessage(miniMessage.deserialize(plugin.config.getString("general-messages.usage", "<red>Usage: {usage}")!!
                    .replace("{usage}", "/r <message>")))
                return true
            }
            targetUuid = plugin.privateMessageManager.getLastMessaged(sender.uniqueId)
            if (targetUuid == null) {
                sender.sendMessage(miniMessage.deserialize(plugin.config.getString("social.messages.nobody-to-reply", "<red>You have nobody to reply to.")!!))
                return true
            }
            target = Bukkit.getPlayer(targetUuid)
            if (target == null) {
                sender.sendMessage(miniMessage.deserialize(plugin.config.getString("social.messages.player-offline", "<red>Player is no longer online.")!!))
                return true
            }
            message = args.joinToString(" ")
        }

        if (target != null) {
            if (plugin.playerSettingsManager.isIgnored(target.uniqueId, sender.uniqueId)) {
                sender.sendMessage(miniMessage.deserialize(plugin.config.getString("ignore.messages.player-ignoring", "<red>This player is ignoring you.")!!))
                return true
            }
            sendMessage(sender, target, message)
            plugin.privateMessageManager.setLastMessaged(sender.uniqueId, target.uniqueId)
            plugin.privateMessageManager.setLastMessaged(target.uniqueId, sender.uniqueId)
            return true
        }

        return false
    }

    private fun sendMessage(sender: Player, receiver: Player, message: String) {
        var senderFormat = plugin.config.getString("private-message-formats.sender", "<gray>You to {receiver}: <white>{message}")!!
        var receiverFormat = plugin.config.getString("private-message-formats.receiver", "<gray>{sender} to You: <white>{message}")!!

        senderFormat = replacePlaceholders(senderFormat, sender, receiver, message)
        receiverFormat = replacePlaceholders(receiverFormat, sender, receiver, message)

        val senderComponent = miniMessage.deserialize(senderFormat)
        val receiverComponent = miniMessage.deserialize(receiverFormat)

        adventure.player(sender).sendMessage(senderComponent)
        adventure.player(receiver).sendMessage(receiverComponent)

        var spyFormat = plugin.config.getString("social-spy.format", "<dark_gray>[Spy] <gray>{sender} -> {receiver}: <white>{message}")!!
        spyFormat = replacePlaceholders(spyFormat, sender, receiver, message)
        val spyComponent = miniMessage.deserialize(spyFormat)

        for (onlinePlayer in Bukkit.getOnlinePlayers()) {
            if (plugin.privateMessageManager.isSocialSpy(onlinePlayer.uniqueId) && onlinePlayer.uniqueId != sender.uniqueId && onlinePlayer.uniqueId != receiver.uniqueId) {
                adventure.player(onlinePlayer).sendMessage(spyComponent)
            }
        }
    }

    private fun replacePlaceholders(format: String, sender: Player, receiver: Player, message: String): String {
        var result = format.replace("{sender}", sender.name)
            .replace("{receiver}", receiver.name)
            .replace("{message}", message)

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            result = PlaceholderAPI.setPlaceholders(sender, result)
        }
        
        return result
    }

    override fun onTabComplete(sender: CommandSender, command: Command, label: String, args: Array<out String>): List<String> {
        if (command.name.equals("msg", ignoreCase = true) && args.size == 1) {
            return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter { name -> name.lowercase(Locale.getDefault()).startsWith(args[0].lowercase(Locale.getDefault())) }
                .collect(Collectors.toList())
        }
        return emptyList()
    }
}
