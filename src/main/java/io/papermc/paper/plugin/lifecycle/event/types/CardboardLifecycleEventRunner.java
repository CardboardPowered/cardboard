package io.papermc.paper.plugin.lifecycle.event.types;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import io.papermc.paper.plugin.lifecycle.event.LifecycleEvent;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventOwner;

/**
 * Entry point the rest of Cardboard uses to drive the plugin lifecycle events.
 */
public final class CardboardLifecycleEventRunner {

    private static final List<CardboardLifecycleEventType<?, ?, ?>> EVENT_TYPES = new CopyOnWriteArrayList<>();

    private static LifecycleEventOwner currentOwner;

    private CardboardLifecycleEventRunner() {
    }

    static void track(CardboardLifecycleEventType<?, ?, ?> eventType) {
        EVENT_TYPES.add(eventType);
    }

    /**
     * The plugin whose handler is running right now, or null outside of a handler. Registrars use
     * this to attribute what a handler registers back to the plugin that registered it.
     */
    public static LifecycleEventOwner currentOwner() {
        return currentOwner;
    }

    static void setCurrentOwner(LifecycleEventOwner owner) {
        currentOwner = owner;
    }

    @SuppressWarnings("unchecked")
    public static <E extends LifecycleEvent> void fire(LifecycleEventType<?, ? extends E, ?> eventType, E event) {
        if (eventType instanceof CardboardLifecycleEventType<?, ?, ?> cardboardType)
            ((CardboardLifecycleEventType<?, E, ?>) cardboardType).fire(event);
    }

    /**
     * Forgets every handler the given owner registered, for every lifecycle event. Called when a
     * plugin is disabled so a reload does not run its handlers twice.
     */
    public static void removeOwner(LifecycleEventOwner owner) {
        for (CardboardLifecycleEventType<?, ?, ?> eventType : EVENT_TYPES)
            eventType.removeOwner(owner);
    }
}
