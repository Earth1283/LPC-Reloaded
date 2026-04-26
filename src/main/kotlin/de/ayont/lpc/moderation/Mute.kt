package de.ayont.lpc.moderation

import java.util.UUID

data class Mute(
    val target: UUID,
    val sender: UUID,
    val reason: String,
    val expiry: Long
) {
    val isPermanent: Boolean
        get() = expiry == -1L

    fun hasExpired(): Boolean {
        return !isPermanent && System.currentTimeMillis() > expiry
    }
}
