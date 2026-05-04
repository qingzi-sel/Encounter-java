package com.encounter.service;

import com.encounter.config.GameConfig;
import com.encounter.domain.model.*;
import com.encounter.engine.GameState;
import com.encounter.event.GameEventBus;
import com.encounter.event.NpcMovedEvent;
import com.encounter.util.AttributeMath;
import com.encounter.util.RandomUtil;
import org.springframework.stereotype.Service;

/**
 * NPC AI: movement and room adaptation logic.
 */
@Service
public class NpcService {

    private final LogService logService;
    private final GameEventBus eventBus;
    private final GameConfig config;

    public NpcService(LogService logService, GameEventBus eventBus, GameConfig config) {
        this.logService = logService;
        this.eventBus = eventBus;
        this.config = config;
    }

    /**
     * NPC adapts to rooms: redistributes HP evenly across the room's active attributes.
     */
    public void tickAdaptation(GameState s, double dt) {
        Room room = RoomLayout.get(s.getNpcLoc());
        if (room == null || room.getAttrs().isEmpty() || s.isNpcAdaptedInRoom()) return;

        s.setNpcRoomTimer(s.getNpcRoomTimer() + dt);
        if (s.getNpcRoomTimer() >= config.getNpc().getAdaptTime()) {
            double nHP = AttributeMath.calcHP(s.getNpcAttrs());
            double splitAmount = AttributeMath.snapVal(nHP / room.getAttrs().size());

            for (AttributeType type : AttributeType.values()) {
                s.getNpcAttrs().set(type, 0.0);
            }
            for (AttributeType a : room.getAttrs()) {
                s.getNpcAttrs().set(a, splitAmount);
            }
            AttributeMath.snapAllInPlace(s.getNpcAttrs());
            s.setNpcAdaptedInRoom(true);

            String attrNames = String.join("与", room.getAttrs().stream().map(AttributeType::getDisplayName).toList());
            logService.addLog(s, "🧟 NPC 在 " + room.getName() + " 中完成了环境适应，属性已重新分配至: " + attrNames + "。");
        }
    }

    /**
     * Tick NPC movement. Moves to a random adjacent room periodically.
     */
    public void tickMovement(GameState s, double dt) {
        s.setNpcMoveTimer(s.getNpcMoveTimer() + dt);
        if (s.getNpcMoveTimer() < s.getNpcNextMoveWait()) return;

        s.setNpcMoveTimer(0);
        s.setNpcNextMoveWait(RandomUtil.nextDouble(config.getNpc().getMoveIntervalMin(), config.getNpc().getMoveIntervalMax()));

        Room currentRoom = RoomLayout.get(s.getNpcLoc());
        if (currentRoom == null || currentRoom.getAdj().isEmpty()) return;

        RoomId fromRoom = s.getNpcLoc();
        RoomId nextRoom = RandomUtil.pickRandom(currentRoom.getAdj());
        s.setNpcLoc(nextRoom);
        s.setNpcRoomTimer(0);
        s.setNpcAdaptedInRoom(false);

        logService.addLog(s, "👣 检测到异常移动信号... (NPC 移动到了未知房间)");

        // Check for traps
        if (s.getTraps().contains(s.getNpcLoc())) {
            s.getTraps().remove(s.getNpcLoc());
            logService.addLog(s, "🔥 [远处] 传来异响！NPC触发了厄运稻草人，全属性被强制削弱！");
            for (AttributeType key : AttributeType.values()) {
                s.getNpcAttrs().set(key, Math.max(0, s.getNpcAttrs().get(key) / 2.0));
            }
        }

        eventBus.publish(new NpcMovedEvent(fromRoom, nextRoom));
    }
}
