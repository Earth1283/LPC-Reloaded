package de.ayont.lpc.listener

import de.ayont.lpc.LPC
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import java.util.*

class ShortcutListener(private val plugin: LPC) : Listener {

    @EventHandler
    fun onCommand(event: PlayerCommandPreprocessEvent) {
        if (!plugin.config.getBoolean("channels.enabled", false)) return

        val msg = event.message // "/g hello" or "/g"
        val parts = msg.split(" ".toRegex(), limit = 2).toTypedArray()
        val cmd = parts[0].substring(1).lowercase(Locale.getDefault()) // "g"

        val channelManager = plugin.channelManager ?: return

        for (channel in channelManager.getChannels().values) {
            if (cmd == channel.getShortcut()) {
                event.isCancelled = true
                
                if (parts.size > 1) {
                    // Quick message: /g hello
                    channel.sendMessage(event.player, parts[1])
                } else {
                    // Switch channel: /g
                    if (channel.canJoin(event.player)) {
                        channelManager.setPlayerChannel(event.player, channel.getId())
                        plugin.adventure?.player(event.player)?.sendMessage(MiniMessage.miniMessage().deserialize(plugin.config.getString("channels.messages.switched", "<green>Switched to channel: <white>{channel}")!!
                            .replace("{channel}", channel.getName())))
                    } else {
                        plugin.adventure?.player(event.player)?.sendMessage(MiniMessage.miniMessage().deserialize(plugin.config.getString("channels.messages.no-permission-join", "<red>You do not have permission to join this channel.")!!))
                    }
                }
                return
            }
        }
    }
}
