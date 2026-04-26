package de.ayont.lpc.channels

import de.ayont.lpc.LPC
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*

class ChannelManager(private val plugin: LPC) {

    private val channels = mutableMapOf<String, Channel>()
    private val playerChannels = mutableMapOf<UUID, String>()
    private var defaultChannel: String = "global"

    fun init() {
        loadChannels()
    }
    
    fun shutdown() {
    }

    fun loadChannels() {
        channels.clear()
        val section = plugin.config.getConfigurationSection("channels.list") ?: return

        defaultChannel = plugin.config.getString("channels.default-channel", "global")!!

        for (key in section.getKeys(false)) {
            val channelSection = section.getConfigurationSection(key) ?: continue

            val type = channelSection.getString("type", "global")!!.lowercase()
            val channel: Channel = when (type) {
                "permission" -> PermissionChannel(plugin, key, channelSection)
                "range" -> RangeChannel(plugin, key, channelSection)
                else -> GlobalChannel(plugin, key, channelSection)
            }
            channels[key] = channel
            plugin.logger.info("Loaded channel: $key")
        }
    }

    fun getChannel(id: String): Channel? {
        return channels[id]
    }
    
    fun loadPlayerChannel(player: Player) {
        val storage = plugin.lpcStorage ?: return
        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
            val channelId = storage.load(player.uniqueId)
            if (channelId != null && channels.containsKey(channelId)) {
                Bukkit.getScheduler().runTask(plugin, Runnable {
                    playerChannels[player.uniqueId] = channelId
                })
            }
        })
    }

    fun getPlayerChannel(player: Player): Channel? {
        val channelId = playerChannels.getOrDefault(player.uniqueId, defaultChannel)
        return channels[channelId] ?: channels[defaultChannel]
    }

    fun setPlayerChannel(player: Player, channelId: String) {
        if (channels.containsKey(channelId)) {
            playerChannels[player.uniqueId] = channelId
            val storage = plugin.lpcStorage
            if (storage != null) {
                Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
                   storage.save(player.uniqueId, channelId) 
                })
            }
        }
    }
    
    fun getChannels(): Map<String, Channel> {
        return channels
    }

    fun sendMessage(sender: Player, message: String) {
        // Check for symbol prefix
        for (channel in channels.values) {
            val symbol = channel.getSymbol()
            if (symbol != null && symbol.isNotEmpty() && message.startsWith(symbol)) {
                if (channel.canJoin(sender)) {
                    val finalMessage = message.substring(symbol.length).trim()
                    if (finalMessage.isNotEmpty()) {
                        channel.sendMessage(sender, finalMessage)
                        return
                    }
                }
            }
        }

        // Send to current channel
        val current = getPlayerChannel(sender)
        if (current != null) {
            current.sendMessage(sender, message)
        } else {
            sender.sendMessage(MiniMessage.miniMessage().deserialize(plugin.config.getString("channels.messages.no-channel", "<red>No channel available.")!!))
        }
    }
}
