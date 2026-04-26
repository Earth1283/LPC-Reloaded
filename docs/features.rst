Features
========

LPC is packed with features you didn't know you needed but now can't live without.

**Chat Bubbles**
----------------
Because sometimes chat is too far away. Now messages can float above players' heads like they're in a comic book. Requires Minecraft 1.19.4+ (welcome to the future).

**Channels**
------------
Tired of seeing "Selling 10 stacks of dirt" while you're trying to talk about the latest server drama? Use channels. They actually work, and you can even switch focus like a pro.

**MiniMessage Support**
-----------------------
If you're still using ``&`` codes, please seek help. MiniMessage allows for gradients, hover text, click actions, and more. It's the gold standard of modern chat formatting.

**How it Works: The Hierarchy of Power**
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
When a player speaks, LPC has to decide which format to use. It's not a democracy; it's a hierarchy.

1. **Channel-specific Group Format:** Did you define a format for their group *inside* the channel they're in? That wins.
2. **Channel-specific Track Format:** If not, is their LuckPerms track mentioned *inside* the channel?
3. **Channel Default Format:** Still nothing? Use the default format for that channel.
4. **Global Group Format:** Not in a channel? Check the global ``group-formats``.
5. **Global Track Format:** No group match? Check ``track-formats``.
6. **Global Chat Format:** The ultimate fallback. The ``chat-format`` you set at the top.

If you don't set *any* of these, we use a hardcoded fallback that looks like ``{prefix}{name} » {message}``. But let's be real, you're better than that.

**Interactive Profiles**
------------------------
Hover over a name in chat to see their rank, bio, and how many times they've been naughty (infractions). It's like LinkedIn, but for Minecraft.

**Automated Filter**
--------------------
Our filter is like a digital bouncer. It keeps out the links, the discord invites, and the excessive shouting. It's not perfect, but it's better than nothing.

**How it Works: The Filter Pipeline**
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
Before a message hits the screen, it goes through our gauntlet:

1. **Mute Check:** Are they muted? If yes, silence. (Unless they have bypass perms, because some people are more equal than others).
2. **Staff Chat Check:** Does it start with ``#``? If so, divert to the staff lounge.
3. **Cooldown:** Are they talking too fast? Wait your turn.
4. **Caps Check:** STOP SHOUTING. If they exceed the percentage, the message is blocked.
5. **Invite/Link Check:** No free advertising.
6. **Word Block:** We swap out your "favorite" words for asterisks. 

Only then—if it survives—does it get rendered and shown to the world.

**Kotlin-Powered**
------------------
The whole thing was refactored to Kotlin. Why does that matter to you? Fewer crashes, better performance, and we get to feel superior to developers who still use pure Java.

**What's NOT Included**
-----------------------
We pride ourselves on what we *don't* have. You won't find any bloated social credit systems, grammar police, or "AI" that sells your chat logs to the highest bidder. If you want a plugin that slows your server to a crawl with "premium" features, you know where to find the "Pro" version.
