package de.ayont.lpc.channels;

import de.ayont.lpc.LPC;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.UUID;

public interface Channel {

    String getId();

    String getName();

    String getFormat();

    String getPermission();

    String getShortcut();

    String getSymbol();

    boolean canJoin(Player player);

    boolean canRead(Player player, @Nullable Player sender);

    void sendMessage(Player sender, String message);
    
    // For when the player just switches to the channel, send a confirmation message?
    void onJoin(Player player);
}
