package de.ayont.lpc.storage.impl;

import de.ayont.lpc.storage.Storage;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class InMemoryStorage implements Storage {

    private final Map<UUID, String> playerChannels = new HashMap<>();

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
}
