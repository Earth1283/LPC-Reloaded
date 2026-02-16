package de.ayont.lpc.storage.impl;

import de.ayont.lpc.moderation.Mute;
import de.ayont.lpc.moderation.Warning;
import de.ayont.lpc.storage.Storage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class InMemoryStorage implements Storage {

    private final Map<UUID, String> playerChannels = new HashMap<>();
    private final Map<UUID, Mute> mutes = new HashMap<>();
    private final List<Warning> warnings = new ArrayList<>();
    private final Map<UUID, String> bios = new HashMap<>();
    private final Map<UUID, Double> slowmodes = new HashMap<>();
    private int nextWarningId = 1;

    @Override
    public void init() {}

    @Override
    public void shutdown() {}

    @Override
    public void save(UUID uuid, String channel) {
        playerChannels.put(uuid, channel);
    }

    @Override
    public String load(UUID uuid) {
        return playerChannels.get(uuid);
    }

    @Override
    public void saveMute(Mute mute) {
        mutes.put(mute.getTarget(), mute);
    }

    @Override
    public void removeMute(UUID uuid) {
        mutes.remove(uuid);
    }

    @Override
    public Mute loadMute(UUID uuid) {
        return mutes.get(uuid);
    }

    @Override
    public void addWarning(Warning warning) {
        warnings.add(new Warning(nextWarningId++, warning.getTarget(), warning.getSender(), warning.getReason(), warning.getTimestamp()));
    }

    @Override
    public void removeWarning(int id) {
        warnings.removeIf(w -> w.getId() == id);
    }

    @Override
    public List<Warning> loadWarnings(UUID uuid) {
        return warnings.stream()
                .filter(w -> w.getTarget().equals(uuid))
                .sorted((w1, w2) -> Long.compare(w2.getTimestamp(), w1.getTimestamp()))
                .collect(Collectors.toList());
    }

    @Override
    public void saveBio(UUID uuid, String bio) {
        bios.put(uuid, bio);
    }

    @Override
    public String loadBio(UUID uuid) {
        return bios.get(uuid);
    }

    @Override
    public void setSlowmode(UUID uuid, double seconds) {
        slowmodes.put(uuid, seconds);
    }

    @Override
    public double getSlowmode(UUID uuid) {
        return slowmodes.getOrDefault(uuid, 0.0);
    }
}
