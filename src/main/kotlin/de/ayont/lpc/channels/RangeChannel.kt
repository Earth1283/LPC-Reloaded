package de.ayont.lpc.channels

import de.ayont.lpc.LPC
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player

class RangeChannel(plugin: LPC, id: String, section: ConfigurationSection) : BaseChannel(plugin, id, section) {

    private val range: Double = section.getDouble("range", 100.0)

    override fun canRead(player: Player, sender: Player): Boolean {
        if (player.world != sender.world) return false
        return player.location.distanceSquared(sender.location) <= range * range
    }
}
