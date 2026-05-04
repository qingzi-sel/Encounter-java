package com.encounter.service;

import com.encounter.config.GameConfig;
import com.encounter.domain.model.AttributeType;
import com.encounter.domain.model.Attributes;
import com.encounter.engine.GameState;
import com.encounter.event.GameEventBus;
import com.encounter.util.AttributeMath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CombatServiceTest {

    private CombatService combatService;
    private LogService logService;
    private GameEventBus eventBus;
    private GameConfig config;

    @BeforeEach
    void setUp() {
        config = new GameConfig();
        // Set properties expected by tests
        config.getCombat().setStealRatio(0.5);
        config.getCombat().setExecutionThreshold(20.0);
        config.getCombat().setStartPhaseDuration(1.0);
        config.getCombat().setComparePhaseDuration(2.5);
        config.getCombat().setResultPhaseDuration(4.5);

        eventBus = new GameEventBus();
        logService = new LogService();
        combatService = new CombatService(logService, eventBus, config);
    }

    @Test
    void checkAndStartCombat_doesNotTrigger_whenNotInSameRoom() {
        GameState s = new GameState();
        // Player at MAIN_GATE, NPC at DUNGEON (default)
        s.setStatus(com.encounter.domain.model.GameStatus.PLAYING);
        combatService.checkAndStartCombat(s);

        assertNull(s.getCombatData());
        assertEquals(com.encounter.domain.model.GameStatus.PLAYING, s.getStatus());
    }

    @Test
    void calcHP_correctlySumsAttributes() {
        Attributes attrs = new Attributes(20, 30, 10, 25, 15);
        assertEquals(100.0, AttributeMath.calcHP(attrs));
    }

    @Test
    void combatTick_transitionsPhases() {
        GameState s = new GameState();
        s.setStatus(com.encounter.domain.model.GameStatus.PLAYING);

        // Set up NPC and player in same room
        s.setPlayerLoc(com.encounter.domain.model.RoomId.YARD);
        s.setNpcLoc(com.encounter.domain.model.RoomId.YARD);
        // Set attributes for both
        for (AttributeType t : AttributeType.values()) {
            s.getPlayerAttrs().set(t, 20.0);
            s.getNpcAttrs().set(t, 20.0);
        }

        combatService.checkAndStartCombat(s);
        assertEquals(com.encounter.domain.model.GameStatus.COMBAT, s.getStatus());
        assertNotNull(s.getCombatData());
        assertEquals(com.encounter.domain.model.CombatPhase.STARTING, s.getCombatData().getPhase());

        // Tick past STARTING phase
        combatService.tick(s, 1.5);
        assertEquals(com.encounter.domain.model.CombatPhase.COMPARING, s.getCombatData().getPhase());

        // Tick past COMPARING phase
        combatService.tick(s, 1.5);
        assertEquals(com.encounter.domain.model.CombatPhase.RESULT, s.getCombatData().getPhase());
    }
}
