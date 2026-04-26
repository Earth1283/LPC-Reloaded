package de.ayont.lpc.managers

import java.util.UUID

class PlayerSettingsManager {
    private val chatBubblesDisabled = mutableSetOf<UUID>()
    private val ignoredPlayers = mutableMapOf<UUID, MutableSet<UUID>>()

    fun isChatBubblesEnabled(uuid: UUID): Boolean {
        return !chatBubblesDisabled.contains(uuid)
    }

    fun setChatBubblesEnabled(uuid: UUID, enabled: Boolean) {
        if (enabled) {
            chatBubblesDisabled.remove(uuid)
        } else {
            chatBubblesDisabled.add(uuid)
        }
    }

    fun ignorePlayer(ignorer: UUID, target: UUID) {
        ignoredPlayers.computeIfAbsent(ignorer) { mutableSetOf() }.add(target)
    }

    fun unignorePlayer(ignorer: UUID, target: UUID) {
        ignoredPlayers[ignorer]?.let {
            it.remove(target)
            if (it.isEmpty()) {
                ignoredPlayers.remove(ignorer)
            }
        }
    }

    fun isIgnored(ignorer: UUID, target: UUID): Boolean {
        return ignoredPlayers[ignorer]?.contains(target) == true
    }
}
