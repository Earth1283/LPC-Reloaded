package de.ayont.lpc;

import de.ayont.lpc.commands.LPCCommand;
import de.ayont.lpc.commands.PrivateMessageCommand;
import de.ayont.lpc.listener.AsyncChatListener;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import de.ayont.lpc.listener.SpigotChatListener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public final class LPC extends JavaPlugin {
    private boolean isPaper;
    private final Map<UUID, UUID> lastMessaged = new HashMap<>();
    private BukkitAudiences adventure;

    private static final LegacyComponentSerializer legacySerializer = LegacyComponentSerializer.builder()
            .character('§')
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();

    public static LegacyComponentSerializer getLegacySerializer() {
        return legacySerializer;
    }

    public BukkitAudiences getAdventure() {
        if(this.adventure == null) {
            throw new IllegalStateException("Adventure is not initialized!");
        }
        return adventure;
    }

    @Override
    public void onEnable() {
        this.adventure = BukkitAudiences.create(this);
        this.isPaper = checkIfPaper();
        registerCommand();
        saveDefaultConfig();
        registerListeners();
    }

    @Override
    public void onDisable() {
        if(this.adventure != null) {
            this.adventure.close();
            this.adventure = null;
        }
    }

    public void registerCommand() {
        String commandName = "lpc";
        LPCCommand lpcCommand = new LPCCommand(this);

        this.getCommand(commandName).setExecutor(lpcCommand);
        this.getCommand(commandName).setTabCompleter(lpcCommand);

        PrivateMessageCommand pmCommand = new PrivateMessageCommand(this);
        this.getCommand("msg").setExecutor(pmCommand);
        this.getCommand("msg").setTabCompleter(pmCommand);
        this.getCommand("reply").setExecutor(pmCommand);
        this.getCommand("reply").setTabCompleter(pmCommand);
    }

    public void setLastMessaged(UUID sender, UUID receiver) {
        lastMessaged.put(sender, receiver);
    }

    public UUID getLastMessaged(UUID sender) {
        return lastMessaged.get(sender);
    }



    private boolean checkIfPaper() {
        try {
            Class.forName("io.papermc.paper.event.player.AsyncChatEvent");
            getLogger().info("Paper API has been detected and will be used.");
            return true;
        } catch (ClassNotFoundException e) {
            getLogger().info("Spigot API has been detected and will be used.");
            return false;
        }
    }

    private void registerListeners() {
        if (isPaper) {
            getServer().getPluginManager().registerEvents(new AsyncChatListener(this), this);
        } else {
            getServer().getPluginManager().registerEvents(new SpigotChatListener(this), this);
        }
    }

}