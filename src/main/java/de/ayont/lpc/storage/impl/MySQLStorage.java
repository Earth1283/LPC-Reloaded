package de.ayont.lpc.storage.impl;

import de.ayont.lpc.LPC;
import de.ayont.lpc.moderation.Mute;
import de.ayont.lpc.moderation.Warning;
import de.ayont.lpc.storage.Storage;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MySQLStorage implements Storage {

    private final LPC plugin;
    private final String host;
    private final int port;
    private final String database;
    private final String username;
    private final String password;
    private final String tablePrefix;
    private Connection connection;

    public MySQLStorage(LPC plugin, String host, int port, String database, String username, String password, String tablePrefix) {
        this.plugin = plugin;
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
        this.tablePrefix = tablePrefix;
    }

    @Override
    public void init() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&autoReconnect=true";
            connection = DriverManager.getConnection(url, username, password);
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("CREATE TABLE IF NOT EXISTS " + tablePrefix + "channels (uuid VARCHAR(36) PRIMARY KEY, channel VARCHAR(64));");
                stmt.execute("CREATE TABLE IF NOT EXISTS " + tablePrefix + "mutes (uuid VARCHAR(36) PRIMARY KEY, sender VARCHAR(36), reason TEXT, expiry LONG);");
                stmt.execute("CREATE TABLE IF NOT EXISTS " + tablePrefix + "warnings (id INTEGER PRIMARY KEY AUTO_INCREMENT, uuid VARCHAR(36), sender VARCHAR(36), reason TEXT, timestamp LONG);");
                stmt.execute("CREATE TABLE IF NOT EXISTS " + tablePrefix + "bios (uuid VARCHAR(36) PRIMARY KEY, bio TEXT);");
                stmt.execute("CREATE TABLE IF NOT EXISTS " + tablePrefix + "slowmodes (uuid VARCHAR(36) PRIMARY KEY, seconds DOUBLE);");
            }
        } catch (ClassNotFoundException | SQLException e) {
            plugin.getLogger().severe("Could not initialize MySQL storage: " + e.getMessage());
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
                "INSERT INTO " + tablePrefix + "channels (uuid, channel) VALUES (?, ?) ON DUPLICATE KEY UPDATE channel = VALUES(channel);")) {
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

        try (PreparedStatement stmt = connection.prepareStatement("SELECT channel FROM " + tablePrefix + "channels WHERE uuid = ?;")) {
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
                "INSERT INTO " + tablePrefix + "mutes (uuid, sender, reason, expiry) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE sender=VALUES(sender), reason=VALUES(reason), expiry=VALUES(expiry);")) {
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
        try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM " + tablePrefix + "mutes WHERE uuid = ?;")) {
            stmt.setString(1, uuid.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Mute loadMute(UUID uuid) {
        if (connection == null) return null;
        try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM " + tablePrefix + "mutes WHERE uuid = ?;")) {
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
                "INSERT INTO " + tablePrefix + "warnings (uuid, sender, reason, timestamp) VALUES (?, ?, ?, ?);")) {
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
        try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM " + tablePrefix + "warnings WHERE id = ?;")) {
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
        try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM " + tablePrefix + "warnings WHERE uuid = ? ORDER BY timestamp DESC;")) {
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
                "INSERT INTO " + tablePrefix + "bios (uuid, bio) VALUES (?, ?) ON DUPLICATE KEY UPDATE bio=VALUES(bio);")) {
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
        try (PreparedStatement stmt = connection.prepareStatement("SELECT bio FROM " + tablePrefix + "bios WHERE uuid = ?;")) {
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
                "INSERT INTO " + tablePrefix + "slowmodes (uuid, seconds) VALUES (?, ?) ON DUPLICATE KEY UPDATE seconds=VALUES(seconds);")) {
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
        try (PreparedStatement stmt = connection.prepareStatement("SELECT seconds FROM " + tablePrefix + "slowmodes WHERE uuid = ?;")) {
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
