package com.encounter.controller.dto;

import com.encounter.domain.model.*;

import java.util.List;
import java.util.Map;

/**
 * Full game state snapshot pushed to the frontend via WebSocket each tick.
 */
public class GameStateDTO {

    private GameStatus status;
    private String playerLoc;
    private Map<String, Double> playerAttrs;
    private PendingRealloc pendingRealloc;
    private String npcLoc;
    private Map<String, Double> npcAttrs;
    private BeastDTO beast;
    private CombatUpdateDTO combat;
    private ReadingDTO reading;
    private DivinationDTO divination;
    private List<String> inventory;
    private List<String> traps;
    private List<String> logs;
    private TimersDTO timers;
    private List<Integer> completedBooks;
    private boolean instantReallocActive;

    // --- Nested DTOs ---

    public static class PendingRealloc {
        private Map<String, Double> attrs;
        private double progress; // 0.0 to 1.0
        private double remainingSeconds;

        public Map<String, Double> getAttrs() { return attrs; }
        public void setAttrs(Map<String, Double> attrs) { this.attrs = attrs; }
        public double getProgress() { return progress; }
        public void setProgress(double progress) { this.progress = progress; }
        public double getRemainingSeconds() { return remainingSeconds; }
        public void setRemainingSeconds(double remainingSeconds) { this.remainingSeconds = remainingSeconds; }
    }

    public static class BeastDTO {
        private double satiety;
        private String state;
        private String loc;

        public double getSatiety() { return satiety; }
        public void setSatiety(double satiety) { this.satiety = satiety; }
        public String getState() { return state; }
        public void setState(String state) { this.state = state; }
        public String getLoc() { return loc; }
        public void setLoc(String loc) { this.loc = loc; }
    }

    public static class CombatUpdateDTO {
        private String phase;
        private String roomId;
        private String roomName;
        private List<String> attrsCompared;
        private Map<String, Double> playerPreAttrs;
        private Map<String, Double> npcPreAttrs;
        private double playerSum;
        private double npcSum;
        private String winner;
        private double stealTotal;
        private Map<String, Double> stolenValues;
        private boolean isExecution;
        private double timer;

        public String getPhase() { return phase; }
        public void setPhase(String phase) { this.phase = phase; }
        public String getRoomId() { return roomId; }
        public void setRoomId(String roomId) { this.roomId = roomId; }
        public String getRoomName() { return roomName; }
        public void setRoomName(String roomName) { this.roomName = roomName; }
        public List<String> getAttrsCompared() { return attrsCompared; }
        public void setAttrsCompared(List<String> attrsCompared) { this.attrsCompared = attrsCompared; }
        public Map<String, Double> getPlayerPreAttrs() { return playerPreAttrs; }
        public void setPlayerPreAttrs(Map<String, Double> playerPreAttrs) { this.playerPreAttrs = playerPreAttrs; }
        public Map<String, Double> getNpcPreAttrs() { return npcPreAttrs; }
        public void setNpcPreAttrs(Map<String, Double> npcPreAttrs) { this.npcPreAttrs = npcPreAttrs; }
        public double getPlayerSum() { return playerSum; }
        public void setPlayerSum(double playerSum) { this.playerSum = playerSum; }
        public double getNpcSum() { return npcSum; }
        public void setNpcSum(double npcSum) { this.npcSum = npcSum; }
        public String getWinner() { return winner; }
        public void setWinner(String winner) { this.winner = winner; }
        public double getStealTotal() { return stealTotal; }
        public void setStealTotal(double stealTotal) { this.stealTotal = stealTotal; }
        public Map<String, Double> getStolenValues() { return stolenValues; }
        public void setStolenValues(Map<String, Double> stolenValues) { this.stolenValues = stolenValues; }
        public boolean isExecution() { return isExecution; }
        public void setExecution(boolean execution) { isExecution = execution; }
        public double getTimer() { return timer; }
        public void setTimer(double timer) { this.timer = timer; }
    }

    public static class ReadingDTO {
        private int bookType;
        private double timer;
        private double corruption;
        private List<WordDTO> words;

        public int getBookType() { return bookType; }
        public void setBookType(int bookType) { this.bookType = bookType; }
        public double getTimer() { return timer; }
        public void setTimer(double timer) { this.timer = timer; }
        public double getCorruption() { return corruption; }
        public void setCorruption(double corruption) { this.corruption = corruption; }
        public List<WordDTO> getWords() { return words; }
        public void setWords(List<WordDTO> words) { this.words = words; }
    }

    public static class WordDTO {
        private int id;
        private String text;
        private boolean isCorrupt;
        private double rot;

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
        public boolean isCorrupt() { return isCorrupt; }
        public void setCorrupt(boolean corrupt) { isCorrupt = corrupt; }
        public double getRot() { return rot; }
        public void setRot(double rot) { this.rot = rot; }
    }

    public static class DivinationDTO {
        private String card;
        private double timer;
        private String displayName;

        public String getCard() { return card; }
        public void setCard(String card) { this.card = card; }
        public double getTimer() { return timer; }
        public void setTimer(double timer) { this.timer = timer; }
        public String getDisplayName() { return displayName; }
        public void setDisplayName(String displayName) { this.displayName = displayName; }
    }

    public static class TimersDTO {
        private double invisibility;
        private double trapped;
        private double divinationCooldown;
        private double showWarning;
        private double reallocProgress;

        public double getInvisibility() { return invisibility; }
        public void setInvisibility(double invisibility) { this.invisibility = invisibility; }
        public double getTrapped() { return trapped; }
        public void setTrapped(double trapped) { this.trapped = trapped; }
        public double getDivinationCooldown() { return divinationCooldown; }
        public void setDivinationCooldown(double divinationCooldown) { this.divinationCooldown = divinationCooldown; }
        public double getShowWarning() { return showWarning; }
        public void setShowWarning(double showWarning) { this.showWarning = showWarning; }
        public double getReallocProgress() { return reallocProgress; }
        public void setReallocProgress(double reallocProgress) { this.reallocProgress = reallocProgress; }
    }

    // --- Getters/Setters ---

    public GameStatus getStatus() { return status; }
    public void setStatus(GameStatus status) { this.status = status; }
    public String getPlayerLoc() { return playerLoc; }
    public void setPlayerLoc(String playerLoc) { this.playerLoc = playerLoc; }
    public Map<String, Double> getPlayerAttrs() { return playerAttrs; }
    public void setPlayerAttrs(Map<String, Double> playerAttrs) { this.playerAttrs = playerAttrs; }
    public PendingRealloc getPendingRealloc() { return pendingRealloc; }
    public void setPendingRealloc(PendingRealloc pendingRealloc) { this.pendingRealloc = pendingRealloc; }
    public String getNpcLoc() { return npcLoc; }
    public void setNpcLoc(String npcLoc) { this.npcLoc = npcLoc; }
    public Map<String, Double> getNpcAttrs() { return npcAttrs; }
    public void setNpcAttrs(Map<String, Double> npcAttrs) { this.npcAttrs = npcAttrs; }
    public BeastDTO getBeast() { return beast; }
    public void setBeast(BeastDTO beast) { this.beast = beast; }
    public CombatUpdateDTO getCombat() { return combat; }
    public void setCombat(CombatUpdateDTO combat) { this.combat = combat; }
    public ReadingDTO getReading() { return reading; }
    public void setReading(ReadingDTO reading) { this.reading = reading; }
    public DivinationDTO getDivination() { return divination; }
    public void setDivination(DivinationDTO divination) { this.divination = divination; }
    public List<String> getInventory() { return inventory; }
    public void setInventory(List<String> inventory) { this.inventory = inventory; }
    public List<String> getTraps() { return traps; }
    public void setTraps(List<String> traps) { this.traps = traps; }
    public List<String> getLogs() { return logs; }
    public void setLogs(List<String> logs) { this.logs = logs; }
    public TimersDTO getTimers() { return timers; }
    public void setTimers(TimersDTO timers) { this.timers = timers; }
    public List<Integer> getCompletedBooks() { return completedBooks; }
    public void setCompletedBooks(List<Integer> completedBooks) { this.completedBooks = completedBooks; }
    public boolean isInstantReallocActive() { return instantReallocActive; }
    public void setInstantReallocActive(boolean instantReallocActive) { this.instantReallocActive = instantReallocActive; }
}
