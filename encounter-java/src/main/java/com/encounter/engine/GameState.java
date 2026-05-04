package com.encounter.engine;

import com.encounter.domain.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Mutable runtime game state for a single game session.
 * All game data lives here. Services mutate this directly during the game loop.
 */
public class GameState {

    // --- Status ---
    private volatile GameStatus status = GameStatus.SETUP;

    // --- Player ---
    private RoomId playerLoc = RoomId.MAIN_GATE;
    private final Attributes playerAttrs = new Attributes();
    private Attributes pendingPlayerAttrs = null;
    private double reallocTimer = 0.0;
    private boolean instantReallocActive = false;

    // --- NPC ---
    private RoomId npcLoc = RoomId.DUNGEON;
    private final Attributes npcAttrs = new Attributes();
    private double npcMoveTimer = 0.0;
    private double npcNextMoveWait = 1.5 + Math.random();
    private double npcRoomTimer = 0.0;
    private boolean npcAdaptedInRoom = false;

    // --- Beast ---
    private final BeastState beast = new BeastState();
    private boolean isFeedingBeast = false;

    // --- Combat ---
    private CombatData combatData = null;

    // --- Reading ---
    private ReadingData readingData = null;

    // --- Divination ---
    private DivinationResult divinationResult = null;
    private double divinationCooldown = 0.0;

    // --- Items & Buffs ---
    private final List<ItemType> inventory = new ArrayList<>();
    private double invisibilityTimer = 0.0;
    private double trappedTimer = 0.0;
    private double showWarningTimer = 0.0;
    private final List<RoomId> traps = new ArrayList<>();
    private final List<Integer> completedBooks = new ArrayList<>();

    // --- Logs ---
    private final List<String> logs = new CopyOnWriteArrayList<>();

    public GameState() {
        // Initial log
        logs.add("[系统] 欢迎来到《Encounter 遭遇》。请先分配你的初始属性点。");
    }

    // --- Getters/Setters ---

    public GameStatus getStatus() { return status; }
    public void setStatus(GameStatus status) { this.status = status; }

    public RoomId getPlayerLoc() { return playerLoc; }
    public void setPlayerLoc(RoomId playerLoc) { this.playerLoc = playerLoc; }

    public Attributes getPlayerAttrs() { return playerAttrs; }
    public Attributes getPendingPlayerAttrs() { return pendingPlayerAttrs; }
    public void setPendingPlayerAttrs(Attributes pendingPlayerAttrs) { this.pendingPlayerAttrs = pendingPlayerAttrs; }

    public double getReallocTimer() { return reallocTimer; }
    public void setReallocTimer(double reallocTimer) { this.reallocTimer = reallocTimer; }

    public boolean isInstantReallocActive() { return instantReallocActive; }
    public void setInstantReallocActive(boolean instantReallocActive) { this.instantReallocActive = instantReallocActive; }

    public RoomId getNpcLoc() { return npcLoc; }
    public void setNpcLoc(RoomId npcLoc) { this.npcLoc = npcLoc; }

    public Attributes getNpcAttrs() { return npcAttrs; }

    public double getNpcMoveTimer() { return npcMoveTimer; }
    public void setNpcMoveTimer(double npcMoveTimer) { this.npcMoveTimer = npcMoveTimer; }

    public double getNpcNextMoveWait() { return npcNextMoveWait; }
    public void setNpcNextMoveWait(double npcNextMoveWait) { this.npcNextMoveWait = npcNextMoveWait; }

    public double getNpcRoomTimer() { return npcRoomTimer; }
    public void setNpcRoomTimer(double npcRoomTimer) { this.npcRoomTimer = npcRoomTimer; }

    public boolean isNpcAdaptedInRoom() { return npcAdaptedInRoom; }
    public void setNpcAdaptedInRoom(boolean npcAdaptedInRoom) { this.npcAdaptedInRoom = npcAdaptedInRoom; }

    public BeastState getBeast() { return beast; }

    public boolean isFeedingBeast() { return isFeedingBeast; }
    public void setFeedingBeast(boolean feedingBeast) { isFeedingBeast = feedingBeast; }

    public CombatData getCombatData() { return combatData; }
    public void setCombatData(CombatData combatData) { this.combatData = combatData; }

    public ReadingData getReadingData() { return readingData; }
    public void setReadingData(ReadingData readingData) { this.readingData = readingData; }

    public DivinationResult getDivinationResult() { return divinationResult; }
    public void setDivinationResult(DivinationResult divinationResult) { this.divinationResult = divinationResult; }

    public double getDivinationCooldown() { return divinationCooldown; }
    public void setDivinationCooldown(double divinationCooldown) { this.divinationCooldown = divinationCooldown; }

    public List<ItemType> getInventory() { return inventory; }

    public double getInvisibilityTimer() { return invisibilityTimer; }
    public void setInvisibilityTimer(double invisibilityTimer) { this.invisibilityTimer = invisibilityTimer; }

    public double getTrappedTimer() { return trappedTimer; }
    public void setTrappedTimer(double trappedTimer) { this.trappedTimer = trappedTimer; }

    public double getShowWarningTimer() { return showWarningTimer; }
    public void setShowWarningTimer(double showWarningTimer) { this.showWarningTimer = showWarningTimer; }

    public List<RoomId> getTraps() { return traps; }
    public List<Integer> getCompletedBooks() { return completedBooks; }
    public List<String> getLogs() { return logs; }

    /**
     * Get effective player attribute value considering debuffs.
     */
    public double getEffectivePlayerAttr(AttributeType attr) {
        double val = playerAttrs.get(attr);
        if (attr == AttributeType.FOCUS
                && beast.getState() == BeastState.BeastStatus.CONTAINED
                && beast.getSatiety() < 30) {
            val *= 0.5;
        }
        return com.encounter.util.AttributeMath.snapVal(val);
    }
}
