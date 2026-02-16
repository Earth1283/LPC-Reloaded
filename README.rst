=========================================
LPC – LuckPerms Chat Formatter Reloaded ✨
=========================================

.. image:: https://cdn.varilx.de/raw/fwtRZS.png
   :alt: LPC Banner
   :align: center

.. image:: https://raw.githubusercontent.com/vLuckyyy/badges/main/avaiable-on-modrinth.svg
   :alt: Available on Modrinth
   :target: https://modrinth.com/plugin/lpc-chat

LPC is a flexible chat formatting plugin with full MiniMessage support, designed specifically for use with LuckPerms. It provides modern formatting capabilities, group/track specific formats, and a suite of advanced features like chat bubbles and channels.

Requirements
------------

* **LuckPerms** (Required) – Permissions plugin
* **PlaceholderAPI** (Optional) – Additional placeholders
* **Minecraft 1.19.4+** (Optional) – Required for Chat Bubbles (Text Displays)

Core Features
-------------

* **MiniMessage Formatting**: Full support for modern tags, gradients, and hex codes.
* **Group & Track Formats**: Customize chat appearance based on LuckPerms groups or tracks.
* **Placeholder Support**: Integrated with LuckPerms metadata and PlaceholderAPI.
* **[ITEM] Placeholder**: Display your held item in chat with full NBT hover details.
* **Chat Bubbles**: Immersive Text Display entities above player heads (configurable).
* **Chat Channels**: Flexible channel system (Global, Local, Staff) with persistent storage.
* **Social Spy & Private Messages**: Modern formatting for PMs with spy capabilities for staff.
* **Auto-Announcer**: Scheduled broadcast messages with gradient support.
* **Advanced Filters**: Regex-based anti-discord, anti-link, and word filters.

New Advanced Features
---------------------

Chat Bubbles
~~~~~~~~~~~~
Visual text bubbles that appear above a player's head when they chat.
* **Toggleable**: Players can use ``/lpc bubbles`` to toggle their visibility.
* **Customizable**: Control duration, height offset, scale, and background colors (RGBA).
* **Billboard Modes**: Bubbles can follow the viewer's camera or stay fixed.

Chat Channels
~~~~~~~~~~~~~
Divide your chat into distinct streams with varying logic.
* **Storage Options**: Persistent channel preferences saved via **SQLite**, **MySQL**, or **Memory**.
* **Channel Types**:
    * ``global``: Standard server-wide chat.
    * ``permission``: Restricted channels (e.g., Staff Chat).
    * ``range``: Distance-based local chat.
* **Quick Access**: Support for shortcuts (``/sc <msg>``) and symbols (``#message``).

[ITEM] Placeholder
~~~~~~~~~~~~~~~~~~
Type ``[item]`` in chat to showcase your hand item.
* **Full NBT**: Hovering over the item in chat displays the complete vanilla tooltip (enchantments, lore, potion effects).

Permissions
-----------

.. list-table::
   :widths: 25 25 50
   :header-rows: 1

   * - Command / Feature
     - Permission
     - Description
   * - ``/lpc reload``
     - ``lpc.reload``
     - Reloads the configuration
   * - ``/lpc bubbles``
     - ``lpc.bubbles.toggle``
     - Toggle chat bubbles visibility for yourself
   * - ``/channel``
     - ``lpc.channel.use``
     - Switch or message in channels
   * - MiniMessage colors
     - ``lpc.chatcolor``
     - Allows using colors in chat
   * - ``[item]`` Placeholder
     - ``lpc.itemplaceholder``
     - Use the ``[item]`` placeholder
   * - Staff Chat
     - ``lpc.staffchat``
     - Access to staff chat and symbols
   * - Social Spy
     - ``lpc.socialspy``
     - Monitor private messages

Available Placeholders
----------------------

* ``{message}``: The chat message
* ``{name}``: Player's name
* ``{displayname}``: Display name / nickname
* ``{world}``: Player's current world
* ``{prefix}`` / ``{suffix}``: Highest priority metadata
* ``{prefixes}`` / ``{suffixes}``: Sorted list of all meta
* ``{username-color}`` / ``{message-color}``: LuckPerms meta colors

Installation
------------

1. Stop your server.
2. Place ``LPC.jar`` into your ``/plugins`` folder.
3. Start the server to generate configuration files.
4. Edit ``config.yml`` to customize your formats and features.
5. Use ``/lpc reload`` to apply changes.

Notes
-----

* **Not affiliated with LuckPerms** – Please do not contact the LuckPerms author for support!
* LPC uses **MiniMessage only**. Legacy color codes (``&a``) are not supported in config strings.
