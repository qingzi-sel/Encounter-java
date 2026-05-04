package com.encounter.controller;

import com.encounter.controller.dto.ActionRequest;
import com.encounter.domain.model.*;
import com.encounter.engine.GameEngine;
import com.encounter.util.AttributeMath;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * REST API for player actions. All game state is pushed via WebSocket.
 */
@RestController
@RequestMapping("/api/game")
public class GameController {

    private final GameEngine engine;

    public GameController(GameEngine engine) {
        this.engine = engine;
    }

    @PostMapping("/start")
    public ResponseEntity<?> startGame(@RequestBody ActionRequest req) {
        if (req.getAttributes() == null || req.getAttributes().size() != 5) {
            return ResponseEntity.badRequest().body(Map.of("error", "需要 5 个属性的初始值"));
        }
        Attributes attrs = new Attributes();
        for (Map.Entry<String, Double> entry : req.getAttributes().entrySet()) {
            try {
                AttributeType type = AttributeType.valueOf(entry.getKey().toUpperCase());
                attrs.set(type, entry.getValue());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(Map.of("error", "未知属性: " + entry.getKey()));
            }
        }
        engine.startGame(attrs);
        return ResponseEntity.ok(Map.of("status", "started"));
    }

    @PostMapping("/move")
    public ResponseEntity<?> move(@RequestBody ActionRequest req) {
        try {
            RoomId target = RoomId.valueOf(req.getTargetRoomId().toUpperCase());
            engine.playerMove(target);
            return ResponseEntity.ok(Map.of("status", "ok"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "未知房间: " + req.getTargetRoomId()));
        }
    }

    @PostMapping("/reallocate")
    public ResponseEntity<?> reallocate(@RequestBody ActionRequest req) {
        if (req.getAttributes() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "需要属性值"));
        }
        Attributes draft = new Attributes();
        for (Map.Entry<String, Double> entry : req.getAttributes().entrySet()) {
            try {
                AttributeType type = AttributeType.valueOf(entry.getKey().toUpperCase());
                draft.set(type, entry.getValue());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(Map.of("error", "未知属性: " + entry.getKey()));
            }
        }
        engine.playerAllocate(draft);
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @PostMapping("/reallocate/cancel")
    public ResponseEntity<?> cancelReallocate() {
        engine.cancelAllocation();
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @PostMapping("/read")
    public ResponseEntity<?> startReading(@RequestBody ActionRequest req) {
        if (req.getBookType() != 20 && req.getBookType() != 50) {
            return ResponseEntity.badRequest().body(Map.of("error", "bookType 必须是 20 或 50"));
        }
        engine.startReading(req.getBookType());
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @PostMapping("/reading/purify")
    public ResponseEntity<?> purifyWord(@RequestBody ActionRequest req) {
        engine.purifyWord(req.getWordId());
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @PostMapping("/divination")
    public ResponseEntity<?> divination() {
        engine.startDivination();
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @PostMapping("/item/use")
    public ResponseEntity<?> useItem(@RequestBody ActionRequest req) {
        try {
            ItemType itemType = ItemType.valueOf(req.getItemType().toUpperCase());
            engine.useItem(itemType);
            return ResponseEntity.ok(Map.of("status", "ok"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "未知道具: " + req.getItemType()));
        }
    }

    @PostMapping("/beast/feed/start")
    public ResponseEntity<?> feedStart() {
        engine.setBeastFeeding(true);
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @PostMapping("/beast/feed/stop")
    public ResponseEntity<?> feedStop() {
        engine.setBeastFeeding(false);
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @GetMapping("/state")
    public ResponseEntity<?> getState() {
        var session = engine.getSession();
        if (session == null) {
            return ResponseEntity.ok(Map.of("status", "no_game"));
        }
        return ResponseEntity.ok(Map.of("status", "ok", "sessionId", session.getId()));
    }

    @GetMapping("/rooms")
    public ResponseEntity<?> getRooms() {
        List<Map<String, Object>> roomList = new ArrayList<>();
        for (Map.Entry<RoomId, Room> entry : RoomLayout.ROOMS.entrySet()) {
            Map<String, Object> roomData = new LinkedHashMap<>();
            Room room = entry.getValue();
            RoomLayout.Point layout = RoomLayout.LAYOUT.get(entry.getKey());
            roomData.put("id", room.getId().name());
            roomData.put("name", room.getName());
            roomData.put("attrs", room.getAttrs().stream().map(AttributeType::name).collect(Collectors.toList()));
            roomData.put("adj", room.getAdj().stream().map(RoomId::name).collect(Collectors.toList()));
            roomData.put("x", layout != null ? layout.x() : 0);
            roomData.put("y", layout != null ? layout.y() : 0);
            roomList.add(roomData);
        }
        List<List<String>> edges = RoomLayout.EDGES.stream()
                .map(e -> List.of(e.from().name(), e.to().name()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(Map.of("rooms", roomList, "edges", edges));
    }

    @PostMapping("/adjust-distributed")
    public ResponseEntity<?> adjustDistributed(@RequestBody ActionRequest req) {
        if (req.getAttributes() == null || req.getTargetKey() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "需要 attributes 和 targetKey"));
        }
        AttributeType targetType;
        try {
            targetType = AttributeType.valueOf(req.getTargetKey().toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "未知属性: " + req.getTargetKey()));
        }

        Map<AttributeType, Double> current = new EnumMap<>(AttributeType.class);
        for (Map.Entry<String, Double> entry : req.getAttributes().entrySet()) {
            try {
                AttributeType type = AttributeType.valueOf(entry.getKey().toUpperCase());
                current.put(type, entry.getValue());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(Map.of("error", "未知属性: " + entry.getKey()));
            }
        }

        Map<AttributeType, Double> result = AttributeMath.adjustDistributed(current, targetType, req.getTargetDelta());
        Map<String, Double> response = new LinkedHashMap<>();
        for (AttributeType type : AttributeType.values()) {
            response.put(type.name(), result.getOrDefault(type, 0.0));
        }
        return ResponseEntity.ok(Map.of("attributes", response));
    }
}
