package de.ayont.lpc.storage.impl

import de.ayont.lpc.LPC
import de.ayont.lpc.moderation.Mute
import de.ayont.lpc.moderation.Warning
import de.ayont.lpc.storage.Storage
import java.io.File
import java.sql.*
import java.util.*
import java.util.logging.Level

class SQLiteStorage(private val plugin: LPC, private val fileName: String) : Storage {

    private var connection: Connection? = null

    override fun init() {
        try {
            Class.forName("org.sqlite.JDBC")
            
            val dataFolder = plugin.dataFolder
            if (!dataFolder.exists()) {
                dataFolder.mkdirs()
            }
            
            val dbFile = File(dataFolder, fileName)
            connection = DriverManager.getConnection("jdbc:sqlite:${dbFile.absolutePath}")
            
            connection?.createStatement()?.use { stmt ->
                stmt.execute("CREATE TABLE IF NOT EXISTS lpc_channels (uuid VARCHAR(36) PRIMARY KEY, channel VARCHAR(64));")
                stmt.execute("CREATE TABLE IF NOT EXISTS lpc_mutes (uuid VARCHAR(36) PRIMARY KEY, sender VARCHAR(36), reason TEXT, expiry LONG);")
                stmt.execute("CREATE TABLE IF NOT EXISTS lpc_warnings (id INTEGER PRIMARY KEY AUTOINCREMENT, uuid VARCHAR(36), sender VARCHAR(36), reason TEXT, timestamp LONG);")
                stmt.execute("CREATE TABLE IF NOT EXISTS lpc_bios (uuid VARCHAR(36) PRIMARY KEY, bio TEXT);")
                stmt.execute("CREATE TABLE IF NOT EXISTS lpc_slowmodes (uuid VARCHAR(36) PRIMARY KEY, seconds DOUBLE);")
            }
        } catch (e: Exception) {
            plugin.logger.log(Level.SEVERE, "Could not initialize SQLite storage: ${e.message}")
            e.printStackTrace()
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
            conn.prepareStatement("INSERT OR REPLACE INTO lpc_channels (uuid, channel) VALUES (?, ?);").use { stmt ->
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
            conn.prepareStatement("SELECT channel FROM lpc_channels WHERE uuid = ?;").use { stmt ->
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
            conn.prepareStatement("INSERT OR REPLACE INTO lpc_mutes (uuid, sender, reason, expiry) VALUES (?, ?, ?, ?);").use { stmt ->
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
            conn.prepareStatement("DELETE FROM lpc_mutes WHERE uuid = ?;").use { stmt ->
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
            conn.prepareStatement("SELECT * FROM lpc_mutes WHERE uuid = ?;").use { stmt ->
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
            conn.prepareStatement("INSERT INTO lpc_warnings (uuid, sender, reason, timestamp) VALUES (?, ?, ?, ?);").use { stmt ->
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
            conn.prepareStatement("DELETE FROM lpc_warnings WHERE id = ?;").use { stmt ->
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
            conn.prepareStatement("SELECT * FROM lpc_warnings WHERE uuid = ? ORDER BY timestamp DESC;").use { stmt ->
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
            conn.prepareStatement("INSERT OR REPLACE INTO lpc_bios (uuid, bio) VALUES (?, ?);").use { stmt ->
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
            conn.prepareStatement("SELECT bio FROM lpc_bios WHERE uuid = ?;").use { stmt ->
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
            conn.prepareStatement("INSERT OR REPLACE INTO lpc_slowmodes (uuid, seconds) VALUES (?, ?);").use { stmt ->
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
            conn.prepareStatement("SELECT seconds FROM lpc_slowmodes WHERE uuid = ?;").use { stmt ->
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
