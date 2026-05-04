package com.encounter.domain.spec;

import com.encounter.domain.model.RoomId;
import com.encounter.engine.GameState;

/**
 * Extension point: special effect triggered when entering a room.
 * Implement to add unique room behavior.
 */
public interface RoomEffect {

    /** Which room this effect belongs to. */
    RoomId getRoomId();

    /** Called when the player enters this room. */
    void onPlayerEnter(GameState state);

    /** Priority (lower = runs first). */
    default int priority() { return 100; }
}
