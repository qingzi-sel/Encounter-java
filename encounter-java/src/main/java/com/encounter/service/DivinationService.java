package com.encounter.service;

import com.encounter.config.GameConfig;
import com.encounter.domain.model.*;
import com.encounter.engine.GameState;
import com.encounter.service.CombatService;
import org.springframework.stereotype.Service;

import java.util.Random;

/**
 * Manages the observatory divination (tarot card) mechanics.
 */
@Service
public class DivinationService {

    private final LogService logService;
    private final GameConfig config;
    private final CombatService combatService;
    private final Random random = new Random();

    private static final TarotCard[] CARDS = TarotCard.values();

    public DivinationService(LogService logService, GameConfig config, CombatService combatService) {
        this.logService = logService;
        this.config = config;
        this.combatService = combatService;
    }

    /**
     * Start divination. Creates a random tarot card result and enters DIVINATION state.
     */
    public void startDivination(GameState s) {
        if (s.getStatus() != GameStatus.PLAYING) return;

        TarotCard card = CARDS[random.nextInt(CARDS.length)];
        s.setDivinationResult(new DivinationResult(card));
        s.setStatus(GameStatus.DIVINATION);
        logService.addLog(s, "✨ 开始星象仪占卜...");
    }

    /**
     * Tick the divination display timer. Called from game loop when status == DIVINATION.
     */
    public void tick(GameState s, double dt) {
        DivinationResult dr = s.getDivinationResult();
        if (dr == null) {
            s.setStatus(GameStatus.PLAYING);
            return;
        }

        dr.setTimer(dr.getTimer() + dt);
        if (dr.getTimer() > config.getDivination().getDisplayDuration()) {
            TarotCard card = dr.getCard();
            s.setDivinationResult(null);
            s.setStatus(GameStatus.PLAYING);
            s.setDivinationCooldown(config.getDivination().getFallbackCooldown()); // Disabled cooldown per original game

            applyCardEffect(s, card);
        }
    }

    private void applyCardEffect(GameState s, TarotCard card) {
        switch (card) {
            case HERMIT -> {
                s.getInventory().add(ItemType.ETHER_POTION);
                logService.addLog(s, "🎴 塔罗牌《隐者》。你获得了【隐世药剂】。");
            }
            case WHEEL -> {
                s.getInventory().add(ItemType.HOURGLASS);
                logService.addLog(s, "🎴 塔罗牌《命运之轮》。你获得了【时光沙漏】。");
            }
            case HANGED -> {
                s.getInventory().add(ItemType.STRAW_DOLL);
                logService.addLog(s, "🎴 塔罗牌《倒吊人》。你获得了【厄运稻草人】。");
            }
            case TOWER -> {
                logService.addLog(s, "🎴 塔罗牌《高塔》！你遭到占卜反噬，被定身 4 秒。你的位置被锁定了！");
                s.setTrappedTimer(config.getDivination().getTowerStunDuration());
                s.setNpcLoc(s.getPlayerLoc());
                s.setNpcAdaptedInRoom(false);
                s.setNpcRoomTimer(0);
                combatService.checkAndStartCombat(s);
            }
        }
    }
}
