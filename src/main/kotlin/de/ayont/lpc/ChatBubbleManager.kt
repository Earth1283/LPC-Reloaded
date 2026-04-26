package de.ayont.lpc

import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.entity.Display
import org.bukkit.entity.Player
import org.bukkit.entity.TextDisplay
import java.util.*

class ChatBubbleManager(private val plugin: LPC) {

    private val activeBubbles = mutableMapOf<UUID, TextDisplay>()

    fun spawnBubble(player: Player, message: String) {
        if (!plugin.config.getBoolean("chat-bubbles.enabled", true)) {
            return
        }

        if (plugin.config.getString("chat-bubbles.behavior", "replace").equals("replace", ignoreCase = true)) {
            removeBubble(player.uniqueId)
        }

        val heightOffset = plugin.config.getDouble("chat-bubbles.height-offset", 2.8)
        val loc = player.location

        val textDisplay = player.world.spawn(loc, TextDisplay::class.java) { display ->
            display.text = message
            display.billboard = Display.Billboard.valueOf(plugin.config.getString("chat-bubbles.billboard", "CENTER")!!.uppercase())
            display.isShadowed = plugin.config.getBoolean("chat-bubbles.shadow", true)
            display.isSeeThrough = plugin.config.getBoolean("chat-bubbles.see-through", false)
            
            val wrapWidth = plugin.config.getInt("chat-bubbles.text-wrap-width", 200)
            display.lineWidth = wrapWidth
            display.brightness = Display.Brightness(15, 15)
            display.interpolationDuration = 1
            display.interpolationDelay = 0

            val colorStr = plugin.config.getString("chat-bubbles.background-color", "0,0,0,100")
            try {
                val parts = colorStr!!.split(",")
                if (parts.size == 4) {
                    val r = parts[0].trim().toInt()
                    val g = parts[1].trim().toInt()
                    val b = parts[2].trim().toInt()
                    val a = parts[3].trim().toInt()
                    display.backgroundColor = Color.fromARGB(a, r, g, b)
                }
            } catch (ignored: Exception) {}

            val scale = plugin.config.getDouble("chat-bubbles.scale", 1.0).toFloat()
            val transformation = display.transformation
            transformation.scale.set(scale, scale, scale)
            transformation.translation.set(0f, heightOffset.toFloat(), 0f)
            display.transformation = transformation
        }

        player.addPassenger(textDisplay)
        activeBubbles[player.uniqueId] = textDisplay

        if (plugin.isPaper) {
            val seeSelf = plugin.config.getBoolean("chat-bubbles.see-self", true)
            val selfEnabled = plugin.playerSettingsManager.isChatBubblesEnabled(player.uniqueId)
            
            if (!seeSelf || !selfEnabled) {
                player.hideEntity(plugin, textDisplay)
            } else {
                player.showEntity(plugin, textDisplay)
            }
            
            for (p in Bukkit.getOnlinePlayers()) {
                if (p == player) continue
                if (!plugin.playerSettingsManager.isChatBubblesEnabled(p.uniqueId)) {
                    p.hideEntity(plugin, textDisplay)
                } else {
                    p.showEntity(plugin, textDisplay)
                }
            }
        }

        val duration = plugin.config.getDouble("chat-bubbles.duration", 5.0)
        Bukkit.getScheduler().runTaskLater(plugin, Runnable {
            if (activeBubbles[player.uniqueId] == textDisplay) {
                removeBubble(player.uniqueId)
            }
        }, (duration * 20).toLong())
    }

    fun removeBubble(playerUUID: UUID) {
        val display = activeBubbles.remove(playerUUID)
        if (display != null && display.isValid) {
            display.remove()
        }
    }

    fun updateVisibilityForPlayer(player: Player) {
        if (!plugin.isPaper) return
        
        val enabled = plugin.playerSettingsManager.isChatBubblesEnabled(player.uniqueId)
        for (display in activeBubbles.values) {
            if (enabled) {
                player.showEntity(plugin, display)
            } else {
                player.hideEntity(plugin, display)
            }
        }
    }
}
