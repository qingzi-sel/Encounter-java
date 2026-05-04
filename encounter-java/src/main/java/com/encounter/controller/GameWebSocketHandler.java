package com.encounter.controller;

import com.encounter.controller.dto.GameStateDTO;
import com.encounter.domain.model.*;
import com.encounter.engine.GameEngine;
import com.encounter.engine.GameState;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

/**
 * Raw WebSocket handler — pushes GameStateDTO JSON to all connected clients each tick.
 */
@Component
public class GameWebSocketHandler extends TextWebSocketHandler {

    private final GameEngine engine;
    private final ObjectMapper mapper = new ObjectMapper();
    private final Set<WebSocketSession> sessions = new CopyOnWriteArraySet<>();

    public GameWebSocketHandler(GameEngine engine) {
        this.engine = engine;
    }

    @PostConstruct
    public void init() {
        engine.setStatePushCallback(this::broadcast);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
        // Push initial state if game is running
        var s = engine.getSession();
        if (s != null) {
            try {
                String json = mapper.writeValueAsString(toDTO(s.getState()));
                session.sendMessage(new TextMessage(json));
            } catch (IOException e) {
                // ignore
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        // Client can send messages but we don't need to handle them — REST handles actions
    }

    private void broadcast(GameState s) {
        String json;
        try {
            json = mapper.writeValueAsString(toDTO(s));
        } catch (Exception e) {
            return;
        }
        TextMessage msg = new TextMessage(json);
        for (WebSocketSession session : sessions) {
            try {
                if (session.isOpen()) {
                    session.sendMessage(msg);
                }
            } catch (IOException e) {
                sessions.remove(session);
            }
        }
    }

    private GameStateDTO toDTO(GameState s) {
        GameStateDTO dto = new GameStateDTO();
        dto.setStatus(s.getStatus());
        dto.setPlayerLoc(s.getPlayerLoc().name());
        dto.setPlayerAttrs(attrsToMap(s.getPlayerAttrs()));
        dto.setNpcLoc(s.getNpcLoc().name());
        dto.setNpcAttrs(attrsToMap(s.getNpcAttrs()));
        dto.setInventory(s.getInventory().stream().map(Enum::name).collect(Collectors.toList()));
        dto.setTraps(s.getTraps().stream().map(Enum::name).collect(Collectors.toList()));
        dto.setLogs(new ArrayList<>(s.getLogs()));
        dto.setCompletedBooks(new ArrayList<>(s.getCompletedBooks()));
        dto.setInstantReallocActive(s.isInstantReallocActive());

        if (s.getPendingPlayerAttrs() != null) {
            GameStateDTO.PendingRealloc pr = new GameStateDTO.PendingRealloc();
            pr.setAttrs(attrsToMap(s.getPendingPlayerAttrs()));
            pr.setProgress(s.getReallocTimer() / 4.0);
            pr.setRemainingSeconds(Math.max(0, 4.0 - s.getReallocTimer()));
            dto.setPendingRealloc(pr);
        }

        GameStateDTO.BeastDTO beastDTO = new GameStateDTO.BeastDTO();
        beastDTO.setSatiety(s.getBeast().getSatiety());
        beastDTO.setState(s.getBeast().getState().name());
        beastDTO.setLoc(s.getBeast().getLoc().name());
        dto.setBeast(beastDTO);

        if (s.getCombatData() != null) {
            CombatData cd = s.getCombatData();
            GameStateDTO.CombatUpdateDTO c = new GameStateDTO.CombatUpdateDTO();
            c.setPhase(cd.getPhase().name());
            c.setRoomId(cd.getRoomId().name());
            c.setRoomName(RoomLayout.get(cd.getRoomId()).getName());
            c.setAttrsCompared(cd.getAttrsCompared().stream().map(AttributeType::getDisplayName).collect(Collectors.toList()));
            c.setPlayerSum(cd.getPlayerSum());
            c.setNpcSum(cd.getNpcSum());
            c.setStealTotal(cd.getStealTotal());
            c.setExecution(cd.isExecution());
            c.setTimer(cd.getTimer());

            Map<String, Double> pp = new LinkedHashMap<>();
            cd.getPlayerPreAttrs().forEach((k, v) -> pp.put(k.name(), v));
            c.setPlayerPreAttrs(pp);

            Map<String, Double> np = new LinkedHashMap<>();
            cd.getNpcPreAttrs().forEach((k, v) -> np.put(k.name(), v));
            c.setNpcPreAttrs(np);

            Map<String, Double> sv = new LinkedHashMap<>();
            cd.getStolenValues().forEach((k, v) -> sv.put(k.name(), v));
            c.setStolenValues(sv);

            if (cd.getWinner() != null) c.setWinner(cd.getWinner().name().toLowerCase());
            dto.setCombat(c);
        }

        if (s.getReadingData() != null) {
            ReadingData rd = s.getReadingData();
            GameStateDTO.ReadingDTO r = new GameStateDTO.ReadingDTO();
            r.setBookType(rd.getBookType());
            r.setTimer(rd.getTimer());
            r.setCorruption(rd.getCorruption());
            r.setWords(rd.getWords().stream().map(w -> {
                GameStateDTO.WordDTO wd = new GameStateDTO.WordDTO();
                wd.setId(w.getId());
                wd.setText(w.getText());
                wd.setCorrupt(w.isCorrupt());
                wd.setRot(w.getRot());
                return wd;
            }).collect(Collectors.toList()));
            dto.setReading(r);
        }

        if (s.getDivinationResult() != null) {
            GameStateDTO.DivinationDTO div = new GameStateDTO.DivinationDTO();
            div.setCard(s.getDivinationResult().getCard().name());
            div.setTimer(s.getDivinationResult().getTimer());
            div.setDisplayName(s.getDivinationResult().getCard().getDisplayName());
            dto.setDivination(div);
        }

        GameStateDTO.TimersDTO timers = new GameStateDTO.TimersDTO();
        timers.setInvisibility(s.getInvisibilityTimer());
        timers.setTrapped(s.getTrappedTimer());
        timers.setDivinationCooldown(s.getDivinationCooldown());
        timers.setShowWarning(s.getShowWarningTimer());
        timers.setReallocProgress(s.getReallocTimer() / 4.0);
        dto.setTimers(timers);

        return dto;
    }

    private Map<String, Double> attrsToMap(Attributes attrs) {
        Map<String, Double> map = new LinkedHashMap<>();
        for (AttributeType type : AttributeType.values()) {
            map.put(type.name(), attrs.get(type));
        }
        return map;
    }
}
