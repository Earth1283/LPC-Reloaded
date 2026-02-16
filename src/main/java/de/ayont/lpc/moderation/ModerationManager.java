package de.ayont.lpc.moderation;

import de.ayont.lpc.LPC;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ModerationManager {
    private final LPC plugin;
    private final Map<UUID, Long> slowmodes = new ConcurrentHashMap<>();
    private final Map<UUID, Long> lastChatTimes = new ConcurrentHashMap<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm");

    public ModerationManager(LPC plugin) {
        this.plugin = plugin;
    }

    public Mute getActiveMute(UUID uuid) {
        if (!plugin.getConfig().getBoolean("mutes.enabled", true)) return null;
        if (plugin.getStorage() == null) return null;
        Mute mute = plugin.getStorage().loadMute(uuid);
        if (mute != null && mute.hasExpired()) {
            plugin.getStorage().removeMute(uuid);
            return null;
        }
        return mute;
    }

    public void mutePlayer(Mute mute) {
        if (!plugin.getConfig().getBoolean("mutes.enabled", true)) return;
        if (plugin.getStorage() == null) return;
        plugin.getStorage().saveMute(mute);
    }

    public void unmutePlayer(UUID uuid) {
        if (!plugin.getConfig().getBoolean("mutes.enabled", true)) return;
        if (plugin.getStorage() == null) return;
        plugin.getStorage().removeMute(uuid);
    }

    public void warnPlayer(Warning warning) {
        if (!plugin.getConfig().getBoolean("warnings.enabled", true)) return;
        if (plugin.getStorage() == null) return;
        plugin.getStorage().addWarning(warning);
    }

    public List<Warning> getWarnings(UUID uuid) {
        if (!plugin.getConfig().getBoolean("warnings.enabled", true)) return Collections.emptyList();
        if (plugin.getStorage() == null) return Collections.emptyList();
        return plugin.getStorage().loadWarnings(uuid);
    }

    public void deleteWarning(int id) {
        if (!plugin.getConfig().getBoolean("warnings.enabled", true)) return;
        if (plugin.getStorage() == null) return;
        plugin.getStorage().removeWarning(id);
    }

    public void setBio(UUID uuid, String bio) {
        if (!plugin.getConfig().getBoolean("profiles.enabled", true)) return;
        if (plugin.getStorage() == null) return;
        plugin.getStorage().saveBio(uuid, bio);
    }

    public String getBio(UUID uuid) {
        if (!plugin.getConfig().getBoolean("profiles.enabled", true)) return "";
        if (plugin.getStorage() == null) return "";
        String bio = plugin.getStorage().loadBio(uuid);
        return bio != null ? bio : "";
    }

    public void setSlowmode(UUID uuid, double seconds) {
        slowmodes.put(uuid, (long) (seconds * 1000));
        if (plugin.getStorage() != null) {
            plugin.getStorage().setSlowmode(uuid, seconds);
        }
    }

    public long getSlowmode(UUID uuid) {
        return slowmodes.computeIfAbsent(uuid, k -> {
            if (plugin.getStorage() != null) {
                return (long) (plugin.getStorage().getSlowmode(uuid) * 1000);
            }
            return 0L;
        });
    }

    public boolean canChat(Player player) {
        long now = System.currentTimeMillis();
        long slowmode = getSlowmode(player.getUniqueId());
        
        // If no per-player slowmode, use global
        if (slowmode <= 0) {
             slowmode = (long) (plugin.getConfig().getDouble("filter.cooldown", 0.0) * 1000);
        }

        if (slowmode <= 0 || player.hasPermission("lpc.filter.bypass")) {
            return true;
        }

        long lastChat = lastChatTimes.getOrDefault(player.getUniqueId(), 0L);
        return (now - lastChat) >= slowmode;
    }

    public void updateLastChat(UUID uuid) {
        lastChatTimes.put(uuid, System.currentTimeMillis());
    }

    public double getTimeLeft(UUID uuid) {
        long now = System.currentTimeMillis();
        long slowmode = getSlowmode(uuid);
        if (slowmode <= 0) {
            slowmode = (long) (plugin.getConfig().getDouble("filter.cooldown", 0.0) * 1000);
        }
        long lastChat = lastChatTimes.getOrDefault(uuid, 0L);
        return Math.max(0, (slowmode - (now - lastChat)) / 1000.0);
    }

    public String getInfractionsSummary(UUID uuid, int max) {
        if (!plugin.getConfig().getBoolean("warnings.enabled", true)) return "";
        List<Warning> warnings = getWarnings(uuid);
        if (warnings.isEmpty()) {
            return plugin.getConfig().getString("profiles.no-infractions", "<gray>No recent infractions.");
        }

        String format = plugin.getConfig().getString("profiles.infraction-format", "<red>• {reason} <dark_gray>({date})</dark_gray>");
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (Warning w : warnings) {
            if (count >= max) break;
            sb.append(format
                    .replace("{reason}", w.getReason())
                    .replace("{date}", dateFormat.format(new Date(w.getTimestamp())))
            );
            if (count < Math.min(warnings.size(), max) - 1) {
                sb.append("<newline>");
            }
            count++;
        }
        return sb.toString();
    }
}
