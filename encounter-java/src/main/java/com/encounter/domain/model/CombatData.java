package com.encounter.domain.model;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Data for an ongoing combat encounter. Mutable — mutated by CombatService during the combat flow.
 */
public class CombatData {

    private double timer;
    private CombatPhase phase;
    private RoomId roomId;
    private List<AttributeType> attrsCompared;
    private double playerSum;
    private double npcSum;
    private Winner winner;
    private double stealTotal;
    private Map<AttributeType, Double> stolenValues;
    private Map<AttributeType, Double> playerPreAttrs;
    private Map<AttributeType, Double> npcPreAttrs;
    private boolean execution;

    public CombatData() {
        this.timer = 0.0;
        this.phase = CombatPhase.STARTING;
        this.stolenValues = new EnumMap<>(AttributeType.class);
        this.playerPreAttrs = new EnumMap<>(AttributeType.class);
        this.npcPreAttrs = new EnumMap<>(AttributeType.class);
    }

    public double getTimer() { return timer; }
    public void setTimer(double timer) { this.timer = timer; }

    public CombatPhase getPhase() { return phase; }
    public void setPhase(CombatPhase phase) { this.phase = phase; }

    public RoomId getRoomId() { return roomId; }
    public void setRoomId(RoomId roomId) { this.roomId = roomId; }

    public List<AttributeType> getAttrsCompared() { return attrsCompared; }
    public void setAttrsCompared(List<AttributeType> attrsCompared) { this.attrsCompared = attrsCompared; }

    public double getPlayerSum() { return playerSum; }
    public void setPlayerSum(double playerSum) { this.playerSum = playerSum; }

    public double getNpcSum() { return npcSum; }
    public void setNpcSum(double npcSum) { this.npcSum = npcSum; }

    public Winner getWinner() { return winner; }
    public void setWinner(Winner winner) { this.winner = winner; }

    public double getStealTotal() { return stealTotal; }
    public void setStealTotal(double stealTotal) { this.stealTotal = stealTotal; }

    public Map<AttributeType, Double> getStolenValues() { return stolenValues; }
    public void setStolenValues(Map<AttributeType, Double> stolenValues) { this.stolenValues = stolenValues; }

    public Map<AttributeType, Double> getPlayerPreAttrs() { return playerPreAttrs; }
    public void setPlayerPreAttrs(Map<AttributeType, Double> playerPreAttrs) { this.playerPreAttrs = playerPreAttrs; }

    public Map<AttributeType, Double> getNpcPreAttrs() { return npcPreAttrs; }
    public void setNpcPreAttrs(Map<AttributeType, Double> npcPreAttrs) { this.npcPreAttrs = npcPreAttrs; }

    public boolean isExecution() { return execution; }
    public void setExecution(boolean execution) { this.execution = execution; }

    public enum Winner {
        PLAYER, NPC, DRAW
    }
}
