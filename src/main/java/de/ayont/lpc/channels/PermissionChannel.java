package de.ayont.lpc.channels;

import de.ayont.lpc.LPC;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class PermissionChannel extends BaseChannel {

    public PermissionChannel(LPC plugin, String id, ConfigurationSection section) {
        super(plugin, id, section);
    }

    @Override
    public boolean canRead(Player player, Player sender) {
        return player.hasPermission(permission);
    }
}
