package de.ayont.lpc.moderation

import java.util.UUID

data class Warning(
    val id: Int,
    val target: UUID,
    val sender: UUID,
    val reason: String,
    val timestamp: Long
)
