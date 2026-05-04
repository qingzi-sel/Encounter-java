package com.encounter.service;

import com.encounter.config.GameConfig;
import com.encounter.domain.model.*;
import com.encounter.engine.GameState;
import com.encounter.event.CombatResolvedEvent;
import com.encounter.event.CombatStartedEvent;
import com.encounter.event.GameEventBus;
import com.encounter.util.AttributeMath;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Handles combat encounter logic: per-attribute comparison, resolution, and post-combat displacement.
 */
@Service
public class CombatService {

    private final LogService logService;
    private final GameEventBus eventBus;
    private final GameConfig config;
    private final Random random = new Random();

    public CombatService(LogService logService, GameEventBus eventBus, GameConfig config) {
        this.logService = logService;
        this.eventBus = eventBus;
        this.config = config;
    }

    /**
     * Check if player and NPC are in the same room, and start combat if so.
     * Combat now compares each attribute individually — every attribute is its own mini-battle.
     */
    public void checkAndStartCombat(GameState s) {
        if (s.getStatus() != GameStatus.PLAYING) return;
        if (s.getPlayerLoc() != s.getNpcLoc()) return;

        Room room = RoomLayout.get(s.getPlayerLoc());
        // Safe rooms (no attributes) — combat does not trigger
        if (room.getAttrs().isEmpty()) return;

        // Invisibility
        if (s.getInvisibilityTimer() > 0) {
            logService.addLog(s, "👻 NPC进入了房间，但由于处于以太虚无状态，它无法察觉你的存在。");
            return;
        }

        // Cancel reallocation
        if (s.getPendingPlayerAttrs() != null) {
            s.setPendingPlayerAttrs(null);
            s.setReallocTimer(0);
            logService.addLog(s, "⚠️ 警告：重组进程因遭遇战被强制中断。");
        }

        List<AttributeType> activeAttrs = room.getAttrs();

        // Per-attribute comparison
        Map<AttributeType, Double> playerPre = new EnumMap<>(AttributeType.class);
        Map<AttributeType, Double> npcPre = new EnumMap<>(AttributeType.class);
        Map<AttributeType, Double> stolenValues = new EnumMap<>(AttributeType.class);
        Map<AttributeType, CombatData.Winner> perAttrWinners = new EnumMap<>(AttributeType.class);

        int playerWins = 0;
        int npcWins = 0;
        double stealTotal = 0;
        boolean isExecution = false;
        double npcHP = AttributeMath.calcHP(s.getNpcAttrs());

        for (AttributeType a : activeAttrs) {
            double effP = s.getEffectivePlayerAttr(a);
            double nVal = s.getNpcAttrs().get(a);
            playerPre.put(a, effP);
            npcPre.put(a, nVal);

            if (effP > nVal + 0.01) {
                // Player wins this attribute
                perAttrWinners.put(a, CombatData.Winner.PLAYER);
                playerWins++;
                double stolen;
                if (npcHP < config.getCombat().getExecutionThreshold()) {
                    isExecution = true;
                    stolen = nVal; // execution: steal all
                } else {
                    stolen = AttributeMath.snapVal(nVal * config.getCombat().getStealRatio());
                }
                stolenValues.put(a, stolen);
                stealTotal += stolen;
            } else if (nVal > effP + 0.01) {
                // NPC wins this attribute
                perAttrWinners.put(a, CombatData.Winner.NPC);
                npcWins++;
                double stolen = AttributeMath.snapVal(effP * config.getCombat().getStealRatio());
                stolenValues.put(a, stolen);
                stealTotal += stolen;
            } else {
                // Draw on this attribute
                perAttrWinners.put(a, CombatData.Winner.DRAW);
            }
        }

        // Overall winner: side that won more attributes
        CombatData.Winner overallWinner;
        if (playerWins > npcWins) {
            overallWinner = CombatData.Winner.PLAYER;
        } else if (npcWins > playerWins) {
            overallWinner = CombatData.Winner.NPC;
        } else {
            overallWinner = CombatData.Winner.DRAW;
        }

        // Sum for display
        double pScore = playerPre.values().stream().mapToDouble(Double::doubleValue).sum();
        double nScore = npcPre.values().stream().mapToDouble(Double::doubleValue).sum();

        CombatData cd = new CombatData();
        cd.setPhase(CombatPhase.STARTING);
        cd.setRoomId(s.getPlayerLoc());
        cd.setAttrsCompared(activeAttrs);
        cd.setPlayerSum(pScore);
        cd.setNpcSum(nScore);
        cd.setWinner(overallWinner);
        cd.setStealTotal(stealTotal);
        cd.setStolenValues(stolenValues);
        cd.setPlayerPreAttrs(playerPre);
        cd.setNpcPreAttrs(npcPre);
        cd.setExecution(isExecution);

        s.setCombatData(cd);
        s.setStatus(GameStatus.COMBAT);
        eventBus.publish(new CombatStartedEvent(s.getPlayerLoc()));
        logService.addLog(s, "⚔️ 遭遇战触发！[" + room.getName() + "] 正在进行空间封锁！");
    }

    /**
     * Tick combat phases. Called from the game loop when status == COMBAT.
     */
    public void tick(GameState s, double dt) {
        CombatData cd = s.getCombatData();
        if (cd == null) {
            s.setStatus(GameStatus.PLAYING);
            return;
        }

        cd.setTimer(cd.getTimer() + dt);

        if (cd.getPhase() == CombatPhase.STARTING && cd.getTimer() > config.getCombat().getStartPhaseDuration()) {
            cd.setPhase(CombatPhase.COMPARING);
            logService.addLog(s, "📊 属性比对开始...");
        } else if (cd.getPhase() == CombatPhase.COMPARING && cd.getTimer() > config.getCombat().getComparePhaseDuration()) {
            cd.setPhase(CombatPhase.RESULT);
            applyCombatResult(s, cd);
        } else if (cd.getPhase() == CombatPhase.RESULT && cd.getTimer() > config.getCombat().getResultPhaseDuration()) {
            double pHP = AttributeMath.calcHP(s.getPlayerAttrs());
            double nHP = AttributeMath.calcHP(s.getNpcAttrs());
            if (pHP <= 0 || nHP <= 0) {
                s.setStatus(GameStatus.PLAYING);
                s.setCombatData(null);
                return;
            }
            // Post-combat displacement
            List<RoomId> allRooms = new ArrayList<>(RoomLayout.ROOMS.keySet());
            allRooms.remove(s.getPlayerLoc());
            RoomId dest = allRooms.get(random.nextInt(allRooms.size()));
            s.setPlayerLoc(dest);
            logService.addLog(s, "🌀 战后排斥引擎启动，系统自动执行紧急跳跃至 [" + RoomLayout.get(dest).getName() + "]。");
            s.setStatus(GameStatus.PLAYING);
            s.setCombatData(null);
        }
    }

    /**
     * Apply per-attribute steal results: each side gains what they won and loses what they lost.
     */
    private void applyCombatResult(GameState s, CombatData cd) {
        Map<AttributeType, Double> stolen = cd.getStolenValues();

        // Determine per-attribute winners by comparing pre-attrs
        for (Map.Entry<AttributeType, Double> entry : stolen.entrySet()) {
            AttributeType attr = entry.getKey();
            double amount = entry.getValue();
            double pPre = cd.getPlayerPreAttrs().getOrDefault(attr, 0.0);
            double nPre = cd.getNpcPreAttrs().getOrDefault(attr, 0.0);

            if (pPre > nPre + 0.01) {
                // Player won this attribute: steal from NPC
                s.getNpcAttrs().set(attr, s.getNpcAttrs().get(attr) - amount);
                s.getPlayerAttrs().set(attr, s.getPlayerAttrs().get(attr) + amount);
            } else if (nPre > pPre + 0.01) {
                // NPC won this attribute: steal from player
                s.getPlayerAttrs().set(attr, s.getPlayerAttrs().get(attr) - amount);
                s.getNpcAttrs().set(attr, s.getNpcAttrs().get(attr) + amount);
            }
            // Draw: no change for this attribute
        }

        AttributeMath.snapAllInPlace(s.getPlayerAttrs());
        AttributeMath.snapAllInPlace(s.getNpcAttrs());

        String winner = cd.getWinner() == CombatData.Winner.PLAYER ? "player" :
                cd.getWinner() == CombatData.Winner.NPC ? "npc" : "draw";

        eventBus.publish(new CombatResolvedEvent(cd.getRoomId(), winner, cd.getStealTotal(), stolen, cd.isExecution()));

        if (cd.getWinner() == CombatData.Winner.PLAYER) {
            if (cd.isExecution()) {
                logService.addLog(s, "💀 斩杀！目标全线溃败，你吸取了其全部剩余 " + String.format("%.1f", cd.getStealTotal()) + " 点属性。");
            } else {
                logService.addLog(s, "🏆 胜！汲取 " + String.format("%.1f", cd.getStealTotal()) + " 点。");
            }
        } else if (cd.getWinner() == CombatData.Winner.NPC) {
            logService.addLog(s, "💀 败！被夺走 " + String.format("%.1f", cd.getStealTotal()) + " 点。");
        } else {
            logService.addLog(s, "🤝 平局！无属性变动。");
        }
    }
}
