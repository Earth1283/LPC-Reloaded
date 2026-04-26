package de.ayont.lpc.commands

import de.ayont.lpc.LPC
import de.ayont.lpc.moderation.Mute
import de.ayont.lpc.moderation.Warning
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import java.util.*
import java.util.regex.Pattern
import java.util.stream.Collectors

class ModerationCommand(private val plugin: LPC) : CommandExecutor, TabCompleter {
    private val miniMessage = MiniMessage.miniMessage()

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        return when (command.name.lowercase(Locale.getDefault())) {
            "mute" -> handleMute(sender, args)
            "unmute" -> handleUnmute(sender, args)
            "warn" -> handleWarn(sender, args)
            "warnings" -> handleWarnings(sender, args)
            "delwarn" -> handleDelWarn(sender, args)
            "profile" -> handleProfile(sender, args)
            "setbio" -> handleSetBio(sender, args)
            "slowmode" -> handleSlowmode(sender, args)
            else -> false
        }
    }

    private fun handleMute(sender: CommandSender, args: Array<out String>): Boolean {
        if (!plugin.config.getBoolean("mutes.enabled", true)) {
            sender.sendMessage(miniMessage.deserialize(plugin.config.getString("mutes.messages.disabled", "<red>Muting is currently disabled.")!!))
            return true
        }
        if (!sender.hasPermission("lpc.mute")) {
            sender.sendMessage(miniMessage.deserialize(plugin.config.getString("general-messages.no-permission", "<red>No permission.")!!))
            return true
        }
        if (args.isEmpty()) {
            sender.sendMessage(miniMessage.deserialize(plugin.config.getString("general-messages.usage", "<red>Usage: {usage}")!!
                .replace("{usage}", "/mute <player> [duration] [reason]")))
            return true
        }

        val target = Bukkit.getOfflinePlayer(args[0])
        var expiry: Long = -1
        var reason = plugin.config.getString("mutes.default-reason", "No reason provided")!!
        
        var reasonIndex = 1
        if (args.size >= 2) {
            val duration = parseDuration(args[1])
            if (duration != 0L) {
                expiry = System.currentTimeMillis() + duration
                reasonIndex = 2
            }
        }

        if (args.size > reasonIndex) {
            reason = args.sliceArray(reasonIndex until args.size).joinToString(" ")
        }

        val mute = Mute(target.uniqueId, if (sender is Player) sender.uniqueId else UUID.nameUUIDFromBytes("CONSOLE".toByteArray()), reason, expiry)
        plugin.moderationManager?.mutePlayer(mute)

        val timeStr = if (expiry == -1L) "Permanent" else formatTimeLeft(expiry - System.currentTimeMillis())
        val broadcast = plugin.config.getString("mutes.messages.broadcast-mute", "<red>{player} has been muted by {staff} for {reason} ({time}).")!!
            .replace("{player}", target.name ?: args[0])
            .replace("{staff}", sender.name)
            .replace("{reason}", reason)
            .replace("{time}", timeStr)
        
        Bukkit.broadcast(miniMessage.deserialize(broadcast), "lpc.mute")
        return true
    }

    private fun handleUnmute(sender: CommandSender, args: Array<out String>): Boolean {
        if (!plugin.config.getBoolean("mutes.enabled", true)) {
            sender.sendMessage(miniMessage.deserialize(plugin.config.getString("mutes.messages.disabled", "<red>Muting is currently disabled.")!!))
            return true
        }
        if (!sender.hasPermission("lpc.unmute")) {
            sender.sendMessage(miniMessage.deserialize(plugin.config.getString("general-messages.no-permission", "<red>No permission.")!!))
            return true
        }
        if (args.isEmpty()) {
            sender.sendMessage(miniMessage.deserialize(plugin.config.getString("general-messages.usage", "<red>Usage: {usage}")!!
                .replace("{usage}", "/unmute <player>")))
            return true
        }

        val target = Bukkit.getOfflinePlayer(args[0])
        plugin.moderationManager?.unmutePlayer(target.uniqueId)

        val broadcast = plugin.config.getString("mutes.messages.broadcast-unmute", "<green>{player} has been unmuted by {staff}.")!!
            .replace("{player}", target.name ?: args[0])
            .replace("{staff}", sender.name)
        
        Bukkit.broadcast(miniMessage.deserialize(broadcast), "lpc.mute")
        return true
    }

    private fun handleWarn(sender: CommandSender, args: Array<out String>): Boolean {
        if (!plugin.config.getBoolean("warnings.enabled", true)) {
            sender.sendMessage(miniMessage.deserialize(plugin.config.getString("warnings.messages.disabled", "<red>Warnings are currently disabled.")!!))
            return true
        }
        if (!sender.hasPermission("lpc.warn")) {
            sender.sendMessage(miniMessage.deserialize(plugin.config.getString("general-messages.no-permission", "<red>No permission.")!!))
            return true
        }
        if (args.size < 2) {
            sender.sendMessage(miniMessage.deserialize(plugin.config.getString("general-messages.usage", "<red>Usage: {usage}")!!
                .replace("{usage}", "/warn <player> <reason>")))
            return true
        }

        val target = Bukkit.getOfflinePlayer(args[0])
        val reason = args.sliceArray(1 until args.size).joinToString(" ")

        val warning = Warning(0, target.uniqueId, if (sender is Player) sender.uniqueId else UUID.nameUUIDFromBytes("CONSOLE".toByteArray()), reason, System.currentTimeMillis())
        plugin.moderationManager?.warnPlayer(warning)

        val broadcast = plugin.config.getString("warnings.messages.broadcast", "<red>{player} has been warned by {staff} for {reason}.")!!
            .replace("{player}", target.name ?: args[0])
            .replace("{staff}", sender.name)
            .replace("{reason}", reason)
        
        Bukkit.broadcast(miniMessage.deserialize(broadcast), "lpc.warn")
        
        if (target.isOnline && target.player != null) {
            val warnedMsg = plugin.config.getString("warnings.messages.warned", "<red>You have been warned for: <white>{reason}")!!
                .replace("{reason}", reason)
            target.player!!.sendMessage(miniMessage.deserialize(warnedMsg))
        }
        return true
    }

    private fun handleWarnings(sender: CommandSender, args: Array<out String>): Boolean {
        if (!plugin.config.getBoolean("warnings.enabled", true)) {
            sender.sendMessage(miniMessage.deserialize(plugin.config.getString("warnings.messages.disabled", "<red>Warnings are currently disabled.")!!))
            return true
        }
        if (!sender.hasPermission("lpc.warnings")) {
            sender.sendMessage(miniMessage.deserialize(plugin.config.getString("general-messages.no-permission", "<red>No permission.")!!))
            return true
        }
        if (args.isEmpty()) {
            sender.sendMessage(miniMessage.deserialize(plugin.config.getString("general-messages.usage", "<red>Usage: {usage}")!!
                .replace("{usage}", "/warnings <player>")))
            return true
        }

        val target = Bukkit.getOfflinePlayer(args[0])
        val warnings = plugin.moderationManager?.getWarnings(target.uniqueId) ?: emptyList()

        if (warnings.isEmpty()) {
            sender.sendMessage(miniMessage.deserialize(plugin.config.getString("warnings.messages.no-warnings", "<gray>No warnings for {player}")!!
                .replace("{player}", target.name ?: args[0])))
            return true
        }

        sender.sendMessage(miniMessage.deserialize(plugin.config.getString("warnings.messages.header", "<gold>Warnings for {player}:")!!
            .replace("{player}", target.name ?: args[0])))
        for (w in warnings) {
            sender.sendMessage(miniMessage.deserialize(plugin.config.getString("warnings.messages.item", "<yellow>#{id} - <white>{reason} <gray>({date})")!!
                .replace("{id}", w.id.toString())
                .replace("{reason}", w.reason)
                .replace("{date}", Date(w.timestamp).toString())))
        }
        return true
    }

    private fun handleDelWarn(sender: CommandSender, args: Array<out String>): Boolean {
        if (!plugin.config.getBoolean("warnings.enabled", true)) {
            sender.sendMessage(miniMessage.deserialize(plugin.config.getString("warnings.messages.disabled", "<red>Warnings are currently disabled.")!!))
            return true
        }
        if (!sender.hasPermission("lpc.delwarn")) {
            sender.sendMessage(miniMessage.deserialize(plugin.config.getString("general-messages.no-permission", "<red>No permission.")!!))
            return true
        }
        if (args.isEmpty()) {
            sender.sendMessage(miniMessage.deserialize(plugin.config.getString("general-messages.usage", "<red>Usage: {usage}")!!
                .replace("{usage}", "/delwarn <id>")))
            return true
        }

        try {
            val id = args[0].toInt()
            plugin.moderationManager?.deleteWarning(id)
            sender.sendMessage(miniMessage.deserialize(plugin.config.getString("warnings.messages.deleted", "<green>Warning #{id} deleted.")!!
                .replace("{id}", args[0])))
        } catch (e: NumberFormatException) {
            sender.sendMessage(miniMessage.deserialize(plugin.config.getString("warnings.messages.invalid-id", "<red>Invalid ID.")!!))
        }
        return true
    }

    private fun handleProfile(sender: CommandSender, args: Array<out String>): Boolean {
        if (!plugin.config.getBoolean("profiles.enabled", true)) {
            sender.sendMessage(miniMessage.deserialize(plugin.config.getString("profiles.messages.disabled", "<red>Profiles are currently disabled.")!!))
            return true
        }
        val target: OfflinePlayer
        if (args.isEmpty()) {
            if (sender !is Player) {
                plugin.adventure?.sender(sender)?.sendMessage(miniMessage.deserialize(plugin.config.getString("general-messages.usage", "<red>Usage: {usage}")!!
                    .replace("{usage}", "/profile <player>")))
                return true
            }
            target = sender
        } else {
            if (!sender.hasPermission("lpc.profile.others")) {
                sender.sendMessage(miniMessage.deserialize(plugin.config.getString("general-messages.no-permission", "<red>No permission.")!!))
                return true
            }
            target = Bukkit.getOfflinePlayer(args[0])
        }

        val bio = plugin.moderationManager?.getBio(target.uniqueId) ?: ""
        val warnings = plugin.moderationManager?.getWarnings(target.uniqueId) ?: emptyList()
        val mute = plugin.moderationManager?.getActiveMute(target.uniqueId)

        sender.sendMessage(miniMessage.deserialize(plugin.config.getString("profiles.messages.header", "<gold>Profile: {player}")!!
            .replace("{player}", target.name ?: args[0])))
        sender.sendMessage(miniMessage.deserialize(plugin.config.getString("profiles.messages.bio", "<gray>Bio: <white>{bio}")!!
            .replace("{bio}", if (bio.isEmpty()) plugin.config.getString("profiles.messages.no-bio", "No bio set.")!! else bio)))
        sender.sendMessage(miniMessage.deserialize(plugin.config.getString("profiles.messages.warnings", "<gray>Warnings: <white>{count}")!!
            .replace("{count}", warnings.size.toString())))
        if (mute != null) {
            val time = if (mute.isPermanent) "Permanent" else formatTimeLeft(mute.expiry - System.currentTimeMillis())
            sender.sendMessage(miniMessage.deserialize(plugin.config.getString("profiles.messages.mute", "<gray>Mute: <red>{reason} (Expires: {time})")!!
                .replace("{reason}", mute.reason)
                .replace("{time}", time)))
        }
        return true
    }

    private fun handleSetBio(sender: CommandSender, args: Array<out String>): Boolean {
        if (!plugin.config.getBoolean("profiles.enabled", true)) {
            sender.sendMessage(miniMessage.deserialize(plugin.config.getString("profiles.messages.disabled", "<red>Profiles are currently disabled.")!!))
            return true
        }
        if (sender !is Player) {
            plugin.adventure?.sender(sender)?.sendMessage(miniMessage.deserialize(plugin.config.getString("general-messages.only-players", "Only players can use this command.")!!))
            return true
        }
        if (args.isEmpty()) {
            sender.sendMessage(miniMessage.deserialize(plugin.config.getString("general-messages.usage", "<red>Usage: {usage}")!!
                .replace("{usage}", "/setbio <text>")))
            return true
        }

        val bio = args.joinToString(" ")
        plugin.moderationManager?.setBio(sender.uniqueId, bio)
        sender.sendMessage(miniMessage.deserialize(plugin.config.getString("profiles.messages.bio-set", "<green>Bio updated!")!!))
        return true
    }

    private fun handleSlowmode(sender: CommandSender, args: Array<out String>): Boolean {
        if (!sender.hasPermission("lpc.slowmode.set")) {
            sender.sendMessage(miniMessage.deserialize(plugin.config.getString("general-messages.no-permission", "<red>No permission.")!!))
            return true
        }
        if (args.size < 2) {
            sender.sendMessage(miniMessage.deserialize(plugin.config.getString("general-messages.usage", "<red>Usage: {usage}")!!
                .replace("{usage}", "/slowmode <player> <seconds>")))
            return true
        }

        val target = Bukkit.getOfflinePlayer(args[0])
        try {
            val seconds = args[1].toDouble()
            plugin.moderationManager?.setSlowmode(target.uniqueId, seconds)
            sender.sendMessage(miniMessage.deserialize(plugin.config.getString("moderation.messages.slowmode-set", "<green>Slowmode for {player} set to {seconds}s.")!!
                .replace("{player}", target.name ?: args[0])
                .replace("{seconds}", seconds.toString())))
        } catch (e: NumberFormatException) {
            sender.sendMessage(miniMessage.deserialize(plugin.config.getString("moderation.messages.invalid-seconds", "<red>Invalid seconds.")!!))
        }
        return true
    }

    private fun parseDuration(duration: String): Long {
        val pattern = Pattern.compile("(\\d+)([smhd])")
        val matcher = pattern.matcher(duration.lowercase(Locale.getDefault()))
        var totalMillis: Long = 0
        while (matcher.find()) {
            val value = matcher.group(1).toLong()
            val unit = matcher.group(2)
            when (unit) {
                "s" -> totalMillis += value * 1000
                "m" -> totalMillis += value * 60 * 1000
                "h" -> totalMillis += value * 60 * 60 * 1000
                "d" -> totalMillis += value * 24 * 60 * 60 * 1000
            }
        }
        return totalMillis
    }

    private fun formatTimeLeft(millis: Long): String {
        if (millis <= 0) return "0s"
        val seconds = millis / 1000 % 60
        val minutes = millis / (60 * 1000) % 60
        val hours = millis / (60 * 60 * 1000) % 24
        val days = millis / (24 * 60 * 60 * 1000)

        val sb = StringBuilder()
        if (days > 0) sb.append(days).append("d ")
        if (hours > 0) sb.append(hours).append("h ")
        if (minutes > 0) sb.append(minutes).append("m ")
        if (seconds > 0) sb.append(seconds).append("s")
        return sb.toString().trim()
    }

    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): List<String> {
        if (args.size == 1) {
            if (command.name.equals("setbio", ignoreCase = true) || command.name.equals("delwarn", ignoreCase = true)) return emptyList()
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).filter { n -> n.lowercase(Locale.getDefault()).startsWith(args[0].lowercase(Locale.getDefault())) }.collect(Collectors.toList())
        }
        if (args.size == 2 && command.name.equals("mute", ignoreCase = true)) {
            return listOf("10m", "1h", "1d")
        }
        return emptyList()
    }
}
