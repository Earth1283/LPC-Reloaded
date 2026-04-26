package de.ayont.lpc.renderer

import de.ayont.lpc.LPC
import io.papermc.paper.chat.ChatRenderer
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.entity.Player

class LPCChatRenderer(private val plugin: LPC) : ChatRenderer {

    override fun render(source: Player, sourceDisplayName: Component, message: Component, viewer: Audience): Component {
        val plainMessage = PlainTextComponentSerializer.plainText().serialize(message)
        return plugin.chatRendererUtil?.render(source, plainMessage, viewer, null) ?: Component.empty()
    }
}
