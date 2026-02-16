package de.ayont.lpc.storage.impl;

import de.ayont.lpc.LPC;
import de.ayont.lpc.storage.Storage;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
            try (PreparedStatement stmt = connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS " + tablePrefix + "channels (uuid VARCHAR(36) PRIMARY KEY, channel VARCHAR(64));")) {
                stmt.execute();
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
}
