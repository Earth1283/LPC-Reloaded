package de.ayont.lpc.storage;

import java.util.UUID;

public interface Storage {
    void init();
    void shutdown();
    void save(UUID uuid, String channel);
    String load(UUID uuid);
}
