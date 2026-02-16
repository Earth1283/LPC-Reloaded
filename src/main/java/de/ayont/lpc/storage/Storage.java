package de.ayont.lpc.storage;

import de.ayont.lpc.moderation.Mute;
import de.ayont.lpc.moderation.Warning;
import java.util.List;
import java.util.UUID;

public interface Storage {
    void init();
    void shutdown();
    void save(UUID uuid, String channel);
    String load(UUID uuid);

    // Mutes
    void saveMute(Mute mute);
    void removeMute(UUID uuid);
    Mute loadMute(UUID uuid);

    // Warnings
    void addWarning(Warning warning);
    void removeWarning(int id);
    List<Warning> loadWarnings(UUID uuid);

    // Profiles
    void saveBio(UUID uuid, String bio);
    String loadBio(UUID uuid);

    // Slowmode
    void setSlowmode(UUID uuid, double seconds);
    double getSlowmode(UUID uuid);
}
