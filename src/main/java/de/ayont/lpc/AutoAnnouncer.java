package de.ayont.lpc;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class AutoAnnouncer extends BukkitRunnable {

    private final LPC plugin;
    private final MiniMessage miniMessage;
    private int currentIndex = 0;
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

        String strategy = plugin.getConfig().getString("announcer.selection-strategy", "ordered").toLowerCase();
        String messageToSend;

        switch (strategy) {
            case "random":
                messageToSend = messages.get(random.nextInt(messages.size()));
                break;
            case "reverse": // bottom to top
                 // Wait, reverse order means iterating from end to start?
                 // Or just picking messages in reverse order?
                 // If "ordered" is 0 -> size-1.
                 // "reverse" is size-1 -> 0.
                 // I need to track index.
                 // currentIndex starts at 0.
                 // Wait, if I use index logic, I need to know direction.
                 // Let's assume ordered means 0, 1, 2...
                 // Reverse means 2, 1, 0...
                 // But typically "reverse" implies just reversing the list once.
                 // I'll implement cycling backwards.
                 if (currentIndex < 0) {
                     currentIndex = messages.size() - 1;
                 }
                 if (currentIndex >= messages.size()) { // Should not happen but safe
                     currentIndex = messages.size() - 1;
                 }
                 messageToSend = messages.get(currentIndex);
                 currentIndex--;
                 if (currentIndex < 0) {
                     currentIndex = messages.size() - 1;
                 }
                 break;
            case "ordered":
            default:
                if (currentIndex >= messages.size()) {
                    currentIndex = 0;
                }
                messageToSend = messages.get(currentIndex);
                currentIndex++;
                break;
        }

        // Broadcast to all players using Adventure
        Audience audience = plugin.getAdventure().all();
        audience.sendMessage(miniMessage.deserialize(messageToSend));
    }
}
