package de.ayont.lpc.moderation;

import java.util.UUID;

public class PlayerProfile {
    private final UUID uuid;
    private String bio;

    public PlayerProfile(UUID uuid, String bio) {
        this.uuid = uuid;
        this.bio = bio;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }
}
