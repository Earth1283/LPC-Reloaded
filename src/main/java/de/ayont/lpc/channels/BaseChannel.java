package de.ayont.lpc.channels;

import de.ayont.lpc.LPC;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public abstract class BaseChannel implements Channel {

    protected final LPC plugin;
    protected final String id;
    protected final String format;
    protected final String permission;
    protected final String shortcut;
    protected final String symbol;
    protected final MiniMessage miniMessage;
    protected final LuckPerms luckPerms;

    public BaseChannel(LPC plugin, String id, ConfigurationSection section) {
        this.plugin = plugin;
        this.id = id;
        this.format = section.getString("format", "{prefix}{name} » {message}");
        this.permission = section.getString("permission", "");
        this.shortcut = section.getString("shortcut", "");
        this.symbol = section.getString("symbol", "");
        this.miniMessage = MiniMessage.miniMessage();
        this.luckPerms = LuckPermsProvider.get();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return id;
    }

    @Override
    public String getFormat() {
        return format;
    }

    @Override
    public String getPermission() {
        return permission;
    }

    @Override
    public String getShortcut() {
        return shortcut;
    }

    @Override
    public String getSymbol() {
        return symbol;
    }

    @Override
    public boolean canJoin(Player player) {
        return permission.isEmpty() || player.hasPermission(permission);
    }

    @Override
    public boolean canRead(Player player, Player sender) {
        // By default, anyone with join permission can read.
        // Range checks will override this.
        return canJoin(player);
    }

    @Override
    public void sendMessage(Player sender, String message) {
        if (!canJoin(sender)) {
            sender.sendMessage(miniMessage.deserialize(plugin.getConfig().getString("channels.messages.no-permission-speak", "<red>You do not have permission to speak in this channel.")));
            return;
        }

        String formattedMessage = replacePlaceholders(format, sender).replace("{message}", message);
        Component component = miniMessage.deserialize(formattedMessage);
        
        // Cache legacy string for Spigot recipients to avoid redundant serialization
        String legacyMessage = null;
        if (!plugin.isPaper()) {
            legacyMessage = LPC.getLegacySerializer().serialize(component);
        }

        for (Player recipient : Bukkit.getOnlinePlayers()) {
            if (canRead(recipient, sender)) {
                if (plugin.isPaper()) {
                    recipient.sendMessage(component);
                } else {
                    recipient.sendMessage(legacyMessage);
                }
            }
        }
        
        // Log to console
        if (plugin.isPaper()) {
            plugin.getAdventure().console().sendMessage(component);
        } else {
            plugin.getLogger().info(legacyMessage != null ? legacyMessage : LPC.getLegacySerializer().serialize(component));
        }
    }

    @Override
    public void onJoin(Player player) {
        // Optional: Send "You joined X channel"
    }

    protected String replacePlaceholders(String format, Player player) {
        CachedMetaData metaData = this.luckPerms.getPlayerAdapter(Player.class).getMetaData(player);
        return format
                .replace("{name}", player.getName())
                .replace("{displayname}", player.getDisplayName()) // Spigot compat
                .replace("{world}", player.getWorld().getName())
                .replace("{prefix}", metaData.getPrefix() != null ? metaData.getPrefix() : "")
                .replace("{suffix}", metaData.getSuffix() != null ? metaData.getSuffix() : "")
                .replace("{username-color}", metaData.getMetaValue("username-color") != null ? metaData.getMetaValue("username-color") : "")
                .replace("{message-color}", metaData.getMetaValue("message-color") != null ? metaData.getMetaValue("message-color") : "");
    }
}
