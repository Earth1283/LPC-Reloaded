==============================================
LPC – LuckPerms Chat Formatter (The Good One)
==============================================

.. image:: https://cdn.varilx.de/raw/fwtRZS.png
   :alt: LPC Banner
   :align: center

.. image:: https://raw.githubusercontent.com/vLuckyyy/badges/main/avaiable-on-modrinth.svg
   :alt: Available on Modrinth
   :target: https://modrinth.com/plugin/lpc-chat

LPC is a flexible chat formatting plugin that actually works. We use MiniMessage because it's the 21st century, and we've recently refactored the entire thing to **Kotlin** for that sweet, sweet null safety. Stop using plugins that crash because someone's name is null.

LPC Reloaded is a **Pro plugin, but it's free on all platforms**. We deliver every single meaningful Pro feature without charging you a cent—simply **because we can**. It's licensed under the **GPL**, because we actually believe in open source.

Why LPC? (The Anti-Bloat Promise)
---------------------------------

LPC Reloaded was forked from the original free version of LPC. We've since reached **95% feature parity** with the "Pro" version via a strictly **clean-room implementation**. We didn't need to steal their code to beat them; we just wrote better code.

The remaining 5%? Those are the "features" like AI moderation and grammar correction that we've left out because they're fundamentally stupid and we have self-respect.

While the `paid alternative <https://www.spigotmc.org/resources/122297/>`_ is busy charging you a monthly fee to bloat your server with features you never asked for, we're busy keeping things lean and functional. 

Here’s why we’re better than the "Pro" version:

* **No "MiniMessage Native" BS:** We don't take credit for Paper's hard work. Advertising "Native MiniMessage" as a premium feature is like a car dealer bragging about "Native Rubber" on the tires. Paper does the heavy lifting; we just use the API correctly.
* **No "Grammar Systems":** If your players can't spell, a plugin won't fix their education. We assume your users aren't toddlers.
* **No "Reputation Systems":** This is Minecraft, not a social credit simulator. We leave the dystopian nightmares to the "Pro" guys.
* **No "AI Moderation":** We don't send your chat logs to OpenAI just to check for bad words. Your data stays on your server, where it belongs. Sam Altman has enough data already.
* **No GUI Menus:** We believe you're smart enough to edit a text file. We won't make you waste your life clicking around a chest GUI like you're playing a minigame just to change a chat format.

Core Features (That Actually Matter)
------------------------------------

* **Kotlin-Powered**: Null safety™ included. 
* **MiniMessage Formatting**: Gradients, hex codes, hover text—everything that makes your chat look like it wasn't made in 2012.
* **Chat Bubbles**: Immersion! Messages float above heads like a comic book. Requires Minecraft 1.19.4+ (welcome to the future).
* **Chat Channels**: Segment the noise into Global, Local, or Staff channels.
* **[ITEM] Placeholder**: Show off your loot with full NBT hover details.
* **Interactive Profiles**: Hover over a name to see their bio and infractions. It's like LinkedIn, but with more cubes.

Documentation
-------------

We wrote actual documentation in the ``docs/`` folder because we're professional like that. 

- `Installation Guide <docs/installation.rst>`_ (Even your brother could do it)
- `Configuration Breakdown <docs/configuration.rst>`_ (Don't mess up the YAML)
- `Commands & Permissions <docs/commands.rst>`_ (Assert your dominance)
- `Examples <docs/examples.rst>`_ (For the copy-paste enthusiasts)

Requirements
------------

* **LuckPerms** (Required) – Because why would you use anything else?
* **Java 21** – We stay modern. If you're still on Java 8, please reconsider your life choices.
* **Paper/Spigot** – Paper is better, but we support both because we're nice.

Installation
------------

1. Download the jar.
2. Drop it in ``/plugins``.
3. Restart.
4. Go have a snack while the "Pro" users are still trying to figure out their GUI menus.

Notes
-----

* **Not affiliated with LuckPerms** – They're busy doing actual work; we just make it look pretty.
* LPC uses **MiniMessage only**. Legacy color codes (``&a``) are a relic of the past. Let them go.
