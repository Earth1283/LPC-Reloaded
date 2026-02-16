package de.ayont.lpc.commands;

import de.ayont.lpc.LPC;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SocialSpyCommand implements CommandExecutor {

    private final LPC plugin;
    private final MiniMessage miniMessage;

    public SocialSpyCommand(LPC plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            plugin.getAdventure().sender(sender).sendMessage(miniMessage.deserialize(plugin.getConfig().getString("general-messages.only-players", "Only players can use this command.")));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("lpc.socialspy")) {
            player.sendMessage(miniMessage.deserialize(plugin.getConfig().getString("general-messages.no-permission", "<red>You do not have permission to use this command.")));
            return true;
        }

        boolean newState = !plugin.isSocialSpy(player.getUniqueId());
        plugin.setSocialSpy(player.getUniqueId(), newState);

        if (newState) {
            player.sendMessage(miniMessage.deserialize(plugin.getConfig().getString("social-spy.messages.enabled", "<green>Social Spy enabled.")));
        } else {
            player.sendMessage(miniMessage.deserialize(plugin.getConfig().getString("social-spy.messages.disabled", "<red>Social Spy disabled.")));
        }

        return true;
    }
}
