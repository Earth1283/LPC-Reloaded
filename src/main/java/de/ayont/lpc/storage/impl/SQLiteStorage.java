package de.ayont.lpc.storage.impl;

import de.ayont.lpc.LPC;
import de.ayont.lpc.storage.Storage;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
            
            try (PreparedStatement stmt = connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS lpc_channels (uuid VARCHAR(36) PRIMARY KEY, channel VARCHAR(64));")) {
                stmt.execute();
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
}
