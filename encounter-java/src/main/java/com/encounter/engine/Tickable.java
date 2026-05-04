package com.encounter.engine;

/**
 * A component that receives per-tick updates from the game engine.
 */
@FunctionalInterface
public interface Tickable {
    void tick(double dt);
}
