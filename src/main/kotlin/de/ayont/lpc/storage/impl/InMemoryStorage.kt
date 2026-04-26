package de.ayont.lpc.storage.impl

import de.ayont.lpc.moderation.Mute
import de.ayont.lpc.moderation.Warning
import de.ayont.lpc.storage.Storage
import java.util.*

class InMemoryStorage : Storage {

    private val playerChannels = mutableMapOf<UUID, String>()
    private val mutes = mutableMapOf<UUID, Mute>()
    private val warnings = mutableListOf<Warning>()
    private val bios = mutableMapOf<UUID, String>()
    private val slowmodes = mutableMapOf<UUID, Double>()
    private var nextWarningId = 1

    override fun init() {}

    override fun shutdown() {}

    override fun save(uuid: UUID, channel: String) {
        playerChannels[uuid] = channel
    }

    override fun load(uuid: UUID): String? {
        return playerChannels[uuid]
    }

    override fun saveMute(mute: Mute) {
        mutes[mute.target] = mute
    }

    override fun removeMute(uuid: UUID) {
        mutes.remove(uuid)
    }

    override fun loadMute(uuid: UUID): Mute? {
        return mutes[uuid]
    }

    override fun addWarning(warning: Warning) {
        warnings.add(Warning(nextWarningId++, warning.target, warning.sender, warning.reason, warning.timestamp))
    }

    override fun removeWarning(id: Int) {
        warnings.removeIf { it.id == id }
    }

    override fun loadWarnings(uuid: UUID): List<Warning> {
        return warnings.filter { it.target == uuid }
            .sortedByDescending { it.timestamp }
    }

    override fun saveBio(uuid: UUID, bio: String) {
        bios[uuid] = bio
    }

    override fun loadBio(uuid: UUID): String? {
        return bios[uuid]
    }

    override fun setSlowmode(uuid: UUID, seconds: Double) {
        slowmodes[uuid] = seconds
    }

    override fun getSlowmode(uuid: UUID): Double {
        return slowmodes.getOrDefault(uuid, 0.0)
    }
}
