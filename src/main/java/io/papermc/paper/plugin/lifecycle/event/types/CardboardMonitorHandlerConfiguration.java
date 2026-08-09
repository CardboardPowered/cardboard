package io.papermc.paper.plugin.lifecycle.event.types;

import io.papermc.paper.plugin.lifecycle.event.LifecycleEvent;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventOwner;
import io.papermc.paper.plugin.lifecycle.event.handler.LifecycleEventHandler;
import io.papermc.paper.plugin.lifecycle.event.handler.configuration.MonitorLifecycleEventHandlerConfiguration;

public final class CardboardMonitorHandlerConfiguration<O extends LifecycleEventOwner, E extends LifecycleEvent>
        implements MonitorLifecycleEventHandlerConfiguration<O>, CardboardHandlerConfiguration {

    private final CardboardLifecycleEventType<O, E, ?> eventType;
    private final LifecycleEventHandler<? super E> handler;
    private boolean monitor;

    CardboardMonitorHandlerConfiguration(CardboardLifecycleEventType<O, E, ?> eventType, LifecycleEventHandler<? super E> handler) {
        this.eventType = eventType;
        this.handler = handler;
    }

    @Override
    public MonitorLifecycleEventHandlerConfiguration<O> monitor() {
        this.monitor = true;
        return this;
    }

    @Override
    public void registerTo(LifecycleEventOwner owner) {
        this.eventType.register(owner, this.handler, 0, this.monitor);
    }
}
