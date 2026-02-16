# LPC – LuckPerms Chat Formatter Reloaded ✨

![LPC Banner](https://cdn.varilx.de/raw/fwtRZS.png)

LPC is a powerful, modern, and flexible chat formatting plugin with full **MiniMessage** support, designed specifically to work seamlessly with **LuckPerms**. It replaces legacy formatting with a rich, gradient-capable system and adds immersive features like chat bubbles and channels.

## 🔧 Requirements

- **LuckPerms** (Required) – Permissions & Metadata provider
- **PlaceholderAPI** (Optional) – For additional variable support
- **Minecraft 1.19.4+** – Required for Chat Bubble features (Text Displays)

## ✅ Features

- **Modern Formatting**: Full [MiniMessage](https://docs.advntr.dev/minimessage/format.html) support (Gradients, Hex, Hover/Click events).
- **Chat Bubbles**: Immersive Text Display entities above players' heads. Fully customizable and toggleable by players.
- **Chat Channels**: Flexible Global, Local (range-based), and Staff channels with persistent storage (SQLite/MySQL).
- **[item] Showcase**: Display your held item in chat with a full vanilla-style hover tooltip (shows NBT, enchantments, and lore).
- **Group & Track Logic**: Assign different chat formats based on LuckPerms groups or tracks.
- **Social Spy**: Monitor private messages with high-quality formatting.
- **Advanced Filters**: Built-in Regex filters for anti-discord, anti-link, and customizable word blocking.
- **Auto-Announcer**: Scheduled, multi-line announcements with gradient support.

## 🧑‍💼 Permissions

| Command / Feature | Permission Node | Description |
| :--- | :--- | :--- |
| `/lpc reload` | `lpc.reload` | Reloads the configuration |
| `/lpc bubbles` | `lpc.bubbles.toggle` | Toggle chat bubbles visibility |
| `/channel` | `lpc.channel.use` | Switch or message in channels |
| MiniMessage | `lpc.chatcolor` | Allows using MiniMessage in chat |
| `[item]` | `lpc.itemplaceholder` | Enables item showcasing |
| Staff Chat | `lpc.staffchat` | Access to staff channels |
| Social Spy | `lpc.socialspy` | Monitor private messages |

## ⚙️ Configuration Snippet

LPC uses a single, well-documented `config.yml`. Here is a look at the modern channel and bubble setup:

```yaml
# Chat Channels
channels:
  enabled: true
  storage:
    type: "SQLITE"
  list:
    global:
      format: "{prefix}{name}<dark_gray> »<reset> {message}"
      symbol: "!"
    local:
      type: "range"
      range: 100
      format: "<yellow>[L] {name}: <white>{message}"

# Chat Bubbles
chat-bubbles:
  enabled: true
  duration: 5.0
  background-color: "0,0,0,100"
```

## 🪄 Available Placeholders

- `{message}`: The chat message
- `{name}` / `{displayname}`: Player identity
- `{prefix}` / `{suffix}`: Highest priority LuckPerms meta
- `{prefixes}` / `{suffixes}`: Full sorted list of meta
- `{world}`: Current world name
- `{username-color}` / `{message-color} `: Meta-defined colors

## 🚀 Installation

1. Place `LPC.jar` into your `/plugins` folder.
2. Restart your server to generate files.
3. Configure your formats in `config.yml`.
4. Use `/lpc reload` to apply changes instantly!

---

*LPC is not affiliated with LuckPerms. For support, please visit our GitHub or Discord.*
