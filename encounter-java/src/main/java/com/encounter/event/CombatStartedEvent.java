package com.encounter.event;

import com.encounter.domain.model.RoomId;

public class CombatStartedEvent extends GameEvent {
    private final RoomId roomId;

    public CombatStartedEvent(RoomId roomId) {
        this.roomId = roomId;
    }

    public RoomId getRoomId() { return roomId; }
}
