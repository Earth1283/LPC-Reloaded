package de.ayont.lpc.listener;

import de.ayont.lpc.LPC;
import de.ayont.lpc.channels.Channel;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.Map;

public class ShortcutListener implements Listener {

    private final LPC plugin;

    public ShortcutListener(LPC plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (!plugin.getConfig().getBoolean("channels.enabled", false)) return;

        String msg = event.getMessage(); // "/g hello" or "/g"
        String[] parts = msg.split(" ", 2);
        String cmd = parts[0].substring(1).toLowerCase(); // "g"

        for (Channel channel : plugin.getChannelManager().getChannels().values()) {
            if (cmd.equals(channel.getShortcut())) {
                event.setCancelled(true);
                
                if (parts.length > 1) {
                    // Quick message: /g hello
                    channel.sendMessage(event.getPlayer(), parts[1]);
                } else {
                    // Switch channel: /g
                                if (channel.canJoin(event.getPlayer())) {
                                    plugin.getChannelManager().setPlayerChannel(event.getPlayer(), channel.getId());
                                    event.getPlayer().sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize(plugin.getConfig().getString("channels.messages.switched", "<green>Switched to channel: <white>{channel}").replace("{channel}", channel.getName())));
                                } else {
                                    event.getPlayer().sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize(plugin.getConfig().getString("channels.messages.no-permission-join", "<red>You do not have permission to join this channel.")));
                                }
                    
                }
                return;
            }
        }
    }
}
