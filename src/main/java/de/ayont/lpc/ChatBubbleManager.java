package de.ayont.lpc;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.util.Transformation;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChatBubbleManager {

    private final LPC plugin;
    private final Map<UUID, TextDisplay> activeBubbles = new HashMap<>();

    public ChatBubbleManager(LPC plugin) {
        this.plugin = plugin;
    }

    public void spawnBubble(Player player, String message) {
        if (!plugin.getConfig().getBoolean("chat-bubbles.enabled", true)) {
            return;
        }

        if (plugin.getConfig().getString("chat-bubbles.behavior", "replace").equalsIgnoreCase("replace")) {
            removeBubble(player.getUniqueId());
        }

        double heightOffset = plugin.getConfig().getDouble("chat-bubbles.height-offset", 2.8);
        Location loc = player.getLocation();

        TextDisplay textDisplay = player.getWorld().spawn(loc, TextDisplay.class, display -> {
            display.setText(message);
            display.setBillboard(Display.Billboard.valueOf(plugin.getConfig().getString("chat-bubbles.billboard", "CENTER").toUpperCase()));
            display.setShadowed(plugin.getConfig().getBoolean("chat-bubbles.shadow", true));
            display.setSeeThrough(plugin.getConfig().getBoolean("chat-bubbles.see-through", false));
            
            int wrapWidth = plugin.getConfig().getInt("chat-bubbles.text-wrap-width", 200);
            display.setLineWidth(wrapWidth);
            display.setBrightness(new Display.Brightness(15, 15));
            display.setInterpolationDuration(1);
            display.setInterpolationDelay(0);

            String colorStr = plugin.getConfig().getString("chat-bubbles.background-color", "0,0,0,100");
            try {
                String[] parts = colorStr.split(",");
                if (parts.length == 4) {
                    int r = Integer.parseInt(parts[0].trim());
                    int g = Integer.parseInt(parts[1].trim());
                    int b = Integer.parseInt(parts[2].trim());
                    int a = Integer.parseInt(parts[3].trim());
                    display.setBackgroundColor(Color.fromARGB(a, r, g, b));
                }
            } catch (Exception ignored) {}

            float scale = (float) plugin.getConfig().getDouble("chat-bubbles.scale", 1.0);
            Transformation transformation = display.getTransformation();
            transformation.getScale().set(scale, scale, scale);
            transformation.getTranslation().set(0, (float) heightOffset, 0);
            display.setTransformation(transformation);
        });

        // Attach to player (Teleporting it every tick or using a passenger?)
        // Passenger is easier but might be weird for billboards. 
        // For now, let's just make it a passenger if billboard is CENTER/VERTICAL.
        // Actually, TextDisplay can just stay at the location but it won't move with the player.
        // Let's use a Task to follow the player or mount it.
        // Mounting is better for performance.
        player.addPassenger(textDisplay);

        activeBubbles.put(player.getUniqueId(), textDisplay);

        // Visibility toggle
        if (plugin.isPaper()) {
            boolean seeSelf = plugin.getConfig().getBoolean("chat-bubbles.see-self", true);
            boolean selfEnabled = plugin.isChatBubblesEnabled(player.getUniqueId());
            
            if (!seeSelf || !selfEnabled) {
                player.hideEntity(plugin, textDisplay);
            } else {
                player.showEntity(plugin, textDisplay);
            }
            
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.equals(player)) continue;
                if (!plugin.isChatBubblesEnabled(p.getUniqueId())) {
                    p.hideEntity(plugin, textDisplay);
                } else {
                    p.showEntity(plugin, textDisplay);
                }
            }
        }

        double duration = plugin.getConfig().getDouble("chat-bubbles.duration", 5.0);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (activeBubbles.get(player.getUniqueId()) == textDisplay) {
                removeBubble(player.getUniqueId());
            }
        }, (long) (duration * 20));
    }

    public void removeBubble(UUID playerUUID) {
        TextDisplay display = activeBubbles.remove(playerUUID);
        if (display != null && display.isValid()) {
            display.remove();
        }
    }

    public void updateVisibilityForPlayer(Player player) {
        if (!plugin.isPaper()) return;
        
        boolean enabled = plugin.isChatBubblesEnabled(player.getUniqueId());
        for (TextDisplay display : activeBubbles.values()) {
            if (enabled) {
                player.showEntity(plugin, display);
            } else {
                player.hideEntity(plugin, display);
            }
        }
    }
}
