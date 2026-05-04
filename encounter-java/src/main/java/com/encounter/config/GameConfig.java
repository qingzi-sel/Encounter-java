package com.encounter.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Externalized game balance parameters loaded from application.yml.
 * Tweak values without recompiling.
 */
@Component
@ConfigurationProperties(prefix = "game")
public class GameConfig {

    private double maxHp = 100.0;
    private long tickIntervalMs = 50;

    private NpcConfig npc = new NpcConfig();
    private BeastConfig beast = new BeastConfig();
    private CombatConfig combat = new CombatConfig();
    private ReadingConfig reading = new ReadingConfig();
    private ReallocationConfig reallocation = new ReallocationConfig();
    private ItemsConfig items = new ItemsConfig();
    private DivinationConfig divination = new DivinationConfig();

    // --- Getters/Setters ---

    public double getMaxHp() { return maxHp; }
    public void setMaxHp(double maxHp) { this.maxHp = maxHp; }

    public long getTickIntervalMs() { return tickIntervalMs; }
    public void setTickIntervalMs(long tickIntervalMs) { this.tickIntervalMs = tickIntervalMs; }

    public NpcConfig getNpc() { return npc; }
    public void setNpc(NpcConfig npc) { this.npc = npc; }

    public BeastConfig getBeast() { return beast; }
    public void setBeast(BeastConfig beast) { this.beast = beast; }

    public CombatConfig getCombat() { return combat; }
    public void setCombat(CombatConfig combat) { this.combat = combat; }

    public ReadingConfig getReading() { return reading; }
    public void setReading(ReadingConfig reading) { this.reading = reading; }

    public ReallocationConfig getReallocation() { return reallocation; }
    public void setReallocation(ReallocationConfig reallocation) { this.reallocation = reallocation; }

    public ItemsConfig getItems() { return items; }
    public void setItems(ItemsConfig items) { this.items = items; }

    public DivinationConfig getDivination() { return divination; }
    public void setDivination(DivinationConfig divination) { this.divination = divination; }

    // --- Inner config classes ---

    public static class NpcConfig {
        private double moveIntervalMin = 1.5;
        private double moveIntervalMax = 2.5;
        private double adaptTime = 1.0;

        public double getMoveIntervalMin() { return moveIntervalMin; }
        public void setMoveIntervalMin(double moveIntervalMin) { this.moveIntervalMin = moveIntervalMin; }
        public double getMoveIntervalMax() { return moveIntervalMax; }
        public void setMoveIntervalMax(double moveIntervalMax) { this.moveIntervalMax = moveIntervalMax; }
        public double getAdaptTime() { return adaptTime; }
        public void setAdaptTime(double adaptTime) { this.adaptTime = adaptTime; }
    }

    public static class BeastConfig {
        private double initialSatiety = 100.0;
        private double decayRate = 2.0;
        private double feedRate = 15.0;
        private double moveInterval = 1.0;

        public double getInitialSatiety() { return initialSatiety; }
        public void setInitialSatiety(double initialSatiety) { this.initialSatiety = initialSatiety; }
        public double getDecayRate() { return decayRate; }
        public void setDecayRate(double decayRate) { this.decayRate = decayRate; }
        public double getFeedRate() { return feedRate; }
        public void setFeedRate(double feedRate) { this.feedRate = feedRate; }
        public double getMoveInterval() { return moveInterval; }
        public void setMoveInterval(double moveInterval) { this.moveInterval = moveInterval; }
    }

    public static class CombatConfig {
        private double stealRatio = 0.5;
        private double executionThreshold = 20.0;
        private double startPhaseDuration = 1.0;
        private double comparePhaseDuration = 2.5;
        private double resultPhaseDuration = 4.5;

        public double getStealRatio() { return stealRatio; }
        public void setStealRatio(double stealRatio) { this.stealRatio = stealRatio; }
        public double getExecutionThreshold() { return executionThreshold; }
        public void setExecutionThreshold(double executionThreshold) { this.executionThreshold = executionThreshold; }
        public double getStartPhaseDuration() { return startPhaseDuration; }
        public void setStartPhaseDuration(double startPhaseDuration) { this.startPhaseDuration = startPhaseDuration; }
        public double getComparePhaseDuration() { return comparePhaseDuration; }
        public void setComparePhaseDuration(double comparePhaseDuration) { this.comparePhaseDuration = comparePhaseDuration; }
        public double getResultPhaseDuration() { return resultPhaseDuration; }
        public void setResultPhaseDuration(double resultPhaseDuration) { this.resultPhaseDuration = resultPhaseDuration; }
    }

    public static class ReadingConfig {
        private double duration = 10.0;
        private double corruptionCap = 100.0;
        private double book20Reward = 5.0;
        private double book50Reward = 10.0;
        private double failureFocusLoss = 5.0;
        private double spawnInterval20 = 0.6;
        private double spawnInterval50 = 0.3;
        private double corruptionRate20 = 2.0;
        private double corruptionRate50 = 3.0;
        private double purifyAmount = 2.5;

        public double getDuration() { return duration; }
        public void setDuration(double duration) { this.duration = duration; }
        public double getCorruptionCap() { return corruptionCap; }
        public void setCorruptionCap(double corruptionCap) { this.corruptionCap = corruptionCap; }
        public double getBook20Reward() { return book20Reward; }
        public void setBook20Reward(double book20Reward) { this.book20Reward = book20Reward; }
        public double getBook50Reward() { return book50Reward; }
        public void setBook50Reward(double book50Reward) { this.book50Reward = book50Reward; }
        public double getFailureFocusLoss() { return failureFocusLoss; }
        public void setFailureFocusLoss(double failureFocusLoss) { this.failureFocusLoss = failureFocusLoss; }
        public double getSpawnInterval20() { return spawnInterval20; }
        public void setSpawnInterval20(double spawnInterval20) { this.spawnInterval20 = spawnInterval20; }
        public double getSpawnInterval50() { return spawnInterval50; }
        public void setSpawnInterval50(double spawnInterval50) { this.spawnInterval50 = spawnInterval50; }
        public double getCorruptionRate20() { return corruptionRate20; }
        public void setCorruptionRate20(double corruptionRate20) { this.corruptionRate20 = corruptionRate20; }
        public double getCorruptionRate50() { return corruptionRate50; }
        public void setCorruptionRate50(double corruptionRate50) { this.corruptionRate50 = corruptionRate50; }
        public double getPurifyAmount() { return purifyAmount; }
        public void setPurifyAmount(double purifyAmount) { this.purifyAmount = purifyAmount; }
    }

    public static class ReallocationConfig {
        private double duration = 4.0;

        public double getDuration() { return duration; }
        public void setDuration(double duration) { this.duration = duration; }
    }

    public static class ItemsConfig {
        private double invisibilityDuration = 5.0;

        public double getInvisibilityDuration() { return invisibilityDuration; }
        public void setInvisibilityDuration(double invisibilityDuration) { this.invisibilityDuration = invisibilityDuration; }
    }

    public static class DivinationConfig {
        private double cooldown = 60.0;
        private double fallbackCooldown = 0.0;
        private double towerStunDuration = 4.0;
        private double displayDuration = 3.0;

        public double getCooldown() { return cooldown; }
        public void setCooldown(double cooldown) { this.cooldown = cooldown; }
        public double getFallbackCooldown() { return fallbackCooldown; }
        public void setFallbackCooldown(double fallbackCooldown) { this.fallbackCooldown = fallbackCooldown; }
        public double getTowerStunDuration() { return towerStunDuration; }
        public void setTowerStunDuration(double towerStunDuration) { this.towerStunDuration = towerStunDuration; }
        public double getDisplayDuration() { return displayDuration; }
        public void setDisplayDuration(double displayDuration) { this.displayDuration = displayDuration; }
    }
}
