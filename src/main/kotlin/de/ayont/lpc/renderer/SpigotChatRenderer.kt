package de.ayont.lpc.renderer

import de.ayont.lpc.LPC
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player

class SpigotChatRenderer(private val plugin: LPC) {
    fun render(source: Player, message: String, viewer: Audience): Component {
        return plugin.chatRendererUtil?.render(source, message, viewer, null) ?: Component.empty()
    }
}
