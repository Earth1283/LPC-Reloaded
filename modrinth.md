# LPC – LuckPerms Chat Formatter Reloaded ✨

![LPC Banner](https://cdn.varilx.de/raw/fwtRZS.png)

LPC (LuckPerms Chat) is a **powerful, modern, and flexible chat formatting plugin** for Spigot and Paper servers. Designed specifically for **LuckPerms**, it replaces outdated legacy formatting with a rich, **MiniMessage-powered** system. Enhance your Minecraft server's social experience with gradients, chat bubbles, and advanced channel management.

## 🔧 Why Choose LPC Reloaded?

LPC is optimized for performance and built for modern Minecraft versions. It bridges the gap between complex permission systems and beautiful chat aesthetics.

- **LuckPerms Integration**: Native support for primary groups, prefixes, suffixes, and custom metadata.
- **Paper & Spigot Support**: Works seamlessly across both platforms with advanced Paper-only features.
- **SEO Optimized**: The most complete chat solution for 1.19.4, 1.20, and 1.21+ servers.

## ✅ Latest Features

- **Modern MiniMessage Formatting**: Full support for [MiniMessage](https://docs.advntr.dev/minimessage/format.html) (Gradients, Hex colors, Hover text, and Click events).
- **Immersive Chat Bubbles**: Text Display entities appear above players' heads. Fully customizable duration, scale, and transparency.
- **Advanced Chat Channels**:
    - **Global, Local (Range-based), and Staff channels**.
    - **Per-Channel Formatting**: Unique formats for different chat streams.
    - **Group-Specific Channel Formats**: Show different styles based on LuckPerms groups *inside* specific channels.
    - **Persistent Storage**: SQLite or MySQL support for player channel preferences.
- **[item] Showcase**: Display your held item with a vanilla-style hover tooltip (shows NBT, enchantments, and lore).
- **Global Group & Track Logic**: Assign unique formats based on LuckPerms groups or tracks with priority handling.
- **Smart Mentions**: Highlight player names in chat with customizable sounds and visuals.
- **Automated Moderation**:
    - **Regex Filters**: Block Discord invites, links, and custom words.
    - **Caps Control & Cooldowns**: Keep your chat clean and spam-free.
- **Social Spy**: Staff can monitor private messages with clear, professional formatting.

## 🧑‍💼 Permissions & Commands

| Feature | Permission Node | Command |
| :--- | :--- | :--- |
| **Reload Config** | `lpc.reload` | `/lpc reload` |
| **Toggle Bubbles** | `lpc.bubbles.toggle` | `/lpc bubbles` |
| **Switch Channels** | `lpc.channel.use` | `/channel <name>` |
| **MiniMessage** | `lpc.chatcolor` | (Automatic) |
| **Item Showcase** | `lpc.itemplaceholder` | `[item]` |
| **Staff Chat** | `lpc.staffchat` | `/sc <message>` |
| **Social Spy** | `lpc.socialspy` | `/socialspy` |
| **Warn/Mute** | `lpc.warn` / `lpc.mute` | `/warn`, `/mute` |

## 🪄 Supported Placeholders

LPC supports standard placeholders and integrates with **PlaceholderAPI**.

- `{message}`: The chat message.
- `{name}` / `{displayname}`: Sender's name.
- `{prefix}` / `{suffix}`: Highest priority LuckPerms metadata.
- `{prefixes}` / `{suffixes}`: Combined list of all prefixes/suffixes.
- `{world}`: Current world of the player.
- `{username-color}` / `{message-color}`: Defined via LuckPerms meta.
- **[item]**: Renders the held item.

## 🚀 Quick Setup

1. **Download**: Place the `LPC.jar` in your `/plugins` folder.
2. **Requirements**: Ensure **LuckPerms** is installed.
3. **Configure**: Edit `config.yml` to define your `group-formats` and `channels`.
4. **Reload**: Use `/lpc reload` to apply changes without restarting.

---

*LPC is not affiliated with LuckPerms. For support, please visit our GitHub or Discord. Optimized for Minecraft 1.20.x and 1.21.x.*
