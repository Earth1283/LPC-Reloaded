package de.ayont.lpc.managers

import java.util.UUID

class PrivateMessageManager {
    private val lastMessaged = mutableMapOf<UUID, UUID>()
    private val socialSpyEnabled = mutableSetOf<UUID>()

    fun setLastMessaged(sender: UUID, receiver: UUID) {
        lastMessaged[sender] = receiver
    }

    fun getLastMessaged(sender: UUID): UUID? {
        return lastMessaged[sender]
    }

    fun isSocialSpy(uuid: UUID): Boolean {
        return socialSpyEnabled.contains(uuid)
    }

    fun setSocialSpy(uuid: UUID, enabled: Boolean) {
        if (enabled) {
            socialSpyEnabled.add(uuid)
        } else {
            socialSpyEnabled.remove(uuid)
        }
    }
}
