package com.encounter.domain.spec;

import com.encounter.event.GameEvent;

/**
 * Generic event listener marker. Each specific event type has its own listener interface.
 *
 * @param <T> the event type to listen for
 */
@FunctionalInterface
public interface EventListener<T extends GameEvent> {
    void onEvent(T event);
}
