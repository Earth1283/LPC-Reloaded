package de.ayont.lpc.channels;

import de.ayont.lpc.LPC;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class RangeChannel extends BaseChannel {

    private final double range;

    public RangeChannel(LPC plugin, String id, ConfigurationSection section) {
        super(plugin, id, section);
        this.range = section.getDouble("range", 100.0);
    }

    @Override
    public boolean canRead(Player player, Player sender) {
        if (!player.getWorld().equals(sender.getWorld())) return false;
        return player.getLocation().distanceSquared(sender.getLocation()) <= range * range;
    }
}
