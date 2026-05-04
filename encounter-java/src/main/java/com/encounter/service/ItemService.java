package com.encounter.service;

import com.encounter.config.GameConfig;
import com.encounter.domain.model.AttributeType;
import com.encounter.domain.model.ItemType;
import com.encounter.domain.model.RoomLayout;
import com.encounter.engine.GameState;
import com.encounter.event.GameEventBus;
import com.encounter.event.ItemUsedEvent;
import org.springframework.stereotype.Service;

/**
 * Handles item usage logic.
 */
@Service
public class ItemService {

    private final LogService logService;
    private final GameEventBus eventBus;
    private final GameConfig config;

    public ItemService(LogService logService, GameEventBus eventBus, GameConfig config) {
        this.logService = logService;
        this.eventBus = eventBus;
        this.config = config;
    }

    public void useItem(GameState s, ItemType itemType) {
        if (!s.getInventory().contains(itemType)) return;

        s.getInventory().remove(itemType);

        switch (itemType) {
            case ETHER_POTION -> {
                s.setInvisibilityTimer(config.getItems().getInvisibilityDuration());
                logService.addLog(s, "✨ 你饮下了【隐世药剂】，获得了虚无状态！");
            }
            case HOURGLASS -> {
                s.setInstantReallocActive(true);
                logService.addLog(s, "⏳ 【时光沙漏】已激活！下一次属性重组将瞬间完成！");
            }
            case STRAW_DOLL -> {
                s.getTraps().add(s.getPlayerLoc());
                logService.addLog(s, "🔥 你在 " + RoomLayout.get(s.getPlayerLoc()).getName() + " 放置了【厄运稻草人】。");
                // Immediate trigger if NPC is already in the room
                if (s.getNpcLoc() == s.getPlayerLoc()) {
                    s.getTraps().remove(s.getPlayerLoc());
                    logService.addLog(s, "🔥 刚放置的厄运稻草人立即被NPC触发，其全属性被强制削弱！");
                    for (AttributeType key : AttributeType.values()) {
                        s.getNpcAttrs().set(key, Math.max(0, s.getNpcAttrs().get(key) / 2.0));
                    }
                }
            }
        }

        eventBus.publish(new ItemUsedEvent(itemType));
    }
}
