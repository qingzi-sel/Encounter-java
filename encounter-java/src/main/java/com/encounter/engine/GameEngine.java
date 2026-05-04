package com.encounter.engine;

import com.encounter.config.GameConfig;
import com.encounter.domain.model.*;
import com.encounter.service.*;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Core game loop. Drives all systems at a fixed tick rate and pushes state to subscribers.
 *
 * Architecture: GameEngine owns the GameSession and orchestrates all services each tick.
 * Services contain pure logic and mutate GameState directly.
 */
@Service
public class GameEngine {

    private final GameConfig config;
    private final LogService logService;
    private final RoomService roomService;
    private final CombatService combatService;
    private final NpcService npcService;
    private final BeastService beastService;
    private final ReadingService readingService;
    private final DivinationService divinationService;
    private final ItemService itemService;
    private final ReallocationService reallocationService;

    private GameSession session;
    private ScheduledExecutorService scheduler;
    private Consumer<GameState> statePushCallback;

    public GameEngine(GameConfig config,
                      LogService logService,
                      RoomService roomService,
                      CombatService combatService,
                      NpcService npcService,
                      BeastService beastService,
                      ReadingService readingService,
                      DivinationService divinationService,
                      ItemService itemService,
                      ReallocationService reallocationService) {
        this.config = config;
        this.logService = logService;
        this.roomService = roomService;
        this.combatService = combatService;
        this.npcService = npcService;
        this.beastService = beastService;
        this.readingService = readingService;
        this.divinationService = divinationService;
        this.itemService = itemService;
        this.reallocationService = reallocationService;
    }

    /**
     * Register a callback for pushing game state to WebSocket clients.
     */
    public void setStatePushCallback(Consumer<GameState> callback) {
        this.statePushCallback = callback;
    }

    /**
     * Start a new game session with the given initial attributes.
     */
    public GameSession startGame(Attributes initialAttrs) {
        stopGame();
        session = new GameSession();
        GameState state = session.getState();

        for (AttributeType type : AttributeType.values()) {
            state.getPlayerAttrs().set(type, initialAttrs.get(type));
        }
        state.setShowWarningTimer(4.0);
        state.setStatus(GameStatus.PLAYING);
        logService.addLog(state, "🚀 系统初始化成功。当前位置：城堡大门。");

        startLoop();
        return session;
    }

    /**
     * Get the current session, or null if no game is running.
     */
    public GameSession getSession() {
        return session;
    }

    /**
     * Start the game loop.
     */
    private void startLoop() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "game-loop");
            t.setDaemon(true);
            return t;
        });
        long intervalMs = config.getTickIntervalMs();
        scheduler.scheduleAtFixedRate(this::tick, 0, intervalMs, TimeUnit.MILLISECONDS);
    }

    /**
     * Stop the game loop.
     */
    public void stopGame() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
            scheduler = null;
        }
        session = null;
    }

    /**
     * Single tick of the game loop.
     */
    private void tick() {
        if (session == null) return;
        GameState s = session.getState();
        double dt = config.getTickIntervalMs() / 1000.0;

        // --- Handle modal states ---
        if (s.getStatus() == GameStatus.DIVINATION) {
            divinationService.tick(s, dt);
            pushState();
            return;
        }

        if (s.getStatus() == GameStatus.READING) {
            readingService.tick(s, dt);
            pushState();
            return;
        }

        if (s.getStatus() == GameStatus.COMBAT) {
            combatService.tick(s, dt);
            pushState();
            return;
        }

        if (s.getStatus() == GameStatus.GAMEOVER) {
            pushState();
            return;
        }

        // --- Normal playing tick ---
        if (s.getStatus() != GameStatus.PLAYING) {
            pushState();
            return;
        }

        // 1. Update timers
        updateTimers(s, dt);

        // 2. Check death
        if (checkDeath(s)) {
            pushState();
            return;
        }

        // 3. Process player reallocation
        reallocationService.tick(s, dt);

        // 4. Process NPC adaptation + movement
        npcService.tickAdaptation(s, dt);
        npcService.tickMovement(s, dt);

        // 5. Process beast
        beastService.tick(s, dt);
        if (s.getStatus() == GameStatus.GAMEOVER) {
            pushState();
            return;
        }

        // 6. Check death again (npc/beast may have caused it)
        if (checkDeath(s)) {
            pushState();
            return;
        }

        pushState();
    }

    private void updateTimers(GameState s, double dt) {
        if (s.getShowWarningTimer() > 0) s.setShowWarningTimer(Math.max(0, s.getShowWarningTimer() - dt));
        if (s.getDivinationCooldown() > 0) s.setDivinationCooldown(Math.max(0, s.getDivinationCooldown() - dt));
        if (s.getInvisibilityTimer() > 0) s.setInvisibilityTimer(Math.max(0, s.getInvisibilityTimer() - dt));
        if (s.getTrappedTimer() > 0) s.setTrappedTimer(Math.max(0, s.getTrappedTimer() - dt));
    }

    private boolean checkDeath(GameState s) {
        double pHP = com.encounter.util.AttributeMath.calcHP(s.getPlayerAttrs());
        double nHP = com.encounter.util.AttributeMath.calcHP(s.getNpcAttrs());

        if (pHP <= 0) {
            logService.addLog(s, "🔴 生命归零，你死亡了。游戏结束。");
            s.setStatus(GameStatus.GAMEOVER);
            return true;
        }
        if (nHP <= 0) {
            logService.addLog(s, "🏆 NPC生命归零，你取得了胜利！");
            s.setStatus(GameStatus.GAMEOVER);
            return true;
        }

        // Absolute death check
        if (pHP > 0 && pHP < (nHP / 2.0) - 0.05) {
            logService.addLog(s, "⚠️ 【高维重力警告】");
            logService.addLog(s, "💀 你的生命总额（" + String.format("%.1f", pHP) + "）已跌破敌人最低可用功率（" + String.format("%.1f", nHP / 2) + "）。");
            logService.addLog(s, "🔴 绝对死局触发！在毫无胜算的数值黑洞面前，你被规则强制抹除！");
            for (AttributeType type : AttributeType.values()) {
                s.getPlayerAttrs().set(type, 0.0);
            }
            s.setStatus(GameStatus.GAMEOVER);
            return true;
        }
        return false;
    }

    private void pushState() {
        if (statePushCallback != null && session != null) {
            statePushCallback.accept(session.getState());
        }
    }

    // --- Public delegating methods for REST controller ---

    public void playerMove(RoomId targetId) {
        if (session == null) return;
        GameState s = session.getState();
        if (s.getStatus() != GameStatus.PLAYING) return;

        if (s.getTrappedTimer() > 0) {
            logService.addLog(s, "⛓️ 遭到高塔反噬，定身状态还有 " + (int) Math.ceil(s.getTrappedTimer()) + " 秒解除。无法移动！");
            return;
        }

        if (s.getBeast().getState() == BeastState.BeastStatus.ESCAPED && targetId == s.getBeast().getLoc()) {
            s.setPlayerLoc(targetId);
            logService.addLog(s, "💀 你冲进了狂暴怪物的房间，瞬间被撕碎。游戏结束。");
            s.setStatus(GameStatus.GAMEOVER);
            return;
        }

        if (!roomService.isAdjacent(s.getPlayerLoc(), targetId)) {
            logService.addLog(s, "🚫 无法到达目标房间。");
            return;
        }

        s.setPlayerLoc(targetId);
        logService.addLog(s, "🏃 移动至 " + RoomLayout.get(targetId).getName() + "。");

        combatService.checkAndStartCombat(s);
    }

    public void playerAllocate(Attributes draft) {
        if (session == null) return;
        reallocationService.applyDraft(session.getState(), draft);
    }

    public void cancelAllocation() {
        if (session == null) return;
        reallocationService.cancelDraft(session.getState());
    }

    public void startReading(int bookType) {
        if (session == null) return;
        readingService.startReading(session.getState(), bookType);
    }

    public void purifyWord(int wordId) {
        if (session == null) return;
        readingService.purifyWord(session.getState(), wordId);
    }

    public void startDivination() {
        if (session == null) return;
        divinationService.startDivination(session.getState());
    }

    public void useItem(ItemType itemType) {
        if (session == null) return;
        itemService.useItem(session.getState(), itemType);
    }

    public void setBeastFeeding(boolean feeding) {
        if (session == null) return;
        session.getState().setFeedingBeast(feeding);
    }
}
