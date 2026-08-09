package io.papermc.paper.plugin.lifecycle.event.types;

import io.papermc.paper.plugin.lifecycle.event.LifecycleEvent;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventOwner;
import io.papermc.paper.plugin.lifecycle.event.handler.LifecycleEventHandler;
import io.papermc.paper.plugin.lifecycle.event.handler.configuration.PrioritizedLifecycleEventHandlerConfiguration;

public final class CardboardPrioritizableEventType<O extends LifecycleEventOwner, E extends LifecycleEvent>
        extends CardboardLifecycleEventType<O, E, PrioritizedLifecycleEventHandlerConfiguration<O>>
        implements LifecycleEventType.Prioritizable<O, E> {

    CardboardPrioritizableEventType(String name) {
        super(name);
    }

    @Override
    public PrioritizedLifecycleEventHandlerConfiguration<O> newHandler(LifecycleEventHandler<? super E> handler) {
        return new CardboardPrioritizedHandlerConfiguration<>(this, handler);
    }
}
