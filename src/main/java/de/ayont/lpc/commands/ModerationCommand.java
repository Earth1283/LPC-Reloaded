package de.ayont.lpc.commands;

import de.ayont.lpc.LPC;
import de.ayont.lpc.moderation.Mute;
import de.ayont.lpc.moderation.Warning;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ModerationCommand implements CommandExecutor, TabCompleter {
    private final LPC plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public ModerationCommand(LPC plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        switch (command.getName().toLowerCase()) {
            case "mute":
                return handleMute(sender, args);
            case "unmute":
                return handleUnmute(sender, args);
            case "warn":
                return handleWarn(sender, args);
            case "warnings":
                return handleWarnings(sender, args);
            case "delwarn":
                return handleDelWarn(sender, args);
            case "profile":
                return handleProfile(sender, args);
            case "setbio":
                return handleSetBio(sender, args);
            case "slowmode":
                return handleSlowmode(sender, args);
        }
        return false;
    }

    private boolean handleMute(CommandSender sender, String[] args) {
        if (!plugin.getConfig().getBoolean("mutes.enabled", true)) {
            sender.sendMessage(miniMessage.deserialize(plugin.getConfig().getString("mutes.messages.disabled", "<red>Muting is currently disabled.")));
            return true;
        }
        if (!sender.hasPermission("lpc.mute")) {
            sender.sendMessage(miniMessage.deserialize(plugin.getConfig().getString("general-messages.no-permission", "<red>No permission.")));
            return true;
        }
        if (args.length < 1) {
            sender.sendMessage(miniMessage.deserialize(plugin.getConfig().getString("general-messages.usage", "<red>Usage: {usage}").replace("{usage}", "/mute <player> [duration] [reason]")));
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        long expiry = -1;
        String reason = plugin.getConfig().getString("mutes.default-reason", "No reason provided");
        
        int reasonIndex = 1;
        if (args.length >= 2) {
            long duration = parseDuration(args[1]);
            if (duration != 0) {
                expiry = System.currentTimeMillis() + duration;
                reasonIndex = 2;
            }
        }

        if (args.length > reasonIndex) {
            reason = String.join(" ", Arrays.copyOfRange(args, reasonIndex, args.length));
        }

        Mute mute = new Mute(target.getUniqueId(), (sender instanceof Player) ? ((Player) sender).getUniqueId() : UUID.nameUUIDFromBytes("CONSOLE".getBytes()), reason, expiry);
        plugin.getModerationManager().mutePlayer(mute);

        String timeStr = expiry == -1 ? "Permanent" : formatTimeLeft(expiry - System.currentTimeMillis());
        String broadcast = plugin.getConfig().getString("mutes.messages.broadcast-mute", "<red>{player} has been muted by {staff} for {reason} ({time}).")
                .replace("{player}", target.getName() != null ? target.getName() : args[0])
                .replace("{staff}", sender.getName())
                .replace("{reason}", reason)
                .replace("{time}", timeStr);
        
        Bukkit.broadcast(miniMessage.deserialize(broadcast), "lpc.mute");
        return true;
    }

    private boolean handleUnmute(CommandSender sender, String[] args) {
        if (!plugin.getConfig().getBoolean("mutes.enabled", true)) {
            sender.sendMessage(miniMessage.deserialize(plugin.getConfig().getString("mutes.messages.disabled", "<red>Muting is currently disabled.")));
            return true;
        }
        if (!sender.hasPermission("lpc.unmute")) {
            sender.sendMessage(miniMessage.deserialize(plugin.getConfig().getString("general-messages.no-permission", "<red>No permission.")));
            return true;
        }
        if (args.length < 1) {
            sender.sendMessage(miniMessage.deserialize(plugin.getConfig().getString("general-messages.usage", "<red>Usage: {usage}").replace("{usage}", "/unmute <player>")));
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        plugin.getModerationManager().unmutePlayer(target.getUniqueId());

        String broadcast = plugin.getConfig().getString("mutes.messages.broadcast-unmute", "<green>{player} has been unmuted by {staff}.")
                .replace("{player}", target.getName() != null ? target.getName() : args[0])
                .replace("{staff}", sender.getName());
        
        Bukkit.broadcast(miniMessage.deserialize(broadcast), "lpc.mute");
        return true;
    }

    private boolean handleWarn(CommandSender sender, String[] args) {
        if (!plugin.getConfig().getBoolean("warnings.enabled", true)) {
            sender.sendMessage(miniMessage.deserialize(plugin.getConfig().getString("warnings.messages.disabled", "<red>Warnings are currently disabled.")));
            return true;
        }
        if (!sender.hasPermission("lpc.warn")) {
            sender.sendMessage(miniMessage.deserialize(plugin.getConfig().getString("general-messages.no-permission", "<red>No permission.")));
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage(miniMessage.deserialize(plugin.getConfig().getString("general-messages.usage", "<red>Usage: {usage}").replace("{usage}", "/warn <player> <reason>")));
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        String reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

        Warning warning = new Warning(0, target.getUniqueId(), (sender instanceof Player) ? ((Player) sender).getUniqueId() : UUID.nameUUIDFromBytes("CONSOLE".getBytes()), reason, System.currentTimeMillis());
        plugin.getModerationManager().warnPlayer(warning);

        String broadcast = plugin.getConfig().getString("warnings.messages.broadcast", "<red>{player} has been warned by {staff} for {reason}.")
                .replace("{player}", target.getName() != null ? target.getName() : args[0])
                .replace("{staff}", sender.getName())
                .replace("{reason}", reason);
        
        Bukkit.broadcast(miniMessage.deserialize(broadcast), "lpc.warn");
        
        if (target.isOnline() && target.getPlayer() != null) {
            String warnedMsg = plugin.getConfig().getString("warnings.messages.warned", "<red>You have been warned for: <white>{reason}")
                    .replace("{reason}", reason);
            target.getPlayer().sendMessage(miniMessage.deserialize(warnedMsg));
        }
        return true;
    }

    private boolean handleWarnings(CommandSender sender, String[] args) {
        if (!plugin.getConfig().getBoolean("warnings.enabled", true)) {
            sender.sendMessage(miniMessage.deserialize(plugin.getConfig().getString("warnings.messages.disabled", "<red>Warnings are currently disabled.")));
            return true;
        }
        if (!sender.hasPermission("lpc.warnings")) {
            sender.sendMessage(miniMessage.deserialize(plugin.getConfig().getString("general-messages.no-permission", "<red>No permission.")));
            return true;
        }
        if (args.length < 1) {
            sender.sendMessage(miniMessage.deserialize(plugin.getConfig().getString("general-messages.usage", "<red>Usage: {usage}").replace("{usage}", "/warnings <player>")));
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        List<Warning> warnings = plugin.getModerationManager().getWarnings(target.getUniqueId());

        if (warnings.isEmpty()) {
            sender.sendMessage(miniMessage.deserialize(plugin.getConfig().getString("warnings.messages.no-warnings", "<gray>No warnings for {player}").replace("{player}", target.getName() != null ? target.getName() : args[0])));
            return true;
        }

        sender.sendMessage(miniMessage.deserialize(plugin.getConfig().getString("warnings.messages.header", "<gold>Warnings for {player}:").replace("{player}", target.getName() != null ? target.getName() : args[0])));
        for (Warning w : warnings) {
            sender.sendMessage(miniMessage.deserialize(plugin.getConfig().getString("warnings.messages.item", "<yellow>#{id} - <white>{reason} <gray>({date})")
                    .replace("{id}", String.valueOf(w.getId()))
                    .replace("{reason}", w.getReason())
                    .replace("{date}", new Date(w.getTimestamp()).toString())));
        }
        return true;
    }

    private boolean handleDelWarn(CommandSender sender, String[] args) {
        if (!plugin.getConfig().getBoolean("warnings.enabled", true)) {
            sender.sendMessage(miniMessage.deserialize(plugin.getConfig().getString("warnings.messages.disabled", "<red>Warnings are currently disabled.")));
            return true;
        }
        if (!sender.hasPermission("lpc.delwarn")) {
            sender.sendMessage(miniMessage.deserialize(plugin.getConfig().getString("general-messages.no-permission", "<red>No permission.")));
            return true;
        }
        if (args.length < 1) {
            sender.sendMessage(miniMessage.deserialize(plugin.getConfig().getString("general-messages.usage", "<red>Usage: {usage}").replace("{usage}", "/delwarn <id>")));
            return true;
        }

        try {
            int id = Integer.parseInt(args[0]);
            plugin.getModerationManager().deleteWarning(id);
            sender.sendMessage(miniMessage.deserialize(plugin.getConfig().getString("warnings.messages.deleted", "<green>Warning #{id} deleted.").replace("{id}", args[0])));
        } catch (NumberFormatException e) {
            sender.sendMessage(miniMessage.deserialize(plugin.getConfig().getString("warnings.messages.invalid-id", "<red>Invalid ID.")));
        }
        return true;
    }

    private boolean handleProfile(CommandSender sender, String[] args) {
        if (!plugin.getConfig().getBoolean("profiles.enabled", true)) {
            sender.sendMessage(miniMessage.deserialize(plugin.getConfig().getString("profiles.messages.disabled", "<red>Profiles are currently disabled.")));
            return true;
        }
        OfflinePlayer target;
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                plugin.getAdventure().sender(sender).sendMessage(miniMessage.deserialize(plugin.getConfig().getString("general-messages.usage", "<red>Usage: {usage}").replace("{usage}", "/profile <player>")));
                return true;
            }
            target = (Player) sender;
        } else {
            if (!sender.hasPermission("lpc.profile.others")) {
                sender.sendMessage(miniMessage.deserialize(plugin.getConfig().getString("general-messages.no-permission", "<red>No permission.")));
                return true;
            }
            target = Bukkit.getOfflinePlayer(args[0]);
        }

        String bio = plugin.getModerationManager().getBio(target.getUniqueId());
        List<Warning> warnings = plugin.getModerationManager().getWarnings(target.getUniqueId());
        Mute mute = plugin.getModerationManager().getActiveMute(target.getUniqueId());

        sender.sendMessage(miniMessage.deserialize(plugin.getConfig().getString("profiles.messages.header", "<gold>Profile: {player}").replace("{player}", target.getName() != null ? target.getName() : args[0])));
        sender.sendMessage(miniMessage.deserialize(plugin.getConfig().getString("profiles.messages.bio", "<gray>Bio: <white>{bio}").replace("{bio}", bio.isEmpty() ? plugin.getConfig().getString("profiles.messages.no-bio", "No bio set.") : bio)));
        sender.sendMessage(miniMessage.deserialize(plugin.getConfig().getString("profiles.messages.warnings", "<gray>Warnings: <white>{count}").replace("{count}", String.valueOf(warnings.size()))));
        if (mute != null) {
            String time = mute.isPermanent() ? "Permanent" : formatTimeLeft(mute.getExpiry() - System.currentTimeMillis());
            sender.sendMessage(miniMessage.deserialize(plugin.getConfig().getString("profiles.messages.mute", "<gray>Mute: <red>{reason} (Expires: {time})")
                    .replace("{reason}", mute.getReason())
                    .replace("{time}", time)));
        }
        return true;
    }

    private boolean handleSetBio(CommandSender sender, String[] args) {
        if (!plugin.getConfig().getBoolean("profiles.enabled", true)) {
            sender.sendMessage(miniMessage.deserialize(plugin.getConfig().getString("profiles.messages.disabled", "<red>Profiles are currently disabled.")));
            return true;
        }
        if (!(sender instanceof Player)) {
            plugin.getAdventure().sender(sender).sendMessage(miniMessage.deserialize(plugin.getConfig().getString("general-messages.only-players", "Only players can use this command.")));
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage(miniMessage.deserialize(plugin.getConfig().getString("general-messages.usage", "<red>Usage: {usage}").replace("{usage}", "/setbio <text>")));
            return true;
        }

        Player player = (Player) sender;
        String bio = String.join(" ", args);
        plugin.getModerationManager().setBio(player.getUniqueId(), bio);
        sender.sendMessage(miniMessage.deserialize(plugin.getConfig().getString("profiles.messages.bio-set", "<green>Bio updated!")));
        return true;
    }

    private boolean handleSlowmode(CommandSender sender, String[] args) {
        if (!sender.hasPermission("lpc.slowmode.set")) {
            sender.sendMessage(miniMessage.deserialize(plugin.getConfig().getString("general-messages.no-permission", "<red>No permission.")));
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage(miniMessage.deserialize(plugin.getConfig().getString("general-messages.usage", "<red>Usage: {usage}").replace("{usage}", "/slowmode <player> <seconds>")));
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        try {
            double seconds = Double.parseDouble(args[1]);
            plugin.getModerationManager().setSlowmode(target.getUniqueId(), seconds);
            sender.sendMessage(miniMessage.deserialize(plugin.getConfig().getString("moderation.messages.slowmode-set", "<green>Slowmode for {player} set to {seconds}s.")
                    .replace("{player}", target.getName() != null ? target.getName() : args[0])
                    .replace("{seconds}", String.valueOf(seconds))));
        } catch (NumberFormatException e) {
            sender.sendMessage(miniMessage.deserialize(plugin.getConfig().getString("moderation.messages.invalid-seconds", "<red>Invalid seconds.")));
        }
        return true;
    }

    private long parseDuration(String duration) {
        Pattern pattern = Pattern.compile("(\\d+)([smhd])");
        Matcher matcher = pattern.matcher(duration.toLowerCase());
        long totalMillis = 0;
        while (matcher.find()) {
            long value = Long.parseLong(matcher.group(1));
            String unit = matcher.group(2);
            switch (unit) {
                case "s": totalMillis += value * 1000; break;
                case "m": totalMillis += value * 60 * 1000; break;
                case "h": totalMillis += value * 60 * 60 * 1000; break;
                case "d": totalMillis += value * 24 * 60 * 60 * 1000; break;
            }
        }
        return totalMillis;
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

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            if (command.getName().equalsIgnoreCase("setbio") || command.getName().equalsIgnoreCase("delwarn")) return null;
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).filter(n -> n.toLowerCase().startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        if (args.length == 2 && command.getName().equalsIgnoreCase("mute")) {
            return Arrays.asList("10m", "1h", "1d");
        }
        return Collections.emptyList();
    }
}
