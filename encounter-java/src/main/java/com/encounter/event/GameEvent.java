package com.encounter.event;

import java.time.Instant;

/**
 * Base class for all game events.
 */
public abstract class GameEvent {

    private final Instant timestamp = Instant.now();

    public Instant getTimestamp() {
        return timestamp;
    }
}
