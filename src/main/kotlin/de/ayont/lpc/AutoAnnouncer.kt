package de.ayont.lpc

import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.scheduler.BukkitRunnable
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

class AutoAnnouncer(private val plugin: LPC) : BukkitRunnable() {

    private val miniMessage = MiniMessage.miniMessage()
    private val currentIndex = AtomicInteger(0)
    private val random = Random()

    override fun run() {
        if (!plugin.config.getBoolean("announcer.enabled", false)) {
            return
        }

        val messages = plugin.config.getStringList("announcer.messages")
        if (messages.isEmpty()) {
            return
        }

        val strategy = plugin.config.getString("announcer.selection-strategy", "ordered")?.lowercase() ?: "ordered"

        val size = messages.size
        val messageToSend = when (strategy) {
            "random" -> messages[random.nextInt(size)]
            "reverse" -> {
                var revIdx = currentIndex.decrementAndGet()
                if (revIdx < 0 || revIdx >= size) {
                    revIdx = size - 1
                    currentIndex.set(revIdx)
                }
                messages[revIdx]
            }
            else -> {
                var ordIdx = currentIndex.getAndIncrement()
                if (ordIdx < 0 || ordIdx >= size) {
                    ordIdx = 0
                    currentIndex.set(1)
                }
                messages[ordIdx]
            }
        }

        // Broadcast to all players using Adventure
        plugin.adventure?.all()?.sendMessage(miniMessage.deserialize(messageToSend))
    }
}
