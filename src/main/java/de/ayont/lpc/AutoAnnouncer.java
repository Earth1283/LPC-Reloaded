package de.ayont.lpc;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class AutoAnnouncer extends BukkitRunnable {

    private final LPC plugin;
    private final MiniMessage miniMessage;
    private final AtomicInteger currentIndex = new AtomicInteger(0);
    private final Random random = new Random();

    public AutoAnnouncer(LPC plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
    }

    @Override
    public void run() {
        if (!plugin.getConfig().getBoolean("announcer.enabled", false)) {
            return;
        }

        List<String> messages = plugin.getConfig().getStringList("announcer.messages");
        if (messages.isEmpty()) {
            return;
        }

        String strategy = plugin.getConfig().getString("announcer.selection-strategy", "ordered");
        if (strategy == null) strategy = "ordered";
        strategy = strategy.toLowerCase();

        String messageToSend;
        int size = messages.size();

        switch (strategy) {
            case "random":
                messageToSend = messages.get(random.nextInt(size));
                break;
            case "reverse":
                 int revIdx = currentIndex.decrementAndGet();
                 if (revIdx < 0 || revIdx >= size) {
                     revIdx = size - 1;
                     currentIndex.set(revIdx);
                 }
                 messageToSend = messages.get(revIdx);
                 break;
            case "ordered":
            default:
                int ordIdx = currentIndex.getAndIncrement();
                if (ordIdx < 0 || ordIdx >= size) {
                    ordIdx = 0;
                    currentIndex.set(1);
                }
                messageToSend = messages.get(ordIdx);
                break;
        }

        // Broadcast to all players using Adventure
        plugin.getAdventure().all().sendMessage(miniMessage.deserialize(messageToSend));
    }
}
