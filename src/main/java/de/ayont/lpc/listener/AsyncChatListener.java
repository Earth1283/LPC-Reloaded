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
    private final java.util.Map<java.util.UUID, Long> cooldowns = new java.util.HashMap<>();

    public AsyncChatListener(LPC plugin) {
        this.plugin = plugin;
        this.lpcChatRenderer = new LPCChatRenderer(plugin);
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {

        final Player player = event.getPlayer();
        String plainMessage = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(event.message());

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
                    player.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize(denyMsg.replace("{time}", String.format("%.1f", timeLeft))));
                    return;
                }
                cooldowns.put(player.getUniqueId(), now);
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

        // Ignore System
        if (plugin.getConfig().getBoolean("ignore.enabled", false)) {
            event.viewers().removeIf(audience -> audience instanceof Player && plugin.isIgnored(((Player) audience).getUniqueId(), player.getUniqueId()));
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
                        .replacement(displayName.hoverEvent(item)).build()));
    }
}
