package de.ayont.lpc.channels

import de.ayont.lpc.LPC
import net.kyori.adventure.text.minimessage.MiniMessage
import net.luckperms.api.LuckPermsProvider
import org.bukkit.Bukkit
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player

abstract class BaseChannel(
    protected val plugin: LPC,
    private val id: String,
    section: ConfigurationSection
) : Channel {

    private val format: String = section.getString("format", "{prefix}{name} » {message}")!!
    private val permission: String = section.getString("permission", "")!!
    private val shortcut: String = section.getString("shortcut", "")!!
    private val symbol: String = section.getString("symbol", "")!!
    protected val miniMessage: MiniMessage = MiniMessage.miniMessage()
    private val luckPerms = LuckPermsProvider.get()

    override fun getId(): String = id

    override fun getName(): String = id

    override fun getFormat(): String = format

    override fun getPermission(): String = permission

    override fun getShortcut(): String = shortcut

    override fun getSymbol(): String = symbol

    override fun canJoin(player: Player): Boolean {
        return permission.isEmpty() || player.hasPermission(permission)
    }

    override fun canRead(player: Player, sender: Player): Boolean {
        // By default, anyone with join permission can read.
        // Range checks will override this.
        return canJoin(player)
    }

    override fun sendMessage(sender: Player, message: String) {
        if (!canJoin(sender)) {
            sender.sendMessage(miniMessage.deserialize(plugin.config.getString("channels.messages.no-permission-speak", "<red>You do not have permission to speak in this channel.")!!))
            return
        }

        val renderer = plugin.chatRendererUtil ?: return

        for (recipient in Bukkit.getOnlinePlayers()) {
            if (canRead(recipient, sender)) {
                val component = renderer.render(sender, message, recipient, id)
                if (plugin.isPaper) {
                    recipient.sendMessage(component)
                } else {
                    recipient.sendMessage(LPC.legacySerializer.serialize(component))
                }
            }
        }

        // Log to console
        val console = plugin.adventure?.console() ?: return
        val consoleComponent = renderer.render(sender, message, console, id)
        if (plugin.isPaper) {
            console.sendMessage(consoleComponent)
        } else {
            plugin.logger.info(LPC.legacySerializer.serialize(consoleComponent))
        }
    }

    override fun onJoin(player: Player) {
        // Optional: Send "You joined X channel"
    }
}
