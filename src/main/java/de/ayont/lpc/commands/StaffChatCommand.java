package de.ayont.lpc.commands;

import de.ayont.lpc.LPC;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class StaffChatCommand implements CommandExecutor {

    private final LPC plugin;
    private final MiniMessage miniMessage;

    public StaffChatCommand(LPC plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("lpc.staffchat")) {
            player.sendMessage(miniMessage.deserialize("<red>You do not have permission to use this command."));
            return true;
        }

        if (!plugin.getConfig().getBoolean("staff-chat.enabled", false)) {
             player.sendMessage(miniMessage.deserialize("<red>Staff chat is disabled."));
             return true;
        }

        if (args.length < 1) {
            player.sendMessage(miniMessage.deserialize("<red>Usage: /sc <message>"));
            return true;
        }

        String message = String.join(" ", args);
        String format = plugin.getConfig().getString("staff-chat.format", "<red>[Staff] {name}: <white>{message}");
        
        format = format.replace("{name}", player.getName())
                       .replace("{message}", message);

        String finalFormat = format;
        // Broadcast to staff
        plugin.getServer().getOnlinePlayers().stream()
                .filter(p -> p.hasPermission("lpc.staffchat"))
                .forEach(p -> plugin.getAdventure().player(p).sendMessage(miniMessage.deserialize(finalFormat)));
        
        // Also send to console?
        plugin.getAdventure().console().sendMessage(miniMessage.deserialize(finalFormat));

        return true;
    }
}
