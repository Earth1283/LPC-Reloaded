package de.ayont.lpc.storage.impl;

import de.ayont.lpc.LPC;
import de.ayont.lpc.moderation.Mute;
import de.ayont.lpc.moderation.Warning;
import de.ayont.lpc.storage.Storage;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SQLiteStorage implements Storage {

    private final LPC plugin;
    private final String fileName;
    private Connection connection;

    public SQLiteStorage(LPC plugin, String fileName) {
        this.plugin = plugin;
        this.fileName = fileName;
    }

    @Override
    public void init() {
        try {
            // Load the SQLite driver
            Class.forName("org.sqlite.JDBC");
            
            File dataFolder = plugin.getDataFolder();
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }
            
            File dbFile = new File(dataFolder, fileName);
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
            
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("CREATE TABLE IF NOT EXISTS lpc_channels (uuid VARCHAR(36) PRIMARY KEY, channel VARCHAR(64));");
                stmt.execute("CREATE TABLE IF NOT EXISTS lpc_mutes (uuid VARCHAR(36) PRIMARY KEY, sender VARCHAR(36), reason TEXT, expiry LONG);");
                stmt.execute("CREATE TABLE IF NOT EXISTS lpc_warnings (id INTEGER PRIMARY KEY AUTOINCREMENT, uuid VARCHAR(36), sender VARCHAR(36), reason TEXT, timestamp LONG);");
                stmt.execute("CREATE TABLE IF NOT EXISTS lpc_bios (uuid VARCHAR(36) PRIMARY KEY, bio TEXT);");
                stmt.execute("CREATE TABLE IF NOT EXISTS lpc_slowmodes (uuid VARCHAR(36) PRIMARY KEY, seconds DOUBLE);");
            }
        } catch (ClassNotFoundException | SQLException e) {
            plugin.getLogger().severe("Could not initialize SQLite storage: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void shutdown() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void save(UUID uuid, String channel) {
        if (connection == null) return;
        
        try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT OR REPLACE INTO lpc_channels (uuid, channel) VALUES (?, ?);")) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, channel);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String load(UUID uuid) {
        if (connection == null) return null;

        try (PreparedStatement stmt = connection.prepareStatement("SELECT channel FROM lpc_channels WHERE uuid = ?;")) {
            stmt.setString(1, uuid.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("channel");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void saveMute(Mute mute) {
        if (connection == null) return;
        try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT OR REPLACE INTO lpc_mutes (uuid, sender, reason, expiry) VALUES (?, ?, ?, ?);")) {
            stmt.setString(1, mute.getTarget().toString());
            stmt.setString(2, mute.getSender().toString());
            stmt.setString(3, mute.getReason());
            stmt.setLong(4, mute.getExpiry());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeMute(UUID uuid) {
        if (connection == null) return;
        try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM lpc_mutes WHERE uuid = ?;")) {
            stmt.setString(1, uuid.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Mute loadMute(UUID uuid) {
        if (connection == null) return null;
        try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM lpc_mutes WHERE uuid = ?;")) {
            stmt.setString(1, uuid.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Mute(
                            UUID.fromString(rs.getString("uuid")),
                            UUID.fromString(rs.getString("sender")),
                            rs.getString("reason"),
                            rs.getLong("expiry")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void addWarning(Warning warning) {
        if (connection == null) return;
        try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO lpc_warnings (uuid, sender, reason, timestamp) VALUES (?, ?, ?, ?);")) {
            stmt.setString(1, warning.getTarget().toString());
            stmt.setString(2, warning.getSender().toString());
            stmt.setString(3, warning.getReason());
            stmt.setLong(4, warning.getTimestamp());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeWarning(int id) {
        if (connection == null) return;
        try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM lpc_warnings WHERE id = ?;")) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Warning> loadWarnings(UUID uuid) {
        List<Warning> warnings = new ArrayList<>();
        if (connection == null) return warnings;
        try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM lpc_warnings WHERE uuid = ? ORDER BY timestamp DESC;")) {
            stmt.setString(1, uuid.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    warnings.add(new Warning(
                            rs.getInt("id"),
                            UUID.fromString(rs.getString("uuid")),
                            UUID.fromString(rs.getString("sender")),
                            rs.getString("reason"),
                            rs.getLong("timestamp")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return warnings;
    }

    @Override
    public void saveBio(UUID uuid, String bio) {
        if (connection == null) return;
        try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT OR REPLACE INTO lpc_bios (uuid, bio) VALUES (?, ?);")) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, bio);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String loadBio(UUID uuid) {
        if (connection == null) return null;
        try (PreparedStatement stmt = connection.prepareStatement("SELECT bio FROM lpc_bios WHERE uuid = ?;")) {
            stmt.setString(1, uuid.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("bio");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void setSlowmode(UUID uuid, double seconds) {
        if (connection == null) return;
        try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT OR REPLACE INTO lpc_slowmodes (uuid, seconds) VALUES (?, ?);")) {
            stmt.setString(1, uuid.toString());
            stmt.setDouble(2, seconds);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public double getSlowmode(UUID uuid) {
        if (connection == null) return 0;
        try (PreparedStatement stmt = connection.prepareStatement("SELECT seconds FROM lpc_slowmodes WHERE uuid = ?;")) {
            stmt.setString(1, uuid.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("seconds");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
