package de.ayont.lpc.renderer

import de.ayont.lpc.LPC
import me.clip.placeholderapi.PlaceholderAPI
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.luckperms.api.LuckPermsProvider
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

class ChatRendererUtil(private val plugin: LPC) {

    private val luckPerms = LuckPermsProvider.get()
    private val miniMessage = MiniMessage.builder().build()
    private val hasPapi: Boolean = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null

    companion object {
        @JvmStatic
        val legacyToMiniMessageColors = mapOf(
            "&0" to "<black>",
            "&1" to "<dark_blue>",
            "&2" to "<dark_green>",
            "&3" to "<dark_aqua>",
            "&4" to "<dark_red>",
            "&5" to "<dark_purple>",
            "&6" to "<gold>",
            "&7" to "<gray>",
            "&8" to "<dark_gray>",
            "&9" to "<blue>",
            "&a" to "<green>",
            "&b" to "<aqua>",
            "&c" to "<red>",
            "&d" to "<light_purple>",
            "&e" to "<yellow>",
            "&f" to "<white>",
            "&l" to "<bold>",
            "&o" to "<italic>",
            "&n" to "<underlined>",
            "&m" to "<strikethrough>",
            "&k" to "<obfuscated>",
            "&r" to "<reset>"
        )
    }

    fun render(source: Player, message: String, viewer: Audience, channelId: String? = null): Component {
        val metaData = luckPerms.getPlayerAdapter(Player::class.java).getMetaData(source)
        val group = metaData.primaryGroup ?: "default"

        val hasPermission = source.hasPermission("lpc.chatcolor")
        var plainMessage = message

        if (hasPermission) {
            for ((key, value) in legacyToMiniMessageColors) {
                plainMessage = plainMessage.replace(key, value)
            }
        } else {
            plainMessage = miniMessage.escapeTags(plainMessage)
        }

        var format: String? = null

        // 1. Check channel-specific group formats
        if (channelId != null) {
            format = plugin.config.getString("channels.list.$channelId.group-formats.$group")
        }

        // 2. Check channel-specific track formats
        if (format == null && channelId != null) {
            val trackFormatsSection = plugin.config.getConfigurationSection("channels.list.$channelId.track-formats")
            if (trackFormatsSection != null) {
                for (trackName in trackFormatsSection.getKeys(false)) {
                    val track = luckPerms.trackManager.getTrack(trackName) ?: continue
                    if (track.containsGroup(group)) {
                        format = plugin.config.getString("channels.list.$channelId.track-formats.$trackName")
                        break
                    }
                }
            }
        }

        // 3. Check channel default format
        if (format == null && channelId != null) {
            format = plugin.config.getString("channels.list.$channelId.format")
        }

        // 4. Check global group formats
        if (format == null) {
            format = plugin.config.getString("group-formats.$group")
        }

        // 5. Check global track formats
        if (format == null) {
            val trackFormatsSection = plugin.config.getConfigurationSection("track-formats")
            if (trackFormatsSection != null) {
                for (trackName in trackFormatsSection.getKeys(false)) {
                    val track = luckPerms.trackManager.getTrack(trackName) ?: continue
                    if (track.containsGroup(group)) {
                        format = plugin.config.getString("track-formats.$trackName")
                        break
                    }
                }
            }
        }

        // 6. Check global default format
        if (format == null) {
            format = plugin.config.getString("chat-format")
        }

        if (format == null) {
            format = "{prefix}{name} » {message}" // Hard fallback
        }

        var resultFormat = format.replace("{prefix}", metaData.prefix ?: "")
            .replace("{suffix}", metaData.suffix ?: "")
            .replace("{prefixes}", metaData.prefixes.values.joinToString(" "))
            .replace("{suffixes}", metaData.suffixes.values.joinToString(" "))
            .replace("{world}", source.world.name)
            .replace("{username-color}", metaData.getMetaValue("username-color") ?: "")
            .replace("{message-color}", metaData.getMetaValue("message-color") ?: "")

        var nameReplacement = source.name
        var displayNameReplacement = source.displayName

        if (plugin.config.getBoolean("interactive.enabled", false)) {
            val clickAction = plugin.config.getString("interactive.click-action", "SUGGEST_COMMAND")!!.uppercase()
            val clickValue = plugin.config.getString("interactive.click-value", "/msg {name} ")!!
                .replace("{name}", source.name)
            
            val hoverLines = plugin.config.getStringList("interactive.hover-text")
            val bio = plugin.moderationManager?.getBio(source.uniqueId) ?: ""
            var infractions = ""
            if (viewer is Player && viewer.hasPermission("lpc.staff.viewinfractions")) {
                val max = plugin.config.getInt("profiles.max-hover-infractions", 3)
                infractions = plugin.moderationManager?.getInfractionsSummary(source.uniqueId, max) ?: ""
            }

            val hoverBuilder = StringBuilder()
            for (i in hoverLines.indices) {
                val line = hoverLines[i]
                    .replace("{name}", source.name)
                    .replace("{displayname}", displayNameReplacement)
                    .replace("{prefix}", metaData.prefix ?: "")
                    .replace("{suffix}", metaData.suffix ?: "")
                    .replace("{world}", source.world.name)
                    .replace("{bio}", if (bio.isEmpty()) "No bio set." else bio)
                    .replace("{infractions}", infractions)
                
                if (line.trim().isEmpty()) continue

                hoverBuilder.append(line)
                if (i < hoverLines.size - 1) {
                    hoverBuilder.append("<newline>")
                }
            }
            val hover = hoverBuilder.toString()

            val events = "<hover:show_text:'$hover'><click:$clickAction:'$clickValue'>"
            nameReplacement = "$events$nameReplacement</click></hover>"
            displayNameReplacement = "$events$displayNameReplacement</click></hover>"
        }

        resultFormat = resultFormat.replace("{name}", nameReplacement)
            .replace("{displayname}", displayNameReplacement)

        if (!hasPermission) {
            for ((key, value) in legacyToMiniMessageColors) {
                plainMessage = plainMessage.replace(value, key)
            }
        }

        // URL Highlighting
        if (plugin.config.getBoolean("url-highlighting.enabled", false)) {
            val urlFormat = plugin.config.getString("url-highlighting.format", "<underlined><blue>{url}</blue></underlined>")!!
            val urlHover = plugin.config.getString("url-highlighting.hover-text", "<gray>Click to open: <white>{url}")!!
            
            val urlPattern = Pattern.compile("(https?:\\/\\/)?(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_\\+.~#?&//=]*)")
            val matcher = urlPattern.matcher(plainMessage)
            
            val sb = StringBuffer()
            while (matcher.find()) {
                val url = matcher.group()
                val fullUrl = if (url.startsWith("http")) url else "https://$url"
                val replacement = "<click:open_url:'$fullUrl'><hover:show_text:'${urlHover.replace("{url}", url)}'>${urlFormat.replace("{url}", url)}</hover></click>"
                matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement))
            }
            matcher.appendTail(sb)
            plainMessage = sb.toString()
        }

        // Mentions
        if (plugin.config.getBoolean("mentions.enabled", false) && viewer is Player) {
            val mentionPattern = "(?i)@" + Pattern.quote(viewer.name)
            if (Pattern.compile(mentionPattern).matcher(plainMessage).find()) {
                val mentionFormat = plugin.config.getString("mentions.format", "<yellow><b>@{name}</b></yellow>")!!
                    .replace("{name}", viewer.name)
                plainMessage = plainMessage.replace(mentionPattern.toRegex(), mentionFormat)
                
                val soundName = plugin.config.getString("mentions.sound", "entity.experience_orb.pickup")!!
                val volume = plugin.config.getDouble("mentions.volume", 1.0).toFloat()
                val pitch = plugin.config.getDouble("mentions.pitch", 1.0).toFloat()
                
                viewer.playSound(Sound.sound(
                    Key.key(soundName),
                    Sound.Source.PLAYER,
                    volume,
                    pitch
                ))
            }
        }

        resultFormat = resultFormat.replace("{message}", plainMessage)

        if (hasPapi) {
            resultFormat = PlaceholderAPI.setPlaceholders(source, resultFormat)
        }

        return miniMessage.deserialize(resultFormat)
    }
}
