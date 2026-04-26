Configuration
=============

Ah, the ``config.yml``. The place where dreams go to be precisely formatted using YAML. If you mess up a single space, the plugin will probably cry (and so will we).

LPC uses **MiniMessage**. That means no more ``&a`` codes like it's 2010. Use tags like ``<green>``, ``<bold>``, or even gradients if you're feeling fancy.

**Main Chat Format**
-------------------
The ``chat-format`` setting is the heart of LPC. Use placeholders like ``{prefix}``, ``{name}``, and ``{message}``. If you don't include ``{message}``, players will just be staring at each other in silence. Awkward.

**How-To: Group Formats**
~~~~~~~~~~~~~~~~~~~~~~~~~
Want your admins to look more important than they actually are? Use ``group-formats``.

1. Find the ``group-formats:`` section.
2. Add your LuckPerms group name (lowercase, please).
3. Define the format.

Example:
.. code-block:: yaml

   group-formats:
     admin: "<red>[Admin] {name}: <white>{message}"
     moderator: "<blue>[Mod] {name}: <white>{message}"

Now your admins can flex their red prefixes while doing... whatever it is admins do.

**How-To: Setting up a Channel**
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
Channels are for when you want to segment the noise.

1. Go to ``channels.list``.
2. Invent a name for your channel (e.g., ``vip``).
3. Set the ``type`` (``global``, ``permission``, or ``range``).
4. Add a ``permission`` if you want to keep the riff-raff out.

Example:
.. code-block:: yaml

   list:
     vip:
       type: "permission"
       permission: "my.vip.perm"
       format: "<gold>[VIP] {name}: <white>{message}"
       shortcut: "v"
       symbol: "$"

Now players with ``my.vip.perm`` can type ``/v hello`` or start their message with ``$`` to brag to other VIPs.

**How-To: The [item] Placeholder**
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
Because telling people about your sword isn't as good as showing them.

1. Ensure ``use-item-placeholder: true`` is set.
2. Give players the ``lpc.itemplaceholder`` permission.
3. Players type ``[item]`` in chat.

LPC will grab whatever is in their main hand and turn it into a fancy hoverable component. If they're holding dirt, it'll show dirt. We can't fix their inventory choices.

**Channels**
------------
Want to keep the peasants (players) away from the royalty (staff)? Enable channels.
- **Global:** For everyone.
- **Staff:** For talking about the players behind their backs.
- **Local:** For when you only want people within 100 blocks to hear your bad jokes.

**Storage**
-----------
We can remember things!
- ``MEMORY``: Good for those with commitment issues (resets on restart).
- ``SQLITE``: A nice little file. Reliable, like a pet rock.
- ``MYSQL``: For the "I have a network" flex.

**The Filter**
--------------
Stop people from shouting in ALL CAPS or posting their Discord invites. You can even block specific words. We've included some placeholders, but feel free to add your own favorite "polite" words.

**Pro Tip:** Use the `MiniMessage Viewer <https://webui.advntr.dev/>`_ to preview your formats before you break your server.
