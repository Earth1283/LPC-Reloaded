package de.ayont.lpc.commands

import de.ayont.lpc.LPC
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class ChatClearCommand(private val plugin: LPC) : CommandExecutor {

    private val miniMessage = MiniMessage.miniMessage()

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("lpc.chatclear")) {
            sender.sendMessage(miniMessage.deserialize(plugin.config.getString("general-messages.no-permission", "<red>You do not have permission to use this command.")!!))
            return true
        }

        val lines = plugin.config.getInt("chat-clear.lines", 100)
        val blankLines = StringBuilder()
        for (i in 0 until lines) {
            blankLines.append(" \n")
        }
        val blanks = blankLines.toString()

        for (player in Bukkit.getOnlinePlayers()) {
            if (!player.hasPermission("lpc.chatclear.bypass")) {
                player.sendMessage(blanks)
            }
        }

        val broadcastMsg = plugin.config.getString("chat-clear.broadcast", "<red>Chat cleared by {staff}.")!!
            .replace("{staff}", sender.name)
        plugin.adventure?.all()?.sendMessage(miniMessage.deserialize(broadcastMsg))

        return true
    }
}
