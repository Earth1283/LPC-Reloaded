# LPC – LuckPerms Chat Formatter (The Good One) ✨

![LPC Banner](https://cdn.varilx.de/raw/fwtRZS.png)

Stop using chat plugins that look like they were written in 2012 and crash because someone's name is null. LPC Reloaded is a **Pro plugin, but it's free on all platforms**. We deliver every single meaningful Pro feature without charging you a cent—simply **because we can**. We've recently refactored the entire codebase to **Kotlin** for that sweet, sweet null safety and performance. We're licensed under the **GPL**, because we believe in actual open source.

## 🔧 Why LPC Reloaded is better than the "Pro" version?

While the `paid alternative <https://www.spigotmc.org/resources/122297/>`_ is busy charging you a monthly fee to bloat your server with features you never asked for, we're busy keeping things lean and functional. We've reached **95% feature parity** with the "Pro" version via a strictly **clean-room implementation**.

- **No "MiniMessage Native" Marketing BS**: We don't take credit for Paper's hard work. Advertising "Native MiniMessage" as a premium feature is like a car dealer bragging about "Native Rubber" on the tires. Paper does the heavy lifting; we just use the API correctly.
- **No GUI Menus**: We believe you're smart enough to edit a text file. We won't make you waste your life clicking around a chest GUI like you're playing a minigame just to change a chat format.
- **No "AI Moderation"**: We don't send your chat logs to OpenAI just to check for bad words. Your data stays on your server, where it belongs. 
- **No "Reputation Systems"**: This is Minecraft, not a social credit simulator. We leave the dystopian nightmares to the "Pro" guys.
- **No "Grammar Systems"**: If your players can't spell, a plugin won't fix their education.

## ✅ Core Features (That Actually Work)

- **Modern MiniMessage Formatting**: Full support for [MiniMessage](https://docs.advntr.dev/minimessage/format.html) (Gradients, Hex colors, Hover text, and Click events).
- **Immersive Chat Bubbles**: Text Display entities appear above players' heads. Fully customizable. Requires 1.19.4+ (welcome to the future).
- **Advanced Chat Channels**:
    - **Global, Local (Range-based), and Staff channels**.
    - **Group-Specific Channel Formats**: Unique styles based on LuckPerms groups *inside* specific channels.
    - **Persistent Storage**: SQLite or MySQL support for player channel preferences.
- **[item] Showcase**: Display your held item with a vanilla-style hover tooltip (shows NBT, enchantments, and lore).
- **Interactive Profiles**: Hover over a name in chat to see their rank, bio, and how many times they've been naughty (infractions). It's like LinkedIn, but for Minecraft.
- **Smart Mentions**: Highlight player names in chat with customizable sounds and visuals.
- **Automated Moderation**: Regex filters, caps control, and cooldowns.

## 🧑‍💼 Commands

- `/lpc reload`: Fixed a typo in the config? Run this to fix it without anyone noticing your shame.
- `/lpc bubbles`: Toggle chat bubbles visibility for yourself.
- `/channel <name>`: Switch or message in channels.
- `/msg <player> <message>`: Whisper sweet nothings to another player.
- `/r <message>`: Reply to the last person who messaged you.
- `/ignore <player>`: The ultimate "talk to the hand".
- `/socialspy`: Be a creep and read other people's private messages.

## 🚀 Quick Setup

1. **Download**: Place the `LPC.jar` in your `/plugins` folder.
2. **Requirements**: Ensure **LuckPerms** is installed and you're on **Java 21**.
3. **Configure**: Edit `config.yml`. Don't mess up the YAML spaces or the plugin will cry.
4. **Profit**: Go have a snack while the "Pro" users are still trying to figure out their GUI menus.

---

*Not affiliated with LuckPerms. They're busy doing actual work; we just make it look pretty. Optimized for Minecraft 1.20.x and 1.21.x.*
