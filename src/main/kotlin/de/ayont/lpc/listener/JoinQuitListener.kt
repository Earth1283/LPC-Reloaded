package de.ayont.lpc.listener

import de.ayont.lpc.LPC
import net.kyori.adventure.text.minimessage.MiniMessage
import net.luckperms.api.LuckPermsProvider
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class JoinQuitListener(private val plugin: LPC) : Listener {

    private val miniMessage = MiniMessage.miniMessage()
    private val luckPerms = LuckPermsProvider.get()

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        if (plugin.config.getBoolean("channels.enabled", false)) {
            plugin.channelManager?.loadPlayerChannel(event.player)
        }

        if (!plugin.config.getBoolean("join-quit-messages.enabled", false)) {
            return
        }

        val player = event.player
        val format = if (!player.hasPlayedBefore()) {
            plugin.config.getString("join-quit-messages.first-join")
        } else {
            plugin.config.getString("join-quit-messages.join")
        }

        if (format == null || format.isEmpty()) {
            return
        }

        val message = miniMessage.deserialize(replacePlaceholders(format, player))
        event.joinMessage = LPC.legacySerializer.serialize(message)
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        plugin.chatBubbleManager?.removeBubble(event.player.uniqueId)

        if (!plugin.config.getBoolean("join-quit-messages.enabled", false)) {
            return
        }

        val player = event.player
        val format = plugin.config.getString("join-quit-messages.quit")

        if (format == null || format.isEmpty()) {
            return
        }

        val message = miniMessage.deserialize(replacePlaceholders(format, player))
        event.quitMessage = LPC.legacySerializer.serialize(message)
    }

    private fun replacePlaceholders(format: String, player: Player): String {
        val metaData = luckPerms.getPlayerAdapter(Player::class.java).getMetaData(player)
        return format
            .replace("{name}", player.name)
            .replace("{prefix}", metaData.prefix ?: "")
            .replace("{suffix}", metaData.suffix ?: "")
            .replace("{world}", player.world.name)
    }
}
