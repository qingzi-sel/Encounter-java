package com.encounter.service;

import com.encounter.config.GameConfig;
import com.encounter.domain.model.AttributeType;
import com.encounter.domain.model.Attributes;
import com.encounter.engine.GameState;
import com.encounter.util.AttributeMath;
import org.springframework.stereotype.Service;

/**
 * Handles player attribute reallocation with delay mechanics.
 */
@Service
public class ReallocationService {

    private final LogService logService;
    private final GameConfig config;

    public ReallocationService(LogService logService, GameConfig config) {
        this.logService = logService;
        this.config = config;
    }

    /**
     * Tick the reallocation timer. Called from game loop.
     */
    public void tick(GameState s, double dt) {
        if (s.getPendingPlayerAttrs() != null) {
            s.setReallocTimer(s.getReallocTimer() + dt);
            if (s.getReallocTimer() >= config.getReallocation().getDuration()) {
                // Apply the pending attributes
                Attributes pending = s.getPendingPlayerAttrs();
                for (AttributeType type : AttributeType.values()) {
                    s.getPlayerAttrs().set(type, pending.get(type));
                }
                AttributeMath.snapAllInPlace(s.getPlayerAttrs());
                s.setPendingPlayerAttrs(null);
                s.setReallocTimer(0);
                logService.addLog(s, "✨ 属性重组完成。");
            }
        }
    }

    /**
     * Apply a draft attribute distribution.
     */
    public void applyDraft(GameState s, Attributes draft) {
        double currentHP = AttributeMath.calcHP(s.getPlayerAttrs());
        double draftHP = AttributeMath.calcHP(draft);

        if (draftHP > currentHP + 0.01) {
            logService.addLog(s, "⚠️ 新分配的总值超过了当前血量！");
            return;
        }

        if (s.isInstantReallocActive()) {
            for (AttributeType type : AttributeType.values()) {
                s.getPlayerAttrs().set(type, AttributeMath.snapVal(draft.get(type)));
            }
            s.setPendingPlayerAttrs(null);
            s.setReallocTimer(0);
            s.setInstantReallocActive(false);
            logService.addLog(s, "⏳ 时光沙漏被消耗：属性瞬间重组完成了！");
        } else {
            s.setPendingPlayerAttrs(draft.copy());
            s.setReallocTimer(0);
            logService.addLog(s, "⚙️ 开始重组属性，需要 " + (int) config.getReallocation().getDuration() + " 秒生效时间...");
        }
    }

    /**
     * Cancel an ongoing reallocation.
     */
    public void cancelDraft(GameState s) {
        if (s.getPendingPlayerAttrs() != null) {
            s.setPendingPlayerAttrs(null);
            s.setReallocTimer(0);
            logService.addLog(s, "🚫 放弃了属性重组。");
        }
    }
}
