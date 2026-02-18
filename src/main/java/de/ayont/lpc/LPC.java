package de.ayont.lpc;

import de.ayont.lpc.channels.ChannelManager;
import de.ayont.lpc.commands.*;
import de.ayont.lpc.listener.AsyncChatListener;
import de.ayont.lpc.listener.JoinQuitListener;
import de.ayont.lpc.listener.ShortcutListener;
import de.ayont.lpc.listener.SpigotChatListener;
import de.ayont.lpc.moderation.ModerationManager;
import de.ayont.lpc.storage.Storage;
import de.ayont.lpc.storage.impl.InMemoryStorage;
import de.ayont.lpc.storage.impl.MySQLStorage;
import de.ayont.lpc.storage.impl.SQLiteStorage;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public final class LPC extends JavaPlugin {
    private boolean isPaper;
    private final Map<UUID, UUID> lastMessaged = new HashMap<>();
    private final java.util.Set<UUID> socialSpyEnabled = new java.util.HashSet<>();
    private final java.util.Set<UUID> chatBubblesDisabled = new java.util.HashSet<>();
    private final Map<UUID, java.util.Set<UUID>> ignoredPlayers = new HashMap<>();
    private BukkitAudiences adventure;
    private ChatBubbleManager chatBubbleManager;
    private ChannelManager channelManager;
    private de.ayont.lpc.renderer.ChatRendererUtil chatRendererUtil;
    private ModerationManager moderationManager;
    private Storage storage;

    private static final LegacyComponentSerializer legacySerializer = LegacyComponentSerializer.builder()
            .character('§')
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();

    public static LegacyComponentSerializer getLegacySerializer() {
        return legacySerializer;
    }

    public de.ayont.lpc.renderer.ChatRendererUtil getChatRendererUtil() {
        return chatRendererUtil;
    }

    public boolean isPaper() {
        return isPaper;
    }

    public boolean isSocialSpy(UUID uuid) {
        return socialSpyEnabled.contains(uuid);
    }

    public void setSocialSpy(UUID uuid, boolean enabled) {
        if (enabled) {
            socialSpyEnabled.add(uuid);
        } else {
            socialSpyEnabled.remove(uuid);
        }
    }

    public void ignorePlayer(UUID ignorer, UUID target) {
        ignoredPlayers.computeIfAbsent(ignorer, k -> new java.util.HashSet<>()).add(target);
    }

    public void unignorePlayer(UUID ignorer, UUID target) {
        if (ignoredPlayers.containsKey(ignorer)) {
            ignoredPlayers.get(ignorer).remove(target);
            if (ignoredPlayers.get(ignorer).isEmpty()) {
                ignoredPlayers.remove(ignorer);
            }
        }
    }

    public boolean isIgnored(UUID ignorer, UUID target) { // Does ignorer ignore target?
        return ignoredPlayers.containsKey(ignorer) && ignoredPlayers.get(ignorer).contains(target);
    }

    public boolean isChatBubblesEnabled(UUID uuid) {
        return !chatBubblesDisabled.contains(uuid);
    }

    public void setChatBubblesEnabled(UUID uuid, boolean enabled) {
        if (enabled) {
            chatBubblesDisabled.remove(uuid);
        } else {
            chatBubblesDisabled.add(uuid);
        }
    }

    public ChatBubbleManager getChatBubbleManager() {
        return chatBubbleManager;
    }

    public ChannelManager getChannelManager() {
        return channelManager;
    }

    public ModerationManager getModerationManager() {
        return moderationManager;
    }

    public Storage getStorage() {
        return storage;
    }

    public BukkitAudiences getAdventure() {
        if(this.adventure == null) {
            throw new IllegalStateException("Adventure is not initialized!");
        }
        return adventure;
    }

    @Override
    public void onEnable() {
        // Metrics
        int pluginId = 29570;
        Metrics metrics = new Metrics(this, pluginId);

        this.adventure = BukkitAudiences.create(this);
        this.isPaper = checkIfPaper();
        this.chatRendererUtil = new de.ayont.lpc.renderer.ChatRendererUtil(this);
        this.chatBubbleManager = new ChatBubbleManager(this);
        
        saveDefaultConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();

        initStorage();

        this.moderationManager = new ModerationManager(this);
        
        if (getConfig().getBoolean("channels.enabled", false)) {
            this.channelManager = new ChannelManager(this);
            this.channelManager.init();
        }

        registerCommand();
        registerListeners();

        int interval = getConfig().getInt("announcer.interval", 300) * 20;
        if (interval > 0) {
            new AutoAnnouncer(this).runTaskTimerAsynchronously(this, interval, interval);
        }
    }

    @Override
    public void onDisable() {
        if (this.channelManager != null) {
            this.channelManager.shutdown();
        }
        if (this.storage != null) {
            this.storage.shutdown();
        }
        if(this.adventure != null) {
            this.adventure.close();
            this.adventure = null;
        }
    }

    private void initStorage() {
        String type = getConfig().getString("channels.storage.type", "MEMORY").toUpperCase();
        try {
            switch (type) {
                case "SQLITE":
                    storage = new SQLiteStorage(this, getConfig().getString("channels.storage.file", "channels.db"));
                    break;
                case "MYSQL":
                    storage = new MySQLStorage(this,
                            getConfig().getString("channels.storage.mysql.host", "localhost"),
                            getConfig().getInt("channels.storage.mysql.port", 3306),
                            getConfig().getString("channels.storage.mysql.database", "lpc"),
                            getConfig().getString("channels.storage.mysql.username", "root"),
                            getConfig().getString("channels.storage.mysql.password", ""),
                            getConfig().getString("channels.storage.mysql.table-prefix", "lpc_"));
                    break;
                case "MEMORY":
                default:
                    storage = new InMemoryStorage();
                    break;
            }
            storage.init();
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to initialize storage: " + type, e);
            storage = new InMemoryStorage(); // Fallback
        }
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();
    }

    public void registerCommand() {
        register("lpc", new LPCCommand(this));

        PrivateMessageCommand pmCommand = new PrivateMessageCommand(this);
        register("msg", pmCommand);
        register("reply", pmCommand);

        register("socialspy", new SocialSpyCommand(this));
        register("ignore", new IgnoreCommand(this));
        register("staffchat", new StaffChatCommand(this));
        register("chatclear", new ChatClearCommand(this));
        
        if (this.channelManager != null) {
            register("channel", new ChannelCommand(this));
        }

        // Moderation Commands
        ModerationCommand modCommand = new ModerationCommand(this);
        String[] modCommands = {"mute", "unmute", "warn", "warnings", "delwarn", "profile", "setbio", "slowmode"};
        for (String cmd : modCommands) {
            register(cmd, modCommand);
        }
    }

    private void register(String name, CommandExecutor executor) {
        PluginCommand command = getCommand(name);
        if (command != null) {
            command.setExecutor(executor);
            if (executor instanceof TabCompleter) {
                command.setTabCompleter((TabCompleter) executor);
            }
        }
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
        getServer().getPluginManager().registerEvents(new JoinQuitListener(this), this);
        
        if (channelManager != null) {
            getServer().getPluginManager().registerEvents(new ShortcutListener(this), this);
        }
    }
}
