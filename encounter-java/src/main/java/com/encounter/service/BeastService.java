package com.encounter.service;

import com.encounter.config.GameConfig;
import com.encounter.domain.model.BeastState;
import com.encounter.domain.model.GameStatus;
import com.encounter.domain.model.RoomId;
import com.encounter.domain.model.RoomLayout;
import com.encounter.engine.GameState;
import com.encounter.event.BeastEscapedEvent;
import com.encounter.event.GameEventBus;
import com.encounter.util.RandomUtil;
import org.springframework.stereotype.Service;

/**
 * Manages the contained/escaped beast entity in the dungeon.
 */
@Service
public class BeastService {

    private final LogService logService;
    private final GameEventBus eventBus;
    private final GameConfig config;

    public BeastService(LogService logService, GameEventBus eventBus, GameConfig config) {
        this.logService = logService;
        this.eventBus = eventBus;
        this.config = config;
    }

    /**
     * Tick the beast: satiety decay, escape check, movement, and kill check.
     */
    public void tick(GameState s, double dt) {
        BeastState b = s.getBeast();

        if (b.getState() == BeastState.BeastStatus.CONTAINED) {
            if (s.isFeedingBeast() && s.getPlayerLoc() == RoomId.DUNGEON) {
                b.setSatiety(b.getSatiety() + config.getBeast().getFeedRate() * dt);
                if (b.getSatiety() > config.getBeast().getInitialSatiety()) {
                    b.setSatiety(config.getBeast().getInitialSatiety());
                }
            } else {
                b.setSatiety(b.getSatiety() - config.getBeast().getDecayRate() * dt);
            }

            if (b.getSatiety() <= 0) {
                b.setSatiety(0);
                b.setState(BeastState.BeastStatus.ESCAPED);
                eventBus.publish(new BeastEscapedEvent(b.getLoc()));
                logService.addLog(s, "🚨 警告：地牢怪物已突破收容！正在全区域猎杀！");
            }
        } else {
            b.setMoveTimer(b.getMoveTimer() + dt);
            if (b.getMoveTimer() >= config.getBeast().getMoveInterval()) {
                b.setMoveTimer(0);
                var currentRoom = RoomLayout.get(b.getLoc());
                if (currentRoom != null && !currentRoom.getAdj().isEmpty()) {
                    RoomId next = RandomUtil.pickRandom(currentRoom.getAdj());
                    b.setLoc(next);
                    logService.addLog(s, "⚠️ 狂暴怪物移动到了 [" + RoomLayout.get(next).getName() + "]。");

                    if (b.getLoc() == s.getPlayerLoc()) {
                        logService.addLog(s, "💀 你被突脸的狂暴怪物瞬间撕碎。游戏结束。");
                        s.setStatus(GameStatus.GAMEOVER);
                    }
                }
            }
        }
    }
}
