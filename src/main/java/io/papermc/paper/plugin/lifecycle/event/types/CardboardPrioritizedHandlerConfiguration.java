package io.papermc.paper.plugin.lifecycle.event.types;

import io.papermc.paper.plugin.lifecycle.event.LifecycleEvent;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventOwner;
import io.papermc.paper.plugin.lifecycle.event.handler.LifecycleEventHandler;
import io.papermc.paper.plugin.lifecycle.event.handler.configuration.PrioritizedLifecycleEventHandlerConfiguration;

public final class CardboardPrioritizedHandlerConfiguration<O extends LifecycleEventOwner, E extends LifecycleEvent>
        implements PrioritizedLifecycleEventHandlerConfiguration<O>, CardboardHandlerConfiguration {

    private final CardboardLifecycleEventType<O, E, ?> eventType;
    private final LifecycleEventHandler<? super E> handler;
    private int priority;
    private boolean monitor;

    CardboardPrioritizedHandlerConfiguration(CardboardLifecycleEventType<O, E, ?> eventType, LifecycleEventHandler<? super E> handler) {
        this.eventType = eventType;
        this.handler = handler;
    }

    @Override
    public PrioritizedLifecycleEventHandlerConfiguration<O> priority(int priority) {
        this.priority = priority;
        this.monitor = false;
        return this;
    }

    @Override
    public PrioritizedLifecycleEventHandlerConfiguration<O> monitor() {
        this.monitor = true;
        return this;
    }

    @Override
    public void registerTo(LifecycleEventOwner owner) {
        this.eventType.register(owner, this.handler, this.priority, this.monitor);
    }
}
