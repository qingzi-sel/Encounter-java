package com.encounter.domain.model;

import java.util.*;

/**
 * Static registry of all rooms, their topology (adjacency graph), and screen layout coordinates.
 * This is the single source of truth for the castle map.
 */
public final class RoomLayout {

    private RoomLayout() {}

    public static final Map<RoomId, Room> ROOMS = new LinkedHashMap<>();
    public static final Map<RoomId, Point> LAYOUT = new LinkedHashMap<>();
    public static final List<Edge> EDGES = new ArrayList<>();

    static {
        // Define rooms — id, name, attrs, adjacent room ids
        register(RoomId.LIVING_ROOM, "起居室", List.of(AttributeType.STAMINA, AttributeType.FOCUS),
                List.of(RoomId.GREAT_HALL, RoomId.YARD, RoomId.DRESSING_ROOM, RoomId.GUEST_QUARTERS, RoomId.KITCHEN));
        register(RoomId.GREAT_HALL, "大礼堂", List.of(),
                List.of(RoomId.LIVING_ROOM, RoomId.THRONE_ROOM, RoomId.LORDS_CHAMBER, RoomId.GRAND_LIBRARY));
        register(RoomId.THRONE_ROOM, "王座大厅", List.of(),
                List.of(RoomId.GREAT_HALL));
        register(RoomId.LORDS_CHAMBER, "领主卧房", List.of(),
                List.of(RoomId.GREAT_HALL, RoomId.BELL_TOWER));
        register(RoomId.GRAND_LIBRARY, "大图书馆", List.of(AttributeType.INTELLIGENCE),
                List.of(RoomId.GREAT_HALL, RoomId.ALCHEMY_LAB));
        register(RoomId.OBSERVATORY, "占星塔", List.of(),
                List.of(RoomId.ALCHEMY_LAB));
        register(RoomId.BELL_TOWER, "钟楼", List.of(),
                List.of(RoomId.LORDS_CHAMBER, RoomId.SHADOW_CORRIDOR));
        register(RoomId.ALCHEMY_LAB, "炼金室", List.of(),
                List.of(RoomId.GRAND_LIBRARY, RoomId.OBSERVATORY, RoomId.GUEST_QUARTERS));
        register(RoomId.SHADOW_CORRIDOR, "密道", List.of(),
                List.of(RoomId.DRESSING_ROOM, RoomId.BELL_TOWER, RoomId.GREENHOUSE));
        register(RoomId.DRESSING_ROOM, "更衣室", List.of(),
                List.of(RoomId.LIVING_ROOM, RoomId.SHADOW_CORRIDOR, RoomId.CHAPEL));
        register(RoomId.GUEST_QUARTERS, "贵宾室", List.of(),
                List.of(RoomId.LIVING_ROOM, RoomId.ALCHEMY_LAB, RoomId.WINE_CELLAR));
        register(RoomId.GREENHOUSE, "温室废园", List.of(),
                List.of(RoomId.SHADOW_CORRIDOR, RoomId.ARMORY));
        register(RoomId.CHAPEL, "礼拜堂", List.of(),
                List.of(RoomId.DRESSING_ROOM, RoomId.YARD, RoomId.ARMORY));
        register(RoomId.YARD, "城门庭院", List.of(AttributeType.STAMINA, AttributeType.STRENGTH),
                List.of(RoomId.LIVING_ROOM, RoomId.MAIN_GATE, RoomId.CHAPEL, RoomId.KITCHEN));
        register(RoomId.KITCHEN, "厨房", List.of(AttributeType.FOCUS),
                List.of(RoomId.LIVING_ROOM, RoomId.YARD, RoomId.WINE_CELLAR));
        register(RoomId.WINE_CELLAR, "酒窖", List.of(),
                List.of(RoomId.GUEST_QUARTERS, RoomId.KITCHEN, RoomId.DUNGEON));
        register(RoomId.ARMORY, "军械库", List.of(),
                List.of(RoomId.CHAPEL, RoomId.MAIN_GATE, RoomId.GREENHOUSE));
        register(RoomId.MAIN_GATE, "城堡大门", List.of(),
                List.of(RoomId.YARD, RoomId.ARMORY, RoomId.WATCHTOWER));
        register(RoomId.WATCHTOWER, "瞭望台", List.of(),
                List.of(RoomId.MAIN_GATE, RoomId.DUNGEON));
        register(RoomId.DUNGEON, "地牢", List.of(AttributeType.STAMINA, AttributeType.PATIENCE),
                List.of(RoomId.WATCHTOWER, RoomId.WINE_CELLAR));

        // Screen layout coordinates (for frontend map rendering)
        LAYOUT.put(RoomId.THRONE_ROOM, new Point(0, -2));
        LAYOUT.put(RoomId.LORDS_CHAMBER, new Point(-1, -2));
        LAYOUT.put(RoomId.GRAND_LIBRARY, new Point(1, -2));
        LAYOUT.put(RoomId.OBSERVATORY, new Point(2, -2));
        LAYOUT.put(RoomId.BELL_TOWER, new Point(-2, -1));
        LAYOUT.put(RoomId.GREAT_HALL, new Point(0, -1));
        LAYOUT.put(RoomId.ALCHEMY_LAB, new Point(2, -1));
        LAYOUT.put(RoomId.SHADOW_CORRIDOR, new Point(-2, 0));
        LAYOUT.put(RoomId.DRESSING_ROOM, new Point(-1, 0));
        LAYOUT.put(RoomId.LIVING_ROOM, new Point(0, 0));
        LAYOUT.put(RoomId.GUEST_QUARTERS, new Point(1, 0));
        LAYOUT.put(RoomId.GREENHOUSE, new Point(-2, 1));
        LAYOUT.put(RoomId.CHAPEL, new Point(-1, 1));
        LAYOUT.put(RoomId.YARD, new Point(0, 1));
        LAYOUT.put(RoomId.KITCHEN, new Point(1, 1));
        LAYOUT.put(RoomId.WINE_CELLAR, new Point(2, 1));
        LAYOUT.put(RoomId.ARMORY, new Point(-1, 2));
        LAYOUT.put(RoomId.MAIN_GATE, new Point(0, 2));
        LAYOUT.put(RoomId.WATCHTOWER, new Point(1, 2));
        LAYOUT.put(RoomId.DUNGEON, new Point(2, 2));
    }

    private static void register(RoomId id, String name, List<AttributeType> attrs, List<RoomId> adj) {
        Room room = new Room(id, name, attrs, adj);
        ROOMS.put(id, room);
        for (RoomId neighbor : adj) {
            EDGES.add(new Edge(id, neighbor));
        }
    }

    public static Room get(RoomId id) {
        return ROOMS.get(id);
    }

    /**
     * A 2D coordinate for screen layout.
     */
    public record Point(int x, int y) {}

    /**
     * An undirected edge between two rooms.
     */
    public record Edge(RoomId from, RoomId to) {}
}
