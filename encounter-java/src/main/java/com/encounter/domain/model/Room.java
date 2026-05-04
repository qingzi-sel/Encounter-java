package com.encounter.domain.model;

import java.util.List;
import java.util.Map;

/**
 * Immutable room definition loaded from RoomLayout.
 */
public class Room {

    private final RoomId id;
    private final String name;
    private final List<AttributeType> attrs;
    private final List<RoomId> adj;

    public Room(RoomId id, String name, List<AttributeType> attrs, List<RoomId> adj) {
        this.id = id;
        this.name = name;
        this.attrs = List.copyOf(attrs);
        this.adj = List.copyOf(adj);
    }

    public RoomId getId() { return id; }
    public String getName() { return name; }
    public List<AttributeType> getAttrs() { return attrs; }
    public List<RoomId> getAdj() { return adj; }

    public boolean isSafe() {
        return attrs.isEmpty();
    }

    @Override
    public String toString() {
        return "Room{id=" + id + ", name='" + name + "'}";
    }
}
