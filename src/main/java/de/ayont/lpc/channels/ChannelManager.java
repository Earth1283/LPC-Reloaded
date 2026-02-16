package de.ayont.lpc.channels;

import de.ayont.lpc.LPC;
import de.ayont.lpc.storage.Storage;
import de.ayont.lpc.storage.impl.InMemoryStorage;
import de.ayont.lpc.storage.impl.MySQLStorage;
import de.ayont.lpc.storage.impl.SQLiteStorage;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class ChannelManager {

    private final LPC plugin;
    private final Map<String, Channel> channels = new HashMap<>();
    private final Map<UUID, String> playerChannels = new HashMap<>();
    private String defaultChannel;
    private Storage storage;

    public ChannelManager(LPC plugin) {
        this.plugin = plugin;
    }

    public void init() {
        loadChannels();
        initStorage();
    }
    
    public void shutdown() {
        if (storage != null) {
            storage.shutdown();
        }
    }

    private void initStorage() {
        String type = plugin.getConfig().getString("channels.storage.type", "MEMORY").toUpperCase();
        try {
            switch (type) {
                case "SQLITE":
                    storage = new SQLiteStorage(plugin, plugin.getConfig().getString("channels.storage.file", "channels.db"));
                    break;
                case "MYSQL":
                    storage = new MySQLStorage(plugin,
                            plugin.getConfig().getString("channels.storage.mysql.host", "localhost"),
                            plugin.getConfig().getInt("channels.storage.mysql.port", 3306),
                            plugin.getConfig().getString("channels.storage.mysql.database", "lpc"),
                            plugin.getConfig().getString("channels.storage.mysql.username", "root"),
                            plugin.getConfig().getString("channels.storage.mysql.password", ""),
                            plugin.getConfig().getString("channels.storage.mysql.table-prefix", "lpc_"));
                    break;
                case "MEMORY":
                default:
                    storage = new InMemoryStorage();
                    break;
            }
            storage.init();
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to initialize storage: " + type, e);
            storage = new InMemoryStorage(); // Fallback
        }
    }

    public void loadChannels() {
        channels.clear();
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("channels.list");
        if (section == null) return;

        defaultChannel = plugin.getConfig().getString("channels.default-channel", "global");

        for (String key : section.getKeys(false)) {
            ConfigurationSection channelSection = section.getConfigurationSection(key);
            if (channelSection == null) continue;

            String type = channelSection.getString("type", "global").toLowerCase();
            Channel channel;

            switch (type) {
                case "permission":
                    channel = new PermissionChannel(plugin, key, channelSection);
                    break;
                case "range":
                    channel = new RangeChannel(plugin, key, channelSection);
                    break;
                case "global":
                default:
                    channel = new GlobalChannel(plugin, key, channelSection);
                    break;
            }
            channels.put(key, channel);
            plugin.getLogger().info("Loaded channel: " + key);
        }
    }

    public Channel getChannel(String id) {
        return channels.get(id);
    }
    
    public void loadPlayerChannel(Player player) {
        if (storage == null) return;
        String channelId = storage.load(player.getUniqueId());
        if (channelId != null && channels.containsKey(channelId)) {
            playerChannels.put(player.getUniqueId(), channelId);
        }
    }

    public Channel getPlayerChannel(Player player) {
        String channelId = playerChannels.getOrDefault(player.getUniqueId(), defaultChannel);
        return channels.getOrDefault(channelId, channels.get(defaultChannel));
    }

    public void setPlayerChannel(Player player, String channelId) {
        if (channels.containsKey(channelId)) {
            playerChannels.put(player.getUniqueId(), channelId);
            if (storage != null) {
                plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                   storage.save(player.getUniqueId(), channelId); 
                });
            }
        }
    }
    
    public Map<String, Channel> getChannels() {
        return channels;
    }

    public void sendMessage(Player sender, String message) {
        // Check for symbol prefix
        for (Channel channel : channels.values()) {
            if (channel.getSymbol() != null && !channel.getSymbol().isEmpty() && message.startsWith(channel.getSymbol())) {
                if (channel.canJoin(sender)) {
                    String finalMessage = message.substring(channel.getSymbol().length()).trim();
                    if (!finalMessage.isEmpty()) {
                        channel.sendMessage(sender, finalMessage);
                        return;
                    }
                }
            }
        }

        // Send to current channel
        Channel current = getPlayerChannel(sender);
        if (current != null) {
            current.sendMessage(sender, message);
        } else {
            sender.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize("<red>No channel available."));
        }
    }
}
