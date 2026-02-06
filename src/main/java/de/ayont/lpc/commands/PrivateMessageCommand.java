package de.ayont.lpc.commands;

import de.ayont.lpc.LPC;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class PrivateMessageCommand implements CommandExecutor, TabCompleter {

    private final LPC plugin;
    private final MiniMessage miniMessage;
    private final BukkitAudiences adventure;

    public PrivateMessageCommand(LPC plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
        this.adventure = plugin.getAdventure();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;
        UUID targetUuid = null;
        Player target = null;
        String message = "";

        if (command.getName().equalsIgnoreCase("msg")) {
            if (!player.hasPermission("lpc.msg")) {
                player.sendMessage(miniMessage.deserialize("<red>You do not have permission to use this command."));
                return true;
            }
            if (args.length < 2) {
                return false;
            }
            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                player.sendMessage("Player not found.");
                return true;
            }
            targetUuid = target.getUniqueId();
            message = String.join(" ", args).substring(args[0].length() + 1);

        } else if (command.getName().equalsIgnoreCase("reply")) {
            if (!player.hasPermission("lpc.reply")) {
                player.sendMessage(miniMessage.deserialize("<red>You do not have permission to use this command."));
                return true;
            }
            if (args.length < 1) {
                return false;
            }
            targetUuid = plugin.getLastMessaged(player.getUniqueId());
            if (targetUuid == null) {
                player.sendMessage("You have nobody to reply to.");
                return true;
            }
            target = Bukkit.getPlayer(targetUuid);
            if (target == null) {
                player.sendMessage("Player is no longer online.");
                return true;
            }
            message = String.join(" ", args);
        }

        if (target != null) {
            sendMessage(player, target, message);
            plugin.setLastMessaged(player.getUniqueId(), target.getUniqueId());
            plugin.setLastMessaged(target.getUniqueId(), player.getUniqueId());
            return true;
        }

        return false;
    }

    private void sendMessage(Player sender, Player receiver, String message) {
        String senderFormat = plugin.getConfig().getString("private-message-formats.sender", "<gray>You to {receiver}: <white>{message}");
        String receiverFormat = plugin.getConfig().getString("private-message-formats.receiver", "<gray>{sender} to You: <white>{message}");

        senderFormat = replacePlaceholders(senderFormat, sender, receiver, message);
        receiverFormat = replacePlaceholders(receiverFormat, sender, receiver, message);

        // Parse with MiniMessage
        Component senderComponent = miniMessage.deserialize(senderFormat);
        Component receiverComponent = miniMessage.deserialize(receiverFormat);

        // Send
        adventure.player(sender).sendMessage(senderComponent);
        adventure.player(receiver).sendMessage(receiverComponent);
    }

    private String replacePlaceholders(String format, Player sender, Player receiver, String message) {
        // Basic Placeholders
        format = format.replace("{sender}", sender.getName())
                       .replace("{receiver}", receiver.getName())
                       .replace("{message}", message);

        // PAPI support (if enabled/available)
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
             format = PlaceholderAPI.setPlaceholders(sender, format);
        }
        
        return format;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (command.getName().equalsIgnoreCase("msg") && args.length == 1) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
