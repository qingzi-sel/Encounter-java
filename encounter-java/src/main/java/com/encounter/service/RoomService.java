package com.encounter.service;

import com.encounter.domain.model.Room;
import com.encounter.domain.model.RoomId;
import com.encounter.domain.model.RoomLayout;
import org.springframework.stereotype.Service;

/**
 * Room queries and movement validation.
 */
@Service
public class RoomService {

    public Room getRoom(RoomId id) {
        return RoomLayout.get(id);
    }

    public boolean isAdjacent(RoomId from, RoomId to) {
        Room room = RoomLayout.get(from);
        return room != null && room.getAdj().contains(to);
    }

    /**
     * Check if the room has any active combat attributes.
     */
    public boolean hasActiveAttrs(RoomId id) {
        Room room = RoomLayout.get(id);
        return room != null && !room.getAttrs().isEmpty();
    }
}
