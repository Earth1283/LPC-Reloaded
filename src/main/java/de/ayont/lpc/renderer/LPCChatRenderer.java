package de.ayont.lpc.renderer;

import de.ayont.lpc.LPC;
import io.papermc.paper.chat.ChatRenderer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class LPCChatRenderer implements ChatRenderer {

    private final LPC plugin;

    public LPCChatRenderer(LPC plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull Component render(@NotNull Player source, @NotNull Component sourceDisplayName, @NotNull Component message, @NotNull Audience viewer) {
        String plainMessage = PlainTextComponentSerializer.plainText().serialize(message);
        return plugin.getChatRendererUtil().render(source, plainMessage, viewer, null);
    }
}
