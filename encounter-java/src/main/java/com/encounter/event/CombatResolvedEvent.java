package com.encounter.event;

import com.encounter.domain.model.AttributeType;
import com.encounter.domain.model.RoomId;

import java.util.Map;

public class CombatResolvedEvent extends GameEvent {
    private final RoomId roomId;
    private final String winner; // "player", "npc", "draw"
    private final double stealTotal;
    private final Map<AttributeType, Double> stolenValues;
    private final boolean execution;

    public CombatResolvedEvent(RoomId roomId, String winner, double stealTotal,
                               Map<AttributeType, Double> stolenValues, boolean execution) {
        this.roomId = roomId;
        this.winner = winner;
        this.stealTotal = stealTotal;
        this.stolenValues = stolenValues;
        this.execution = execution;
    }

    public RoomId getRoomId() { return roomId; }
    public String getWinner() { return winner; }
    public double getStealTotal() { return stealTotal; }
    public Map<AttributeType, Double> getStolenValues() { return stolenValues; }
    public boolean isExecution() { return execution; }
}
