package de.ayont.lpc.moderation

import de.ayont.lpc.LPC
import org.bukkit.entity.Player
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class ModerationManager(private val plugin: LPC) {
    private val slowmodes = ConcurrentHashMap<UUID, Long>()
    private val lastChatTimes = ConcurrentHashMap<UUID, Long>()
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm")

    fun getActiveMute(uuid: UUID): Mute? {
        if (!plugin.config.getBoolean("mutes.enabled", true)) return null
        val storage = plugin.lpcStorage ?: return null
        val mute = storage.loadMute(uuid)
        if (mute != null && mute.hasExpired()) {
            storage.removeMute(uuid)
            return null
        }
        return mute
    }

    fun mutePlayer(mute: Mute) {
        if (!plugin.config.getBoolean("mutes.enabled", true)) return
        val storage = plugin.lpcStorage ?: return
        storage.saveMute(mute)
    }

    fun unmutePlayer(uuid: UUID) {
        if (!plugin.config.getBoolean("mutes.enabled", true)) return
        val storage = plugin.lpcStorage ?: return
        storage.removeMute(uuid)
    }

    fun warnPlayer(warning: Warning) {
        if (!plugin.config.getBoolean("warnings.enabled", true)) return
        val storage = plugin.lpcStorage ?: return
        storage.addWarning(warning)
    }

    fun getWarnings(uuid: UUID): List<Warning> {
        if (!plugin.config.getBoolean("warnings.enabled", true)) return emptyList()
        val storage = plugin.lpcStorage ?: return emptyList()
        return storage.loadWarnings(uuid)
    }

    fun deleteWarning(id: Int) {
        if (!plugin.config.getBoolean("warnings.enabled", true)) return
        val storage = plugin.lpcStorage ?: return
        storage.removeWarning(id)
    }

    fun setBio(uuid: UUID, bio: String) {
        if (!plugin.config.getBoolean("profiles.enabled", true)) return
        val storage = plugin.lpcStorage ?: return
        storage.saveBio(uuid, bio)
    }

    fun getBio(uuid: UUID): String {
        if (!plugin.config.getBoolean("profiles.enabled", true)) return ""
        val storage = plugin.lpcStorage ?: return ""
        return storage.loadBio(uuid) ?: ""
    }

    fun setSlowmode(uuid: UUID, seconds: Double) {
        slowmodes[uuid] = (seconds * 1000).toLong()
        plugin.lpcStorage?.setSlowmode(uuid, seconds)
    }

    fun getSlowmode(uuid: UUID): Long {
        return slowmodes.getOrPut(uuid) {
            ((plugin.lpcStorage?.getSlowmode(uuid) ?: 0.0) * 1000).toLong()
        }
    }

    fun canChat(player: Player): Boolean {
        val now = System.currentTimeMillis()
        var slowmode = getSlowmode(player.uniqueId)

        // If no per-player slowmode, use global
        if (slowmode <= 0) {
            slowmode = (plugin.config.getDouble("filter.cooldown", 0.0) * 1000).toLong()
        }

        if (slowmode <= 0 || player.hasPermission("lpc.filter.bypass")) {
            return true
        }

        val lastChat = lastChatTimes.getOrDefault(player.uniqueId, 0L)
        return (now - lastChat) >= slowmode
    }

    fun updateLastChat(uuid: UUID) {
        lastChatTimes[uuid] = System.currentTimeMillis()
    }

    fun getTimeLeft(uuid: UUID): Double {
        val now = System.currentTimeMillis()
        var slowmode = getSlowmode(uuid)
        if (slowmode <= 0) {
            slowmode = (plugin.config.getDouble("filter.cooldown", 0.0) * 1000).toLong()
        }
        val lastChat = lastChatTimes.getOrDefault(uuid, 0L)
        return Math.max(0.0, (slowmode - (now - lastChat)) / 1000.0)
    }

    fun getInfractionsSummary(uuid: UUID, max: Int): String {
        if (!plugin.config.getBoolean("warnings.enabled", true)) return ""
        val warnings = getWarnings(uuid)
        if (warnings.isEmpty()) {
            return plugin.config.getString("profiles.no-infractions", "<gray>No recent infractions.")!!
        }

        val format = plugin.config.getString("profiles.infraction-format", "<red>• {reason} <dark_gray>({date})</dark_gray>")!!
        val sb = StringBuilder()
        var count = 0
        for (w in warnings) {
            if (count >= max) break
            sb.append(
                format
                    .replace("{reason}", w.reason)
                    .replace("{date}", dateFormat.format(Date(w.timestamp)))
            )
            if (count < Math.min(warnings.size, max) - 1) {
                sb.append("<newline>")
            }
            count++
        }
        return sb.toString()
    }
}
