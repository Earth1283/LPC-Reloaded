package de.ayont.lpc.commands;

import de.ayont.lpc.LPC;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class ChatClearCommand implements CommandExecutor {

    private final LPC plugin;
    private final MiniMessage miniMessage;

    public ChatClearCommand(LPC plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("lpc.chatclear")) {
            sender.sendMessage(miniMessage.deserialize("<red>You do not have permission to use this command."));
            return true;
        }

        int lines = plugin.getConfig().getInt("chat-clear.lines", 100);
        StringBuilder blankLines = new StringBuilder();
        for (int i = 0; i < lines; i++) {
            blankLines.append(" \n");
        }
        String blanks = blankLines.toString();

        for (org.bukkit.entity.Player player : Bukkit.getOnlinePlayers()) {
            if (!player.hasPermission("lpc.chatclear.bypass")) {
                player.sendMessage(blanks);
            }
        }

        String broadcastMsg = plugin.getConfig().getString("chat-clear.broadcast", "<red>Chat cleared by {staff}.");
        broadcastMsg = broadcastMsg.replace("{staff}", sender.getName());
        plugin.getAdventure().all().sendMessage(miniMessage.deserialize(broadcastMsg));

        return true;
    }
}
