package de.ayont.lpc.listener;

import de.ayont.lpc.LPC;
import de.ayont.lpc.renderer.LPCChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import static java.util.regex.Pattern.*;

public class AsyncChatListener implements Listener {

    private final LPC plugin;
    private final LPCChatRenderer lpcChatRenderer;

    public AsyncChatListener(LPC plugin) {
        this.plugin = plugin;
        this.lpcChatRenderer = new LPCChatRenderer(plugin);
    }

    private String formatTimeLeft(long millis) {
        if (millis <= 0) return "0s";
        long seconds = millis / 1000 % 60;
        long minutes = millis / (60 * 1000) % 60;
        long hours = millis / (60 * 60 * 1000) % 24;
        long days = millis / (24 * 60 * 60 * 1000);

        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("d ");
        if (hours > 0) sb.append(hours).append("h ");
        if (minutes > 0) sb.append(minutes).append("m ");
        if (seconds > 0) sb.append(seconds).append("s");
        return sb.toString().trim();
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {

        final Player player = event.getPlayer();
        String plainMessage = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(event.message());

        // Mute Check
        de.ayont.lpc.moderation.Mute mute = plugin.getModerationManager().getActiveMute(player.getUniqueId());
        if (mute != null && !player.hasPermission("lpc.mute.bypass")) {
            event.setCancelled(true);
            String timeStr = mute.isPermanent() ? "Permanent" : formatTimeLeft(mute.getExpiry() - System.currentTimeMillis());
            String msgKey = mute.isPermanent() ? "mutes.messages.permanent" : "mutes.messages.muted";
            String muteMsg = plugin.getConfig().getString(msgKey, "<red>You are muted! Reason: {reason}")
                    .replace("{reason}", mute.getReason())
                    .replace("{time}", timeStr);
            player.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize(muteMsg));
            return;
        }

        // Staff Chat
        if (plugin.getConfig().getBoolean("staff-chat.enabled", false)) {
            String prefix = plugin.getConfig().getString("staff-chat.prefix", "#");
            if (plainMessage.startsWith(prefix) && player.hasPermission("lpc.staffchat")) {
                event.setCancelled(true);
                String msg = plainMessage.substring(prefix.length()).trim();
                if (!msg.isEmpty()) {
                    String format = plugin.getConfig().getString("staff-chat.format", "<red>[Staff] {name}: <white>{message}")
                            .replace("{name}", player.getName())
                            .replace("{message}", msg);
                    Component component = net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize(format);
                    
                    for (Player p : org.bukkit.Bukkit.getOnlinePlayers()) {
                        if (p.hasPermission("lpc.staffchat")) {
                            plugin.getAdventure().player(p).sendMessage(component);
                        }
                    }
                    plugin.getAdventure().console().sendMessage(component);
                }
                return;
            }
        }

        // Chat Filter & Moderation
        if (plugin.getConfig().getBoolean("filter.enabled", false) && !player.hasPermission("lpc.filter.bypass")) {
            // Cooldown / Slowmode
            if (!plugin.getModerationManager().canChat(player)) {
                event.setCancelled(true);
                String denyMsg = plugin.getConfig().getString("filter.deny-message", "<red>Please wait {time}s.");
                double timeLeft = plugin.getModerationManager().getTimeLeft(player.getUniqueId());
                player.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize(denyMsg.replace("{time}", String.format("%.1f", timeLeft))));
                return;
            }

            // Caps Limit
            if (plugin.getConfig().getBoolean("filter.caps-limit.enabled", false)) {
                int minLength = plugin.getConfig().getInt("filter.caps-limit.min-length", 5);
                if (plainMessage.length() >= minLength) {
                    double percentage = plugin.getConfig().getDouble("filter.caps-limit.percentage", 70.0);
                    int upperCount = 0;
                    for (char c : plainMessage.toCharArray()) {
                        if (Character.isUpperCase(c)) upperCount++;
                    }
                    if (((double) upperCount / plainMessage.length()) * 100 > percentage) {
                        event.setCancelled(true);
                        player.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize(plugin.getConfig().getString("filter.caps-limit.deny-message", "<red>Too many caps!")));
                        return;
                    }
                }
            }

            // Anti-Discord
            if (plugin.getConfig().getBoolean("filter.anti-discord.enabled", false)) {
                String regex = plugin.getConfig().getString("filter.anti-discord.regex", "discord(?:\\.gg|app\\.com\\/invite|\\.com\\/invite)\\/[a-zA-Z0-9]+");
                if (java.util.regex.Pattern.compile(regex).matcher(plainMessage).find()) {
                    event.setCancelled(true);
                    player.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize(plugin.getConfig().getString("filter.anti-discord.deny-message", "<red>No discord invites!")));
                    return;
                }
            }
            
            // Anti-Link
             if (plugin.getConfig().getBoolean("filter.anti-link.enabled", false)) {
                String regex = plugin.getConfig().getString("filter.anti-link.regex", "(https?:\\/\\/)?(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_\\+.~#?&//=]*)");
                if (java.util.regex.Pattern.compile(regex).matcher(plainMessage).find()) {
                    event.setCancelled(true);
                    player.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize(plugin.getConfig().getString("filter.anti-link.deny-message", "<red>No links!")));
                    return;
                }
            }
        }
        
        plugin.getModerationManager().updateLastChat(player.getUniqueId());

        // Ignore System
        if (plugin.getConfig().getBoolean("ignore.enabled", false)) {
            event.viewers().removeIf(audience -> audience instanceof Player && plugin.isIgnored(((Player) audience).getUniqueId(), player.getUniqueId()));
        }

        // Chat Bubbles
        if (plugin.getConfig().getBoolean("chat-bubbles.enabled", true)) {
            org.bukkit.Bukkit.getScheduler().runTask(plugin, () -> {
                plugin.getChatBubbleManager().spawnBubble(player, plainMessage);
            });
        }
        
        // Chat Channels
        if (plugin.getConfig().getBoolean("channels.enabled", false)) {
             event.setCancelled(true);
             plugin.getChannelManager().sendMessage(player, plainMessage);
             return;
        }

        if(!plugin.getConfig().getBoolean("use-item-placeholder", false) || !player.hasPermission("lpc.itemplaceholder")){
            event.renderer(lpcChatRenderer);
            return;
        }

        final ItemStack item = player.getInventory().getItemInMainHand();
        final Component displayName = item.getItemMeta() != null && item.getItemMeta().hasDisplayName() ? item.getItemMeta().displayName() : Component.text(item.getType().toString().toLowerCase().replace("_", " "));
        if (item.getType().equals(Material.AIR) || displayName == null) {
            event.renderer(lpcChatRenderer);
            return;
        }

        event.renderer((source, sourceDisplayName, message, viewer) -> lpcChatRenderer.render(source, sourceDisplayName, message, viewer)
                .replaceText(TextReplacementConfig.builder().match(compile("\\[item]", CASE_INSENSITIVE))
                        .replacement(displayName.hoverEvent(item.asHoverEvent())).build()));
    }
}
