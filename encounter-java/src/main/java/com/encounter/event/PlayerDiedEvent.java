package com.encounter.event;

public class PlayerDiedEvent extends GameEvent {
    public enum Cause { HP_ZERO, BEAST_KILL, ABSOLUTE_DEATH }

    private final Cause cause;

    public PlayerDiedEvent(Cause cause) {
        this.cause = cause;
    }

    public Cause getCause() { return cause; }
}
