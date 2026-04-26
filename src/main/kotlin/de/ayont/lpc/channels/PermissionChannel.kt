package de.ayont.lpc.channels

import de.ayont.lpc.LPC
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player

class PermissionChannel(plugin: LPC, id: String, section: ConfigurationSection) : BaseChannel(plugin, id, section) {

    override fun canRead(player: Player, sender: Player): Boolean {
        return player.hasPermission(getPermission())
    }
}
