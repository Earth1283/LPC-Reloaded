package de.ayont.lpc.renderer;

import de.ayont.lpc.LPC;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SpigotChatRenderer {
    private final LPC plugin;

    public SpigotChatRenderer(LPC plugin) {
        this.plugin = plugin;
    }

    public @NotNull Component render(Player source, String message, Audience viewer) {
        return plugin.getChatRendererUtil().render(source, message, viewer, null);
    }
}
