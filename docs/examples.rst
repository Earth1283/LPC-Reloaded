Examples
========

Because sometimes seeing is believing (and copy-pasting is easier than thinking).

**MiniMessage Masterpieces**
----------------------------
Stop using boring white text. You're better than that.

- **The "I'm Rich" Gradient:**
  ``<gradient:#FFD700:#FFA500><b>[MVP+]</b></gradient> {name} » {message}``
- **The "Server News" Rainbow:**
  ``<rainbow>[ALERT]</rainbow> <red>The server is restarting!</red>``
- **The "Staff" Hover:**
  ``<hover:show_text:'<red>Click to teleport!'><click:run_command:'/tp {name}'><red>[STAFF]</red> {name}</click></hover> » {message}``

**Channel Configurations**
--------------------------
Here are some ways to structure your server's noise.

**The "Noob" Channel (Range-based)**
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
Great for keeping the starter island chatter away from the veterans.

.. code-block:: yaml

   list:
     local:
       type: "range"
       range: 50
       format: "<gray>[Local] {name}: <white>{message}"
       shortcut: "l"

**The "Donator" Lounge (Permission-based)**
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
Reward the people who keep your electricity bill paid.

.. code-block:: yaml

   list:
     lounge:
       type: "permission"
       permission: "lpc.channel.donator"
       format: "<gradient:#55FFFF:#00AAAA>[Lounge]</gradient> {name}: <white>{message}"
       shortcut: "d"
       symbol: "$"

**The "Admin" Secure Line**
~~~~~~~~~~~~~~~~~~~~~~~~~~~
For when you need to talk about who to ban next.

.. code-block:: yaml

   list:
     staff:
       type: "permission"
       permission: "lpc.channel.staff"
       format: "<red>[STAFF ONLY] {name}: <white>{message}"
       shortcut: "sc"
       symbol: "#"

**Complex Hierarchy Example**
-----------------------------
How to use group formats and channel overrides together without losing your mind.

.. code-block:: yaml

   # Global fallback
   chat-format: "<gray>{name} » {message}"

   # Global group override
   group-formats:
     admin: "<red>[Admin] {name} » {message}"

   channels:
     enabled: true
     list:
       global:
         type: "global"
         format: "{prefix}{name} » {message}"
         # This channel-specific override wins for admins in global chat!
         group-formats:
           admin: "<dark_red><b>[GLOBAL ADMIN]</b></dark_red> {name} » {message}"

**Interactive Profiles (Hover Text)**
-------------------------------------
Make your hover text look like a proper RPG profile.

.. code-block:: yaml

   interactive:
     enabled: true
     hover-text:
       - "<gold><b>{name}</b></gold>"
       - "<gray>Rank: <white>{prefix}"
       - "<gray>Level: <white>%player_level%" # Requires PAPI
       - ""
       - "<gray>Bio: <italic>{bio}"
       - ""
       - "{infractions}"
       - ""
       - "<yellow><i>Click to send a message!</i>"
