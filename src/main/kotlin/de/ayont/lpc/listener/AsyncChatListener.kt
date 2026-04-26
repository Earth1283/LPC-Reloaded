package de.ayont.lpc.listener

import de.ayont.lpc.LPC
import de.ayont.lpc.renderer.LPCChatRenderer
import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextReplacementConfig
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import java.util.regex.Pattern

class AsyncChatListener(private val plugin: LPC) : Listener {

    private val lpcChatRenderer = LPCChatRenderer(plugin)

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

    @EventHandler
    fun onChat(event: AsyncChatEvent) {
        val player = event.player
        val plainMessage = PlainTextComponentSerializer.plainText().serialize(event.message())

        // Mute Check
        val mute = plugin.moderationManager?.getActiveMute(player.uniqueId)
        if (mute != null && !player.hasPermission("lpc.mute.bypass")) {
            event.isCancelled = true
            val timeStr = if (mute.isPermanent) "Permanent" else formatTimeLeft(mute.expiry - System.currentTimeMillis())
            val msgKey = if (mute.isPermanent) "mutes.messages.permanent" else "mutes.messages.muted"
            val muteMsg = plugin.config.getString(msgKey, "<red>You are muted! Reason: {reason}")!!
                .replace("{reason}", mute.reason)
                .replace("{time}", timeStr)
            plugin.adventure?.player(player)?.sendMessage(MiniMessage.miniMessage().deserialize(muteMsg))
            return
        }

        // Staff Chat
        if (plugin.config.getBoolean("staff-chat.enabled", false)) {
            val prefix = plugin.config.getString("staff-chat.prefix", "#")!!
            if (plainMessage.startsWith(prefix) && player.hasPermission("lpc.staffchat")) {
                event.isCancelled = true
                val msg = plainMessage.substring(prefix.length).trim()
                if (msg.isNotEmpty()) {
                    val format = plugin.config.getString("staff-chat.format", "<red>[Staff] {name}: <white>{message}")!!
                        .replace("{name}", player.name)
                        .replace("{message}", msg)
                    val component = MiniMessage.miniMessage().deserialize(format)
                    
                    for (p in Bukkit.getOnlinePlayers()) {
                        if (p.hasPermission("lpc.staffchat")) {
                            plugin.adventure?.player(p)?.sendMessage(component)
                        }
                    }
                    plugin.adventure?.console()?.sendMessage(component)
                }
                return
            }
        }

        // Chat Filter & Moderation
        if (plugin.config.getBoolean("filter.enabled", false) && !player.hasPermission("lpc.filter.bypass")) {
            // Cooldown / Slowmode
            if (plugin.moderationManager?.canChat(player) == false) {
                event.isCancelled = true
                val denyMsg = plugin.config.getString("filter.deny-message", "<red>Please wait {time}s.")!!
                val timeLeft = plugin.moderationManager?.getTimeLeft(player.uniqueId) ?: 0.0
                plugin.adventure?.player(player)?.sendMessage(MiniMessage.miniMessage().deserialize(denyMsg.replace("{time}", String.format("%.1f", timeLeft))))
                return
            }

            // Caps Limit
            if (plugin.config.getBoolean("filter.caps-limit.enabled", false)) {
                val minLength = plugin.config.getInt("filter.caps-limit.min-length", 5)
                if (plainMessage.length >= minLength) {
                    val percentage = plugin.config.getDouble("filter.caps-limit.percentage", 70.0)
                    var upperCount = 0
                    for (c in plainMessage.toCharArray()) {
                        if (Character.isUpperCase(c)) upperCount++
                    }
                    if ((upperCount.toDouble() / plainMessage.length) * 100 > percentage) {
                        event.isCancelled = true
                        plugin.adventure?.player(player)?.sendMessage(MiniMessage.miniMessage().deserialize(plugin.config.getString("filter.caps-limit.deny-message", "<red>Too many caps!")!!))
                        return
                    }
                }
            }

            // Anti-Discord
            if (plugin.config.getBoolean("filter.anti-discord.enabled", false)) {
                val regex = plugin.config.getString("filter.anti-discord.regex", "discord(?:\\.gg|app\\.com\\/invite|\\.com\\/invite)\\/[a-zA-Z0-9]+")!!
                if (Pattern.compile(regex).matcher(plainMessage).find()) {
                    event.isCancelled = true
                    plugin.adventure?.player(player)?.sendMessage(MiniMessage.miniMessage().deserialize(plugin.config.getString("filter.anti-discord.deny-message", "<red>No discord invites!")!!))
                    return
                }
            }
            
            // Anti-Link
             if (plugin.config.getBoolean("filter.anti-link.enabled", false)) {
                val regex = plugin.config.getString("filter.anti-link.regex", "(https?:\\/\\/)?(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_\\+.~#?&//=]*)")!!
                if (Pattern.compile(regex).matcher(plainMessage).find()) {
                    event.isCancelled = true
                    plugin.adventure?.player(player)?.sendMessage(MiniMessage.miniMessage().deserialize(plugin.config.getString("filter.anti-link.deny-message", "<red>No links!")!!))
                    return
                }
            }
        }
        
        plugin.moderationManager?.updateLastChat(player.uniqueId)

        // Ignore System
        if (plugin.config.getBoolean("ignore.enabled", false)) {
            event.viewers().removeIf { audience -> audience is Player && plugin.playerSettingsManager.isIgnored(audience.uniqueId, player.uniqueId) }
        }

        // Chat Bubbles
        if (plugin.config.getBoolean("chat-bubbles.enabled", true)) {
            Bukkit.getScheduler().runTask(plugin, Runnable {
                plugin.chatBubbleManager?.spawnBubble(player, plainMessage)
            })
        }
        
        // Chat Channels
        if (plugin.config.getBoolean("channels.enabled", false)) {
             event.isCancelled = true
             plugin.channelManager?.sendMessage(player, plainMessage)
             return
        }

        if(!plugin.config.getBoolean("use-item-placeholder", false) || !player.hasPermission("lpc.itemplaceholder")){
            event.renderer(lpcChatRenderer)
            return
        }

        val item = player.inventory.itemInMainHand
        val displayName = if (item.itemMeta != null && item.itemMeta.hasDisplayName()) item.itemMeta.displayName() else Component.text(item.type.toString().lowercase().replace("_", " "))
        if (item.type == Material.AIR || displayName == null) {
            event.renderer(lpcChatRenderer)
            return
        }

        event.renderer { source, sourceDisplayName, message, viewer ->
            lpcChatRenderer.render(source, sourceDisplayName, message, viewer)
                .replaceText(TextReplacementConfig.builder()
                    .match(Pattern.compile("\\[item]", Pattern.CASE_INSENSITIVE))
                    .replacement(displayName.hoverEvent(item.asHoverEvent()))
                    .build())
        }
    }
}
