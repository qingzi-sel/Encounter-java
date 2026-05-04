package com.encounter.event;

import com.encounter.domain.spec.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Simple in-memory event bus for decoupled service communication.
 * Listeners register by event type. Events are dispatched synchronously in the game loop thread.
 */
@Component
public class GameEventBus {

    private final ConcurrentHashMap<Class<? extends GameEvent>, List<EventListener<?>>> listeners = new ConcurrentHashMap<>();

    /**
     * Register a listener for a specific event type.
     */
    @SuppressWarnings("unchecked")
    public <T extends GameEvent> void subscribe(Class<T> eventType, EventListener<T> listener) {
        listeners.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>()).add(listener);
    }

    /**
     * Publish an event to all registered listeners.
     */
    @SuppressWarnings("unchecked")
    public <T extends GameEvent> void publish(T event) {
        List<EventListener<?>> eventListeners = listeners.get(event.getClass());
        if (eventListeners != null) {
            for (EventListener<?> listener : eventListeners) {
                try {
                    ((EventListener<T>) listener).onEvent(event);
                } catch (Exception e) {
                    System.err.println("Error handling event " + event.getClass().getSimpleName() + ": " + e.getMessage());
                }
            }
        }
    }
}
