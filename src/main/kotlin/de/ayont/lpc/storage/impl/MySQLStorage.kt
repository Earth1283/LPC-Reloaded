package de.ayont.lpc.storage.impl

import de.ayont.lpc.LPC
import de.ayont.lpc.moderation.Mute
import de.ayont.lpc.moderation.Warning
import de.ayont.lpc.storage.Storage
import java.sql.*
import java.util.*
import java.util.logging.Level

class MySQLStorage(
    private val plugin: LPC,
    private val host: String,
    private val port: Int,
    private val database: String,
    private val username: String,
    private val password: String,
    private val tablePrefix: String
) : Storage {

    private var connection: Connection? = null

    override fun init() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver")
            val url = "jdbc:mysql://$host:$port/$database?useSSL=false&autoReconnect=true"
            connection = DriverManager.getConnection(url, username, password)
            connection?.createStatement()?.use { stmt ->
                stmt.execute("CREATE TABLE IF NOT EXISTS ${tablePrefix}channels (uuid VARCHAR(36) PRIMARY KEY, channel VARCHAR(64));")
                stmt.execute("CREATE TABLE IF NOT EXISTS ${tablePrefix}mutes (uuid VARCHAR(36) PRIMARY KEY, sender VARCHAR(36), reason TEXT, expiry LONG);")
                stmt.execute("CREATE TABLE IF NOT EXISTS ${tablePrefix}warnings (id INTEGER PRIMARY KEY AUTO_INCREMENT, uuid VARCHAR(36), sender VARCHAR(36), reason TEXT, timestamp LONG);")
                stmt.execute("CREATE TABLE IF NOT EXISTS ${tablePrefix}bios (uuid VARCHAR(36) PRIMARY KEY, bio TEXT);")
                stmt.execute("CREATE TABLE IF NOT EXISTS ${tablePrefix}slowmodes (uuid VARCHAR(36) PRIMARY KEY, seconds DOUBLE);")
            }
        } catch (e: Exception) {
            plugin.logger.log(Level.SEVERE, "Could not initialize MySQL storage: ${e.message}")
        }
    }

    override fun shutdown() {
        try {
            if (connection != null && !connection!!.isClosed) {
                connection!!.close()
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

    override fun save(uuid: UUID, channel: String) {
        val conn = connection ?: return
        try {
            conn.prepareStatement(
                "INSERT INTO ${tablePrefix}channels (uuid, channel) VALUES (?, ?) ON DUPLICATE KEY UPDATE channel = VALUES(channel);"
            ).use { stmt ->
                stmt.setString(1, uuid.toString())
                stmt.setString(2, channel)
                stmt.executeUpdate()
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

    override fun load(uuid: UUID): String? {
        val conn = connection ?: return null
        try {
            conn.prepareStatement("SELECT channel FROM ${tablePrefix}channels WHERE uuid = ?;").use { stmt ->
                stmt.setString(1, uuid.toString())
                stmt.executeQuery().use { rs ->
                    if (rs.next()) {
                        return rs.getString("channel")
                    }
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        return null
    }

    override fun saveMute(mute: Mute) {
        val conn = connection ?: return
        try {
            conn.prepareStatement(
                "INSERT INTO ${tablePrefix}mutes (uuid, sender, reason, expiry) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE sender=VALUES(sender), reason=VALUES(reason), expiry=VALUES(expiry);"
            ).use { stmt ->
                stmt.setString(1, mute.target.toString())
                stmt.setString(2, mute.sender.toString())
                stmt.setString(3, mute.reason)
                stmt.setLong(4, mute.expiry)
                stmt.executeUpdate()
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

    override fun removeMute(uuid: UUID) {
        val conn = connection ?: return
        try {
            conn.prepareStatement("DELETE FROM ${tablePrefix}mutes WHERE uuid = ?;").use { stmt ->
                stmt.setString(1, uuid.toString())
                stmt.executeUpdate()
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

    override fun loadMute(uuid: UUID): Mute? {
        val conn = connection ?: return null
        try {
            conn.prepareStatement("SELECT * FROM ${tablePrefix}mutes WHERE uuid = ?;").use { stmt ->
                stmt.setString(1, uuid.toString())
                stmt.executeQuery().use { rs ->
                    if (rs.next()) {
                        return Mute(
                            UUID.fromString(rs.getString("uuid")),
                            UUID.fromString(rs.getString("sender")),
                            rs.getString("reason"),
                            rs.getLong("expiry")
                        )
                    }
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        return null
    }

    override fun addWarning(warning: Warning) {
        val conn = connection ?: return
        try {
            conn.prepareStatement(
                "INSERT INTO ${tablePrefix}warnings (uuid, sender, reason, timestamp) VALUES (?, ?, ?, ?);"
            ).use { stmt ->
                stmt.setString(1, warning.target.toString())
                stmt.setString(2, warning.sender.toString())
                stmt.setString(3, warning.reason)
                stmt.setLong(4, warning.timestamp)
                stmt.executeUpdate()
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

    override fun removeWarning(id: Int) {
        val conn = connection ?: return
        try {
            conn.prepareStatement("DELETE FROM ${tablePrefix}warnings WHERE id = ?;").use { stmt ->
                stmt.setInt(1, id)
                stmt.executeUpdate()
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

    override fun loadWarnings(uuid: UUID): List<Warning> {
        val warnings = mutableListOf<Warning>()
        val conn = connection ?: return warnings
        try {
            conn.prepareStatement("SELECT * FROM ${tablePrefix}warnings WHERE uuid = ? ORDER BY timestamp DESC;").use { stmt ->
                stmt.setString(1, uuid.toString())
                stmt.executeQuery().use { rs ->
                    while (rs.next()) {
                        warnings.add(
                            Warning(
                                rs.getInt("id"),
                                UUID.fromString(rs.getString("uuid")),
                                UUID.fromString(rs.getString("sender")),
                                rs.getString("reason"),
                                rs.getLong("timestamp")
                            )
                        )
                    }
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        return warnings
    }

    override fun saveBio(uuid: UUID, bio: String) {
        val conn = connection ?: return
        try {
            conn.prepareStatement(
                "INSERT INTO ${tablePrefix}bios (uuid, bio) VALUES (?, ?) ON DUPLICATE KEY UPDATE bio=VALUES(bio);"
            ).use { stmt ->
                stmt.setString(1, uuid.toString())
                stmt.setString(2, bio)
                stmt.executeUpdate()
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

    override fun loadBio(uuid: UUID): String? {
        val conn = connection ?: return null
        try {
            conn.prepareStatement("SELECT bio FROM ${tablePrefix}bios WHERE uuid = ?;").use { stmt ->
                stmt.setString(1, uuid.toString())
                stmt.executeQuery().use { rs ->
                    if (rs.next()) {
                        return rs.getString("bio")
                    }
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        return null
    }

    override fun setSlowmode(uuid: UUID, seconds: Double) {
        val conn = connection ?: return
        try {
            conn.prepareStatement(
                "INSERT INTO ${tablePrefix}slowmodes (uuid, seconds) VALUES (?, ?) ON DUPLICATE KEY UPDATE seconds=VALUES(seconds);"
            ).use { stmt ->
                stmt.setString(1, uuid.toString())
                stmt.setDouble(2, seconds)
                stmt.executeUpdate()
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

    override fun getSlowmode(uuid: UUID): Double {
        val conn = connection ?: return 0.0
        try {
            conn.prepareStatement("SELECT seconds FROM ${tablePrefix}slowmodes WHERE uuid = ?;").use { stmt ->
                stmt.setString(1, uuid.toString())
                stmt.executeQuery().use { rs ->
                    if (rs.next()) {
                        return rs.getDouble("seconds")
                    }
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        return 0.0
    }
}
