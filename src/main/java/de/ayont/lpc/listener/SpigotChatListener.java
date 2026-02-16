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
    private final java.util.Map<java.util.UUID, Long> cooldowns = new java.util.HashMap<>();

    public SpigotChatListener(LPC plugin) {
        this.plugin = plugin;
        this.chatRenderer = new SpigotChatRenderer(plugin);
        this.legacyToMiniMessageColors = new HashMap<>();
        initColorMappings();
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

        // Chat Filter
        if (plugin.getConfig().getBoolean("filter.enabled", false) && !player.hasPermission("lpc.filter.bypass")) {
            // Cooldown
            double cooldownSeconds = plugin.getConfig().getDouble("filter.cooldown", 0.0);
            if (cooldownSeconds > 0) {
                long now = System.currentTimeMillis();
                long lastChat = cooldowns.getOrDefault(player.getUniqueId(), 0L);
                if (now - lastChat < cooldownSeconds * 1000) {
                    event.setCancelled(true);
                    String denyMsg = plugin.getConfig().getString("filter.deny-message", "<red>Please wait {time}s.");
                    double timeLeft = (cooldownSeconds * 1000 - (now - lastChat)) / 1000.0;
                    plugin.getAdventure().player(player).sendMessage(MiniMessage.miniMessage().deserialize(denyMsg.replace("{time}", String.format("%.1f", timeLeft))));
                    return;
                }
                cooldowns.put(player.getUniqueId(), now);
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

        // Ignore System
        if (plugin.getConfig().getBoolean("ignore.enabled", false)) {
            event.getRecipients().removeIf(p -> plugin.isIgnored(p.getUniqueId(), player.getUniqueId()));
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
                if (meta != null) {
                    StringBuilder hoverText = new StringBuilder();

                    if (meta.hasDisplayName()) {
                        try {
                            Component displayName = meta.displayName();
                            if (displayName != null) {
                                itemName = MiniMessage.miniMessage().serialize(displayName);
                            }
                        } catch (NoSuchMethodError e) {
                            String displayName = meta.getDisplayName();
                            itemName = MiniMessage.miniMessage().serialize(
                                    LegacyComponentSerializer.builder()
                                            .useUnusualXRepeatedCharacterHexFormat()
                                            .hexColors()
                                            .character('§')
                                            .build()
                                            .deserialize(displayName)
                            );
                        }
                    }

                    if (meta.hasLore()) {
                        try {
                            java.util.List<Component> lore = meta.lore();
                            if (lore != null) {
                                for (Component line : lore) {
                                    hoverText.append("\n").append(MiniMessage.miniMessage().serialize(line));
                                }
                            }
                        } catch (NoSuchMethodError e) {
                            java.util.List<String> lore = meta.getLore();
                            if (lore != null) {
                                for (String line : lore) {
                                    hoverText.append("\n").append(MiniMessage.miniMessage().serialize(
                                            LegacyComponentSerializer.builder()
                                                    .useUnusualXRepeatedCharacterHexFormat()
                                                    .hexColors()
                                                    .character('§')
                                                    .build()
                                                    .deserialize(line)
                                    ));
                                }
                            }
                        }
                    }

                    itemName = "<hover:show_text:'" + itemName + hoverText.toString() + "'>" + itemName + "</hover>";
                }
                message = message.replaceFirst("(?i)\\[item]", itemName);
            }
        }

        event.setFormat(LegacyComponentSerializer.builder()
                .useUnusualXRepeatedCharacterHexFormat()
                .hexColors()
                .build()
                .serialize(chatRenderer.render(event.getPlayer(), message)));
    }
}