package de.ayont.lpc.storage

import de.ayont.lpc.moderation.Mute
import de.ayont.lpc.moderation.Warning
import java.util.UUID

interface Storage {
    fun init()
    fun shutdown()
    fun save(uuid: UUID, channel: String)
    fun load(uuid: UUID): String?

    // Mutes
    fun saveMute(mute: Mute)
    fun removeMute(uuid: UUID)
    fun loadMute(uuid: UUID): Mute?

    // Warnings
    fun addWarning(warning: Warning)
    fun removeWarning(id: Int)
    fun loadWarnings(uuid: UUID): List<Warning>

    // Profiles
    fun saveBio(uuid: UUID, bio: String)
    fun loadBio(uuid: UUID): String?

    // Slowmode
    fun setSlowmode(uuid: UUID, seconds: Double)
    fun getSlowmode(uuid: UUID): Double
}
