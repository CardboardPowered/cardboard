package io.papermc.paper.plugin.lifecycle.event.types;

import io.papermc.paper.plugin.lifecycle.event.LifecycleEvent;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventOwner;
import io.papermc.paper.plugin.lifecycle.event.handler.LifecycleEventHandler;
import io.papermc.paper.plugin.lifecycle.event.handler.configuration.MonitorLifecycleEventHandlerConfiguration;

public final class CardboardMonitorableEventType<O extends LifecycleEventOwner, E extends LifecycleEvent>
        extends CardboardLifecycleEventType<O, E, MonitorLifecycleEventHandlerConfiguration<O>>
        implements LifecycleEventType.Monitorable<O, E> {

    CardboardMonitorableEventType(String name) {
        super(name);
    }

    @Override
    public MonitorLifecycleEventHandlerConfiguration<O> newHandler(LifecycleEventHandler<? super E> handler) {
        return new CardboardMonitorHandlerConfiguration<>(this, handler);
    }
}
