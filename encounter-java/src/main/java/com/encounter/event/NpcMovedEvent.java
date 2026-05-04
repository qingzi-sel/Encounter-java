package com.encounter.event;

import com.encounter.domain.model.RoomId;

public class NpcMovedEvent extends GameEvent {
    private final RoomId fromRoom;
    private final RoomId toRoom;

    public NpcMovedEvent(RoomId fromRoom, RoomId toRoom) {
        this.fromRoom = fromRoom;
        this.toRoom = toRoom;
    }

    public RoomId getFromRoom() { return fromRoom; }
    public RoomId getToRoom() { return toRoom; }
}
