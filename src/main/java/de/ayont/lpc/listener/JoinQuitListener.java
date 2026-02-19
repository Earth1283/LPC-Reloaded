package de.ayont.lpc.listener;

import de.ayont.lpc.LPC;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class JoinQuitListener implements Listener {

    private final LPC plugin;
    private final MiniMessage miniMessage;
    private final LuckPerms luckPerms;

    public JoinQuitListener(LPC plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
        this.luckPerms = LuckPermsProvider.get();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (plugin.getConfig().getBoolean("channels.enabled", false)) {
            plugin.getChannelManager().loadPlayerChannel(event.getPlayer());
        }

        if (!plugin.getConfig().getBoolean("join-quit-messages.enabled", false)) {
            return;
        }

        Player player = event.getPlayer();
        String format;
        if (!player.hasPlayedBefore()) {
            format = plugin.getConfig().getString("join-quit-messages.first-join");
        } else {
            format = plugin.getConfig().getString("join-quit-messages.join");
        }

        if (format == null || format.isEmpty()) {
            // Don't change anything if not configured or empty
            return;
        }

        Component message = miniMessage.deserialize(replacePlaceholders(format, player));

        event.setJoinMessage(LPC.getLegacySerializer().serialize(message));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.getChatBubbleManager().removeBubble(event.getPlayer().getUniqueId());

        if (!plugin.getConfig().getBoolean("join-quit-messages.enabled", false)) {
            return;
        }

        Player player = event.getPlayer();
        String format = plugin.getConfig().getString("join-quit-messages.quit");

        if (format == null || format.isEmpty()) {
            return;
        }

        Component message = miniMessage.deserialize(replacePlaceholders(format, player));

        event.setQuitMessage(LPC.getLegacySerializer().serialize(message));
    }

    private String replacePlaceholders(String format, Player player) {
        CachedMetaData metaData = this.luckPerms.getPlayerAdapter(Player.class).getMetaData(player);
        return format
                .replace("{name}", player.getName())
                .replace("{prefix}", metaData.getPrefix() != null ? metaData.getPrefix() : "")
                .replace("{suffix}", metaData.getSuffix() != null ? metaData.getSuffix() : "")
                .replace("{world}", player.getWorld().getName());
    }
}
