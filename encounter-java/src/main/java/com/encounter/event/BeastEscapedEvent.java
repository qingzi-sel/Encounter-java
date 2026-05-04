package com.encounter.event;

import com.encounter.domain.model.RoomId;

public class BeastEscapedEvent extends GameEvent {
    private final RoomId lastContainedLoc;

    public BeastEscapedEvent(RoomId lastContainedLoc) {
        this.lastContainedLoc = lastContainedLoc;
    }

    public RoomId getLastContainedLoc() { return lastContainedLoc; }
}
