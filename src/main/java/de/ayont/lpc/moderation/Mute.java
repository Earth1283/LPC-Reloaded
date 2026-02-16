package de.ayont.lpc.moderation;

import java.util.UUID;

public class Mute {
    private final UUID target;
    private final UUID sender;
    private final String reason;
    private final long expiry;

    public Mute(UUID target, UUID sender, String reason, long expiry) {
        this.target = target;
        this.sender = sender;
        this.reason = reason;
        this.expiry = expiry;
    }

    public UUID getTarget() {
        return target;
    }

    public UUID getSender() {
        return sender;
    }

    public String getReason() {
        return reason;
    }

    public long getExpiry() {
        return expiry;
    }

    public boolean isPermanent() {
        return expiry == -1;
    }

    public boolean hasExpired() {
        return !isPermanent() && System.currentTimeMillis() > expiry;
    }
}
