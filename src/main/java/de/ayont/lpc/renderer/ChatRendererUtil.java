package de.ayont.lpc.renderer;

import de.ayont.lpc.LPC;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.track.Track;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ChatRendererUtil {

    private final LuckPerms luckPerms;
    private final LPC plugin;
    private final MiniMessage miniMessage;
    private final boolean hasPapi;

    private static final Map<String, String> legacyToMiniMessageColors = new HashMap<>() {
        {
            put("&0", "<black>");
            put("&1", "<dark_blue>");
            put("&2", "<dark_green>");
            put("&3", "<dark_aqua>");
            put("&4", "<dark_red>");
            put("&5", "<dark_purple>");
            put("&6", "<gold>");
            put("&7", "<gray>");
            put("&8", "<dark_gray>");
            put("&9", "<blue>");
            put("&a", "<green>");
            put("&b", "<aqua>");
            put("&c", "<red>");
            put("&d", "<light_purple>");
            put("&e", "<yellow>");
            put("&f", "<white>");
            put("&l", "<bold>");
            put("&o", "<italic>");
            put("&n", "<underlined>");
            put("&m", "<strikethrough>");
            put("&k", "<obfuscated>");
            put("&r", "<reset>");
        }
    };

    public ChatRendererUtil(LPC plugin) {
        this.luckPerms = LuckPermsProvider.get();
        this.plugin = plugin;
        this.miniMessage = MiniMessage.builder().build();
        PluginManager pluginManager = plugin.getServer().getPluginManager();
        hasPapi = pluginManager.getPlugin("PlaceholderAPI") != null;
    }

    public @NotNull Component render(@NotNull Player source, @NotNull String message, @NotNull Audience viewer, @Nullable String channelId) {
        final CachedMetaData metaData = this.luckPerms.getPlayerAdapter(Player.class).getMetaData(source);
        final String group = Objects.requireNonNull(metaData.getPrimaryGroup(), "Primary group cannot be null");

        boolean hasPermission = source.hasPermission("lpc.chatcolor");

        String plainMessage = message;

        if (hasPermission) {
            for (Map.Entry<String, String> entry : legacyToMiniMessageColors.entrySet()) {
                plainMessage = plainMessage.replace(entry.getKey(), entry.getValue());
            }
        } else {
            plainMessage = miniMessage.escapeTags(plainMessage);
        }

        String format = null;

        // 1. Check channel-specific group formats
        if (channelId != null) {
            format = plugin.getConfig().getString("channels.list." + channelId + ".group-formats." + group);
        }

        // 2. Check channel-specific track formats
        if (format == null && channelId != null) {
            ConfigurationSection trackFormatsSection = plugin.getConfig().getConfigurationSection("channels.list." + channelId + ".track-formats");
            if (trackFormatsSection != null) {
                for (String trackName : trackFormatsSection.getKeys(false)) {
                    Track track = this.luckPerms.getTrackManager().getTrack(trackName);
                    if (track == null) continue;
                    if (track.containsGroup(group)) {
                        format = plugin.getConfig().getString("channels.list." + channelId + ".track-formats." + trackName);
                        break;
                    }
                }
            }
        }

        // 3. Check channel default format
        if (format == null && channelId != null) {
            format = plugin.getConfig().getString("channels.list." + channelId + ".format");
        }

        // 4. Check global group formats
        if (format == null) {
            format = plugin.getConfig().getString("group-formats." + group);
        }

        // 5. Check global track formats
        if (format == null) {
            ConfigurationSection trackFormatsSection = plugin.getConfig().getConfigurationSection("track-formats");
            if (trackFormatsSection != null) {
                for (String trackName : trackFormatsSection.getKeys(false)) {
                    Track track = this.luckPerms.getTrackManager().getTrack(trackName);
                    if (track == null) continue;
                    if (track.containsGroup(group)) {
                        format = plugin.getConfig().getString("track-formats." + trackName);
                        break;
                    }
                }
            }
        }

        // 6. Check global default format
        if (format == null) {
            format = plugin.getConfig().getString("chat-format");
        }

        if (format == null) {
            format = "{prefix}{name} » {message}"; // Hard fallback
        }

        format = format.replace("{prefix}", metaData.getPrefix() != null ? metaData.getPrefix() : "")
                .replace("{suffix}", metaData.getSuffix() != null ? metaData.getSuffix() : "")
                .replace("{prefixes}", String.join(" ", metaData.getPrefixes().values()))
                .replace("{suffixes}", String.join(" ", metaData.getSuffixes().values()))
                .replace("{world}", source.getWorld().getName())
                .replace("{username-color}", metaData.getMetaValue("username-color") != null ? Objects.requireNonNull(metaData.getMetaValue("username-color")) : "")
                .replace("{message-color}", metaData.getMetaValue("message-color") != null ? Objects.requireNonNull(metaData.getMetaValue("message-color")) : "");

        // Interactive Player Elements
        String nameReplacement = source.getName();
        String displayNameReplacement = source.getDisplayName();

        if (plugin.getConfig().getBoolean("interactive.enabled", false)) {
            String clickAction = plugin.getConfig().getString("interactive.click-action", "SUGGEST_COMMAND").toLowerCase();
            String clickValue = plugin.getConfig().getString("interactive.click-value", "/msg {name} ")
                    .replace("{name}", source.getName());
            
            List<String> hoverLines = plugin.getConfig().getStringList("interactive.hover-text");
            StringBuilder hoverBuilder = new StringBuilder();
            String bio = plugin.getModerationManager().getBio(source.getUniqueId());
            String infractions = "";
            if (viewer instanceof Player && ((Player) viewer).hasPermission("lpc.staff.viewinfractions")) {
                int max = plugin.getConfig().getInt("profiles.max-hover-infractions", 3);
                infractions = plugin.getModerationManager().getInfractionsSummary(source.getUniqueId(), max);
            }

            for (int i = 0; i < hoverLines.size(); i++) {
                String line = hoverLines.get(i)
                        .replace("{name}", source.getName())
                        .replace("{displayname}", displayNameReplacement)
                        .replace("{prefix}", metaData.getPrefix() != null ? metaData.getPrefix() : "")
                        .replace("{suffix}", metaData.getSuffix() != null ? metaData.getSuffix() : "")
                        .replace("{world}", source.getWorld().getName())
                        .replace("{bio}", bio.isEmpty() ? "No bio set." : bio)
                        .replace("{infractions}", infractions);
                
                if (line.trim().isEmpty()) continue;

                hoverBuilder.append(line);
                if (i < hoverLines.size() - 1) {
                    hoverBuilder.append("<newline>");
                }
            }
            String hover = hoverBuilder.toString();

            String events = "<hover:show_text:'" + hover + "'><click:" + clickAction + ":'" + clickValue + "'>";
            nameReplacement = events + nameReplacement + "</click></hover>";
            displayNameReplacement = events + displayNameReplacement + "</click></hover>";
        }

        format = format.replace("{name}", nameReplacement)
                       .replace("{displayname}", displayNameReplacement);

        if (!hasPermission) {
            for (Map.Entry<String, String> entry : legacyToMiniMessageColors.entrySet()) {
                plainMessage = plainMessage.replace(entry.getValue(), entry.getKey());
            }
        }

        // URL Highlighting
        if (plugin.getConfig().getBoolean("url-highlighting.enabled", false)) {
            String urlFormat = plugin.getConfig().getString("url-highlighting.format", "<underlined><blue>{url}</blue></underlined>");
            String urlHover = plugin.getConfig().getString("url-highlighting.hover-text", "<gray>Click to open: <white>{url}");
            
            java.util.regex.Pattern urlPattern = java.util.regex.Pattern.compile("(https?:\\/\\/)?(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_\\+.~#?&//=]*)");
            java.util.regex.Matcher matcher = urlPattern.matcher(plainMessage);
            
            StringBuffer sb = new StringBuffer();
            while (matcher.find()) {
                String url = matcher.group();
                String fullUrl = url.startsWith("http") ? url : "https://" + url;
                String replacement = "<click:open_url:'" + fullUrl + "'><hover:show_text:'" + urlHover.replace("{url}", url) + "'>" + urlFormat.replace("{url}", url) + "</hover></click>";
                matcher.appendReplacement(sb, java.util.regex.Matcher.quoteReplacement(replacement));
            }
            matcher.appendTail(sb);
            plainMessage = sb.toString();
        }

        // Mentions
        if (plugin.getConfig().getBoolean("mentions.enabled", false) && viewer instanceof Player) {
            Player viewerPlayer = (Player) viewer;
            String mentionPattern = "(?i)@" + java.util.regex.Pattern.quote(viewerPlayer.getName());
            if (java.util.regex.Pattern.compile(mentionPattern).matcher(plainMessage).find()) {
                String mentionFormat = plugin.getConfig().getString("mentions.format", "<yellow><b>@{name}</b></yellow>").replace("{name}", viewerPlayer.getName());
                plainMessage = plainMessage.replaceAll(mentionPattern, mentionFormat);
                
                String soundName = plugin.getConfig().getString("mentions.sound", "entity.experience_orb.pickup");
                float volume = (float) plugin.getConfig().getDouble("mentions.volume", 1.0);
                float pitch = (float) plugin.getConfig().getDouble("mentions.pitch", 1.0);
                
                viewer.playSound(net.kyori.adventure.sound.Sound.sound(
                        net.kyori.adventure.key.Key.key(soundName),
                        net.kyori.adventure.sound.Sound.Source.PLAYER,
                        volume,
                        pitch
                ));
            }
        }

        format = format.replace("{message}", plainMessage);

        if (hasPapi) {
            format = PlaceholderAPI.setPlaceholders(source, format);
        }

        return miniMessage.deserialize(format);
    }
    
    public static Map<String, String> getLegacyToMiniMessageColors() {
        return legacyToMiniMessageColors;
    }
}
