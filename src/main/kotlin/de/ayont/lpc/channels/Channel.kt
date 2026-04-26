package de.ayont.lpc.channels

import org.bukkit.entity.Player

interface Channel {

    fun getId(): String

    fun getName(): String

    fun getFormat(): String

    fun getPermission(): String

    fun getShortcut(): String

    fun getSymbol(): String

    fun canJoin(player: Player): Boolean

    fun canRead(player: Player, sender: Player): Boolean

    fun sendMessage(sender: Player, message: String)
    
    // For when the player just switches to the channel, send a confirmation message?
    fun onJoin(player: Player)
}
