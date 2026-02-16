package de.ayont.lpc.moderation;

import java.util.UUID;

public class Warning {
    private final int id;
    private final UUID target;
    private final UUID sender;
    private final String reason;
    private final long timestamp;

    public Warning(int id, UUID target, UUID sender, String reason, long timestamp) {
        this.id = id;
        this.target = target;
        this.sender = sender;
        this.reason = reason;
        this.timestamp = timestamp;
    }

    public int getId() {
        return id;
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

    public long getTimestamp() {
        return timestamp;
    }
}
