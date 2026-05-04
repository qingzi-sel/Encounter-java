package com.encounter.domain.model;

/**
 * Mutable state of the contained beast entity.
 */
public class BeastState {

    private double satiety;
    private BeastStatus state;
    private RoomId loc;
    private double moveTimer;

    public BeastState() {
        this.satiety = 100.0;
        this.state = BeastStatus.CONTAINED;
        this.loc = RoomId.DUNGEON;
        this.moveTimer = 0.0;
    }

    public double getSatiety() { return satiety; }
    public void setSatiety(double satiety) { this.satiety = satiety; }

    public BeastStatus getState() { return state; }
    public void setState(BeastStatus state) { this.state = state; }

    public RoomId getLoc() { return loc; }
    public void setLoc(RoomId loc) { this.loc = loc; }

    public double getMoveTimer() { return moveTimer; }
    public void setMoveTimer(double moveTimer) { this.moveTimer = moveTimer; }

    public enum BeastStatus {
        CONTAINED,
        ESCAPED
    }
}
