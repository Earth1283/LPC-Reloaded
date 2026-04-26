# Java to Kotlin Conversion Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Convert all remaining Java files in `src/main/java` to idiomatic Kotlin files in `src/main/kotlin`, update references, verify with build, and cleanup.

**Architecture:** Systematic conversion package by package. Maintain same package structure. Use Kotlin features like null safety, data classes, and properties. Update `LPC` references as instructed.

**Tech Stack:** Kotlin, Java, Gradle.

---

### Task 1: Convert `de.ayont.lpc.moderation` package

**Files:**
- Convert: `src/main/java/de/ayont/lpc/moderation/Mute.java` -> `src/main/kotlin/de/ayont/lpc/moderation/Mute.kt`
- Convert: `src/main/java/de/ayont/lpc/moderation/Warning.java` -> `src/main/kotlin/de/ayont/lpc/moderation/Warning.kt`
- Convert: `src/main/java/de/ayont/lpc/moderation/PlayerProfile.java` -> `src/main/kotlin/de/ayont/lpc/moderation/PlayerProfile.kt`
- Convert: `src/main/java/de/ayont/lpc/moderation/ModerationManager.java` -> `src/main/kotlin/de/ayont/lpc/moderation/ModerationManager.kt`

- [ ] **Step 1: Convert Mute and Warning to Kotlin data classes**
- [ ] **Step 2: Convert PlayerProfile to Kotlin**
- [ ] **Step 3: Convert ModerationManager to Kotlin**
- [ ] **Step 4: Verify compilation**
Run: `./gradlew compileKotlin` in `/workspaces/LPC-Reloaded/.worktrees/refactor-kotlin`

### Task 2: Convert `de.ayont.lpc.storage` package

**Files:**
- Convert: `src/main/java/de/ayont/lpc/storage/Storage.java` -> `src/main/kotlin/de/ayont/lpc/storage/Storage.kt`
- Convert: `src/main/java/de/ayont/lpc/storage/impl/InMemoryStorage.java` -> `src/main/kotlin/de/ayont/lpc/storage/impl/InMemoryStorage.kt`
- Convert: `src/main/java/de/ayont/lpc/storage/impl/MySQLStorage.java` -> `src/main/kotlin/de/ayont/lpc/storage/impl/MySQLStorage.kt`
- Convert: `src/main/java/de/ayont/lpc/storage/impl/SQLiteStorage.java` -> `src/main/kotlin/de/ayont/lpc/storage/impl/SQLiteStorage.kt`

- [ ] **Step 1: Convert Storage interface**
- [ ] **Step 2: Convert InMemoryStorage**
- [ ] **Step 3: Convert MySQLStorage**
- [ ] **Step 4: Convert SQLiteStorage**
- [ ] **Step 5: Verify compilation**
Run: `./gradlew compileKotlin`

### Task 3: Convert `de.ayont.lpc.channels` package

**Files:**
- Convert: `src/main/java/de/ayont/lpc/channels/Channel.java` -> `src/main/kotlin/de/ayont/lpc/channels/Channel.kt`
- Convert: `src/main/java/de/ayont/lpc/channels/BaseChannel.java` -> `src/main/kotlin/de/ayont/lpc/channels/BaseChannel.kt`
- Convert: `src/main/java/de/ayont/lpc/channels/GlobalChannel.java` -> `src/main/kotlin/de/ayont/lpc/channels/GlobalChannel.kt`
- Convert: `src/main/java/de/ayont/lpc/channels/PermissionChannel.java` -> `src/main/kotlin/de/ayont/lpc/channels/PermissionChannel.kt`
- Convert: `src/main/java/de/ayont/lpc/channels/RangeChannel.java` -> `src/main/kotlin/de/ayont/lpc/channels/RangeChannel.kt`
- Convert: `src/main/java/de/ayont/lpc/channels/ChannelManager.java` -> `src/main/kotlin/de/ayont/lpc/channels/ChannelManager.kt`

- [ ] **Step 1: Convert Channel interface**
- [ ] **Step 2: Convert BaseChannel and its subclasses**
- [ ] **Step 3: Convert ChannelManager**
- [ ] **Step 4: Verify compilation**
Run: `./gradlew compileKotlin`

### Task 4: Convert `de.ayont.lpc.renderer` package

**Files:**
- Convert: `src/main/java/de/ayont/lpc/renderer/ChatRendererUtil.java` -> `src/main/kotlin/de/ayont/lpc/renderer/ChatRendererUtil.kt`
- Convert: `src/main/java/de/ayont/lpc/renderer/LPCChatRenderer.java` -> `src/main/kotlin/de/ayont/lpc/renderer/LPCChatRenderer.kt`
- Convert: `src/main/java/de/ayont/lpc/renderer/SpigotChatRenderer.java` -> `src/main/kotlin/de/ayont/lpc/renderer/SpigotChatRenderer.kt`

- [ ] **Step 1: Convert ChatRendererUtil**
- [ ] **Step 2: Convert LPCChatRenderer and SpigotChatRenderer**
- [ ] **Step 3: Verify compilation**
Run: `./gradlew compileKotlin`

### Task 5: Convert root package remaining files

**Files:**
- Convert: `src/main/java/de/ayont/lpc/ChatBubbleManager.java` -> `src/main/kotlin/de/ayont/lpc/ChatBubbleManager.kt`
- Convert: `src/main/java/de/ayont/lpc/AutoAnnouncer.java` -> `src/main/kotlin/de/ayont/lpc/AutoAnnouncer.kt`
- Convert: `src/main/java/de/ayont/lpc/Metrics.java` -> `src/main/kotlin/de/ayont/lpc/Metrics.kt`

- [ ] **Step 1: Convert ChatBubbleManager**
- [ ] **Step 2: Convert AutoAnnouncer**
- [ ] **Step 3: Convert Metrics**
- [ ] **Step 4: Verify compilation**
Run: `./gradlew compileKotlin`

### Task 6: Convert `de.ayont.lpc.listener` package

**Files:**
- Convert: `src/main/java/de/ayont/lpc/listener/JoinQuitListener.java` -> `src/main/kotlin/de/ayont/lpc/listener/JoinQuitListener.kt`
- Convert: `src/main/java/de/ayont/lpc/listener/ShortcutListener.java` -> `src/main/kotlin/de/ayont/lpc/listener/ShortcutListener.kt`
- Convert: `src/main/java/de/ayont/lpc/listener/SpigotChatListener.java` -> `src/main/kotlin/de/ayont/lpc/listener/SpigotChatListener.kt`
- Convert: `src/main/java/de/ayont/lpc/listener/AsyncChatListener.java` -> `src/main/kotlin/de/ayont/lpc/listener/AsyncChatListener.kt`

- [ ] **Step 1: Convert JoinQuitListener**
- [ ] **Step 2: Convert ShortcutListener**
- [ ] **Step 3: Convert SpigotChatListener**
- [ ] **Step 4: Convert AsyncChatListener**
- [ ] **Step 5: Verify compilation**
Run: `./gradlew compileKotlin`

### Task 7: Convert `de.ayont.lpc.commands` package

**Files:**
- Convert all command files in `de.ayont.lpc.commands`

- [ ] **Step 1: Convert SocialSpyCommand and update `lpc.isSocialSpy` calls**
- [ ] **Step 2: Convert IgnoreCommand and update `lpc.isIgnored` calls**
- [ ] **Step 3: Convert PrivateMessageCommand and update `lpc.setLastMessaged` etc. calls**
- [ ] **Step 4: Convert remaining commands (LPCCommand, ChatClearCommand, ModerationCommand, StaffChatCommand, ChannelCommand)**
- [ ] **Step 5: Verify compilation**
Run: `./gradlew compileKotlin`

### Task 8: Final Cleanup and Verification

- [ ] **Step 1: Verify full build**
Run: `./gradlew build`
- [ ] **Step 2: Remove `src/main/java` directory**
Run: `rm -rf src/main/java`
- [ ] **Step 3: Final build check**
Run: `./gradlew build`
