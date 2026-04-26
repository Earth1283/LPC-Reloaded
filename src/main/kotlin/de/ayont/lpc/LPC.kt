package de.ayont.lpc

import de.ayont.lpc.channels.ChannelManager
import de.ayont.lpc.commands.*
import de.ayont.lpc.listener.AsyncChatListener
import de.ayont.lpc.listener.JoinQuitListener
import de.ayont.lpc.listener.ShortcutListener
import de.ayont.lpc.listener.SpigotChatListener
import de.ayont.lpc.managers.PlayerSettingsManager
import de.ayont.lpc.managers.PrivateMessageManager
import de.ayont.lpc.moderation.ModerationManager
import de.ayont.lpc.storage.Storage
import de.ayont.lpc.storage.impl.InMemoryStorage
import de.ayont.lpc.storage.impl.MySQLStorage
import de.ayont.lpc.storage.impl.SQLiteStorage
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.command.CommandExecutor
import org.bukkit.command.TabCompleter
import org.bukkit.plugin.java.JavaPlugin
import java.util.logging.Level

class LPC : JavaPlugin() {
    var isPaper = false
        private set
    
    val privateMessageManager = PrivateMessageManager()
    val playerSettingsManager = PlayerSettingsManager()
    
    var adventure: BukkitAudiences? = null
        private set
        
    var chatBubbleManager: ChatBubbleManager? = null
        private set
        
    var channelManager: ChannelManager? = null
        private set
        
    var chatRendererUtil: de.ayont.lpc.renderer.ChatRendererUtil? = null
        private set
        
    var moderationManager: ModerationManager? = null
        private set
        
    var lpcStorage: Storage? = null
        private set

    companion object {
        @JvmStatic
        val legacySerializer: LegacyComponentSerializer = LegacyComponentSerializer.builder()
            .character('§')
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build()
    }

    override fun onEnable() {
        val pluginId = 29570
        Metrics(this, pluginId)

        this.adventure = BukkitAudiences.create(this)
        this.isPaper = checkIfPaper()
        this.chatRendererUtil = de.ayont.lpc.renderer.ChatRendererUtil(this)
        this.chatBubbleManager = ChatBubbleManager(this)

        saveDefaultConfig()
        config.options().copyDefaults(true)
        saveConfig()

        initStorage()

        this.moderationManager = ModerationManager(this)

        if (config.getBoolean("channels.enabled", false)) {
            this.channelManager = ChannelManager(this)
            this.channelManager?.init()
        }

        registerCommand()
        registerListeners()

        val interval = config.getInt("announcer.interval", 300) * 20
        if (interval > 0) {
            AutoAnnouncer(this).runTaskTimerAsynchronously(this, interval.toLong(), interval.toLong())
        }
    }

    override fun onDisable() {
        this.channelManager?.shutdown()
        this.lpcStorage?.shutdown()
        this.adventure?.close()
        this.adventure = null
    }

    private fun initStorage() {
        val type = config.getString("channels.storage.type", "MEMORY")!!.uppercase()
        try {
            lpcStorage = when (type) {
                "SQLITE" -> SQLiteStorage(this, config.getString("channels.storage.file", "channels.db")!!)
                "MYSQL" -> MySQLStorage(
                    this,
                    config.getString("channels.storage.mysql.host", "localhost")!!,
                    config.getInt("channels.storage.mysql.port", 3306),
                    config.getString("channels.storage.mysql.database", "lpc")!!,
                    config.getString("channels.storage.mysql.username", "root")!!,
                    config.getString("channels.storage.mysql.password", "")!!,
                    config.getString("channels.storage.mysql.table-prefix", "lpc_")!!
                )
                else -> InMemoryStorage()
            }
            lpcStorage?.init()
        } catch (e: Exception) {
            logger.log(Level.SEVERE, "Failed to initialize storage: $type", e)
            lpcStorage = InMemoryStorage()
            lpcStorage?.init()
        }
    }

    override fun reloadConfig() {
        super.reloadConfig()
        config.options().copyDefaults(true)
        saveConfig()
    }

    private fun registerCommand() {
        register("lpc", LPCCommand(this))

        val pmCommand = PrivateMessageCommand(this)
        register("msg", pmCommand)
        register("reply", pmCommand)

        register("socialspy", SocialSpyCommand(this))
        register("ignore", IgnoreCommand(this))
        register("staffchat", StaffChatCommand(this))
        register("chatclear", ChatClearCommand(this))

        if (this.channelManager != null) {
            register("channel", ChannelCommand(this))
        }

        val modCommand = ModerationCommand(this)
        val modCommands = arrayOf("mute", "unmute", "warn", "warnings", "delwarn", "profile", "setbio", "slowmode")
        for (cmd in modCommands) {
            register(cmd, modCommand)
        }
    }

    private fun register(name: String, executor: CommandExecutor) {
        val command = getCommand(name)
        if (command != null) {
            command.setExecutor(executor)
            if (executor is TabCompleter) {
                command.tabCompleter = executor
            }
        }
    }

    private fun checkIfPaper(): Boolean {
        return try {
            Class.forName("io.papermc.paper.event.player.AsyncChatEvent")
            logger.info("Paper API has been detected and will be used.")
            true
        } catch (e: ClassNotFoundException) {
            logger.info("Spigot API has been detected and will be used.")
            false
        }
    }

    private fun registerListeners() {
        if (isPaper) {
            server.pluginManager.registerEvents(AsyncChatListener(this), this)
        } else {
            server.pluginManager.registerEvents(SpigotChatListener(this), this)
        }
        server.pluginManager.registerEvents(JoinQuitListener(this), this)

        if (channelManager != null) {
            server.pluginManager.registerEvents(ShortcutListener(this), this)
        }
    }
}
