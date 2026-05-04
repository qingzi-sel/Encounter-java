package com.encounter.event;

import com.encounter.domain.model.ItemType;

public class ItemUsedEvent extends GameEvent {
    private final ItemType itemType;

    public ItemUsedEvent(ItemType itemType) {
        this.itemType = itemType;
    }

    public ItemType getItemType() { return itemType; }
}
