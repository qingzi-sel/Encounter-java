package com.encounter.domain.spec;

import com.encounter.domain.model.ItemType;
import com.encounter.engine.GameState;

/**
 * Extension point: defines the effect of using an item.
 * Implement to create new items without modifying existing code.
 */
public interface ItemEffect {

    /** Which item this effect applies to. */
    ItemType getItemType();

    /** Execute the item's effect on the game state. */
    void apply(GameState state);

    /** Description shown in the game log when used. */
    String getApplyMessage();
}
