package de.ayont.lpc.listener;

import de.ayont.lpc.LPC;
import net.kyori.adventure.text.Component;
import de.ayont.lpc.renderer.SpigotChatRenderer;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;


public class SpigotChatListener implements Listener {
    private final LPC plugin;
    private final SpigotChatRenderer chatRenderer;
    private final Map<String, String> legacyToMiniMessageColors;

    public SpigotChatListener(LPC plugin) {
        this.plugin = plugin;
        this.chatRenderer = new SpigotChatRenderer(plugin);
        this.legacyToMiniMessageColors = new HashMap<>();
        initColorMappings();
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

    private void initColorMappings() {
        legacyToMiniMessageColors.put("&0", "<black>");
        legacyToMiniMessageColors.put("&1", "<dark_blue>");
        legacyToMiniMessageColors.put("&2", "<dark_green>");
        legacyToMiniMessageColors.put("&3", "<dark_aqua>");
        legacyToMiniMessageColors.put("&4", "<dark_red>");
        legacyToMiniMessageColors.put("&5", "<dark_purple>");
        legacyToMiniMessageColors.put("&6", "<gold>");
        legacyToMiniMessageColors.put("&7", "<gray>");
        legacyToMiniMessageColors.put("&8", "<dark_gray>");
        legacyToMiniMessageColors.put("&9", "<blue>");
        legacyToMiniMessageColors.put("&a", "<green>");
        legacyToMiniMessageColors.put("&b", "<aqua>");
        legacyToMiniMessageColors.put("&c", "<red>");
        legacyToMiniMessageColors.put("&d", "<light_purple>");
        legacyToMiniMessageColors.put("&e", "<yellow>");
        legacyToMiniMessageColors.put("&f", "<white>");
        legacyToMiniMessageColors.put("&l", "<bold>");
        legacyToMiniMessageColors.put("&o", "<italic>");
        legacyToMiniMessageColors.put("&n", "<underlined>");
        legacyToMiniMessageColors.put("&m", "<strikethrough>");
        legacyToMiniMessageColors.put("&k", "<obfuscated>");
        legacyToMiniMessageColors.put("&r", "<reset>");
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onChat(AsyncPlayerChatEvent event) {
        String rawMessage = event.getMessage();
        org.bukkit.entity.Player player = event.getPlayer();

        // Mute Check
        de.ayont.lpc.moderation.Mute mute = plugin.getModerationManager().getActiveMute(player.getUniqueId());
        if (mute != null && !player.hasPermission("lpc.mute.bypass")) {
            event.setCancelled(true);
            String timeStr = mute.isPermanent() ? "Permanent" : formatTimeLeft(mute.getExpiry() - System.currentTimeMillis());
            String msgKey = mute.isPermanent() ? "mutes.messages.permanent" : "mutes.messages.muted";
            String muteMsg = plugin.getConfig().getString(msgKey, "<red>You are muted! Reason: {reason}")
                    .replace("{reason}", mute.getReason())
                    .replace("{time}", timeStr);
            plugin.getAdventure().player(player).sendMessage(MiniMessage.miniMessage().deserialize(muteMsg));
            return;
        }

        // Staff Chat
        if (plugin.getConfig().getBoolean("staff-chat.enabled", false)) {
            String prefix = plugin.getConfig().getString("staff-chat.prefix", "#");
            if (rawMessage.startsWith(prefix) && player.hasPermission("lpc.staffchat")) {
                event.setCancelled(true);
                String msg = rawMessage.substring(prefix.length()).trim();
                if (!msg.isEmpty()) {
                    String format = plugin.getConfig().getString("staff-chat.format", "<red>[Staff] {name}: <white>{message}")
                            .replace("{name}", player.getName())
                            .replace("{message}", msg);
                    Component component = MiniMessage.miniMessage().deserialize(format);
                    for (org.bukkit.entity.Player p : org.bukkit.Bukkit.getOnlinePlayers()) {
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
                plugin.getAdventure().player(player).sendMessage(MiniMessage.miniMessage().deserialize(denyMsg.replace("{time}", String.format("%.1f", timeLeft))));
                return;
            }

            // Caps Limit
            if (plugin.getConfig().getBoolean("filter.caps-limit.enabled", false)) {
                int minLength = plugin.getConfig().getInt("filter.caps-limit.min-length", 5);
                if (rawMessage.length() >= minLength) {
                    double percentage = plugin.getConfig().getDouble("filter.caps-limit.percentage", 70.0);
                    int upperCount = 0;
                    for (char c : rawMessage.toCharArray()) {
                        if (Character.isUpperCase(c)) upperCount++;
                    }
                    if (((double) upperCount / rawMessage.length()) * 100 > percentage) {
                        event.setCancelled(true);
                        plugin.getAdventure().player(player).sendMessage(MiniMessage.miniMessage().deserialize(plugin.getConfig().getString("filter.caps-limit.deny-message", "<red>Too many caps!")));
                        return;
                    }
                }
            }

            // Anti-Discord
            if (plugin.getConfig().getBoolean("filter.anti-discord.enabled", false)) {
                String regex = plugin.getConfig().getString("filter.anti-discord.regex", "discord(?:\\.gg|app\\.com\\/invite|\\.com\\/invite)\\/[a-zA-Z0-9]+");
                if (java.util.regex.Pattern.compile(regex).matcher(rawMessage).find()) {
                    event.setCancelled(true);
                    plugin.getAdventure().player(player).sendMessage(MiniMessage.miniMessage().deserialize(plugin.getConfig().getString("filter.anti-discord.deny-message", "<red>No discord invites!")));
                    return;
                }
            }

            // Anti-Link
            if (plugin.getConfig().getBoolean("filter.anti-link.enabled", false)) {
                String regex = plugin.getConfig().getString("filter.anti-link.regex", "(https?:\\/\\/)?(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_\\+.~#?&//=]*)");
                if (java.util.regex.Pattern.compile(regex).matcher(rawMessage).find()) {
                    event.setCancelled(true);
                    plugin.getAdventure().player(player).sendMessage(MiniMessage.miniMessage().deserialize(plugin.getConfig().getString("filter.anti-link.deny-message", "<red>No links!")));
                    return;
                }
            }
        }

        plugin.getModerationManager().updateLastChat(player.getUniqueId());

        // Ignore System
        if (plugin.getConfig().getBoolean("ignore.enabled", false)) {
            event.getRecipients().removeIf(p -> plugin.isIgnored(p.getUniqueId(), player.getUniqueId()));
        }

        // Chat Bubbles
        if (plugin.getConfig().getBoolean("chat-bubbles.enabled", true)) {
            org.bukkit.Bukkit.getScheduler().runTask(plugin, () -> {
                plugin.getChatBubbleManager().spawnBubble(player, rawMessage);
            });
        }
        
        // Chat Channels
        if (plugin.getConfig().getBoolean("channels.enabled", false)) {
             event.setCancelled(true);
             plugin.getChannelManager().sendMessage(player, rawMessage);
             return;
        }

        String message = rawMessage;

        if (event.getPlayer().hasPermission("lpc.chatcolor")) {
            message = message.replaceAll("§", "&");
            for (Map.Entry<String, String> entry : legacyToMiniMessageColors.entrySet()) {
                message = message.replace(entry.getKey(), entry.getValue());
            }
        } else {
            for (Map.Entry<String, String> entry : legacyToMiniMessageColors.entrySet()) {
                message = message.replace(entry.getValue(), entry.getKey());
            }
        }

        if (plugin.getConfig().getBoolean("use-item-placeholder", false) && event.getPlayer().hasPermission("lpc.itemplaceholder")) {
            final ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
            if (!item.getType().equals(Material.AIR)) {
                String itemName = item.getType().toString().toLowerCase().replace("_", " ");
                ItemMeta meta = item.getItemMeta();
                if (meta != null && meta.hasDisplayName()) {
                    try {
                        Component dn = meta.displayName();
                        if (dn != null) {
                            itemName = MiniMessage.miniMessage().serialize(dn);
                        }
                    } catch (NoSuchMethodError e) {
                        itemName = MiniMessage.miniMessage().serialize(
                                LegacyComponentSerializer.builder()
                                        .useUnusualXRepeatedCharacterHexFormat()
                                        .hexColors()
                                        .character('§')
                                        .build()
                                        .deserialize(meta.getDisplayName())
                        );
                    }
                }

                // Use MiniMessage show_item tag which handles NBT/item details automatically
                // Syntax: <hover:show_item:'item_type':count:'nbt'>
                // Since we are using adventure-platform-bukkit, we can also use show_item if supported,
                // but building the tag manually is often safer for legacy Spigot compatibility.
                
                String material = item.getType().getKey().toString();
                String nbt = "";
                try {
                    // Try to get NBT via reflection or Bukkit API if available
                    // For modern Paper/Spigot, item.asHoverEvent() is available via Adventure
                    net.kyori.adventure.text.event.HoverEvent<?> hover = item.asHoverEvent();
                    message = message.replaceFirst("(?i)\\[item]", MiniMessage.miniMessage().serialize(
                            Component.text(itemName).hoverEvent(hover)
                    ));
                } catch (NoClassDefFoundError | NoSuchMethodError e) {
                    // Fallback to simple name if adventure hover fails
                    message = message.replaceFirst("(?i)\\[item]", itemName);
                }
            }
        }

        event.setFormat(LegacyComponentSerializer.builder()
                .useUnusualXRepeatedCharacterHexFormat()
                .hexColors()
                .build()
                .serialize(chatRenderer.render(event.getPlayer(), message, plugin.getAdventure().player(event.getPlayer()))));
    }
}