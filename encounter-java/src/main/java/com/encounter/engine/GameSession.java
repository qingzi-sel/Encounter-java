package com.encounter.engine;

import java.util.UUID;

/**
 * Represents a single player's game session.
 * Each session has its own GameState and is managed by GameEngine.
 */
public class GameSession {

    private final String id;
    private final GameState state;

    public GameSession() {
        this.id = UUID.randomUUID().toString();
        this.state = new GameState();
    }

    public String getId() {
        return id;
    }

    public GameState getState() {
        return state;
    }

    /**
     * Reset the session for a new game.
     */
    public void reset() {
        GameState fresh = new GameState();
        // Copy fresh state fields into existing state (preserves references held by services)
        copyState(fresh, state);
    }

    private static void copyState(GameState src, GameState dst) {
        dst.setStatus(src.getStatus());
        dst.setPlayerLoc(src.getPlayerLoc());
        // Copy player attrs
        for (var t : com.encounter.domain.model.AttributeType.values()) {
            dst.getPlayerAttrs().set(t, src.getPlayerAttrs().get(t));
        }
        dst.setPendingPlayerAttrs(null);
        dst.setReallocTimer(0);
        dst.setInstantReallocActive(false);
        dst.setNpcLoc(src.getNpcLoc());
        for (var t : com.encounter.domain.model.AttributeType.values()) {
            dst.getNpcAttrs().set(t, src.getNpcAttrs().get(t));
        }
        dst.setNpcMoveTimer(0);
        dst.setNpcNextMoveWait(1.5 + Math.random());
        dst.setNpcRoomTimer(0);
        dst.setNpcAdaptedInRoom(false);
        dst.getBeast().setSatiety(100);
        dst.getBeast().setState(com.encounter.domain.model.BeastState.BeastStatus.CONTAINED);
        dst.getBeast().setLoc(com.encounter.domain.model.RoomId.DUNGEON);
        dst.getBeast().setMoveTimer(0);
        dst.setCombatData(null);
        dst.setReadingData(null);
        dst.setDivinationResult(null);
        dst.setDivinationCooldown(0);
        dst.getInventory().clear();
        dst.setInvisibilityTimer(0);
        dst.setTrappedTimer(0);
        dst.setShowWarningTimer(0);
        dst.getTraps().clear();
        dst.getCompletedBooks().clear();
        dst.getLogs().clear();
        dst.getLogs().addAll(src.getLogs());
    }
}
