package io.papermc.paper.plugin.lifecycle.event.types;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;

import io.papermc.paper.plugin.lifecycle.event.LifecycleEvent;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventOwner;
import io.papermc.paper.plugin.lifecycle.event.handler.LifecycleEventHandler;
import io.papermc.paper.plugin.lifecycle.event.handler.configuration.LifecycleEventHandlerConfiguration;
import org.bukkit.Bukkit;

/**
 * Holds the handlers plugins registered for one lifecycle event, and runs them when the
 * server fires that event.
 */
public abstract class CardboardLifecycleEventType<O extends LifecycleEventOwner, E extends LifecycleEvent, C extends LifecycleEventHandlerConfiguration<O>>
        implements LifecycleEventType<O, E, C> {

    private record RegisteredHandler<E extends LifecycleEvent>(LifecycleEventOwner owner,
                                                               LifecycleEventHandler<? super E> handler,
                                                               int priority, boolean monitor) {
    }

    // Monitors run after everyone else, and within each group the lower priority runs first.
    private static final Comparator<RegisteredHandler<?>> ORDER =
            Comparator.<RegisteredHandler<?>>comparingInt(h -> h.monitor() ? 1 : 0)
                    .thenComparingInt(RegisteredHandler::priority);

    private final String name;
    private final List<RegisteredHandler<E>> handlers = new ArrayList<>();

    protected CardboardLifecycleEventType(String name) {
        this.name = name;
        CardboardLifecycleEventRunner.track(this);
    }

    @Override
    public String name() {
        return this.name;
    }

    void register(LifecycleEventOwner owner, LifecycleEventHandler<? super E> handler, int priority, boolean monitor) {
        synchronized (this.handlers) {
            this.handlers.add(new RegisteredHandler<>(owner, handler, priority, monitor));
            this.handlers.sort(ORDER);
        }
    }

    /**
     * Drops everything an owner registered, so that disabling or reloading a plugin does not
     * leave its handlers behind to run a second time.
     */
    public void removeOwner(LifecycleEventOwner owner) {
        synchronized (this.handlers) {
            this.handlers.removeIf(h -> h.owner() == owner);
        }
    }

    /**
     * Runs every registered handler. A handler that throws is logged against its own owner and
     * does not stop the remaining handlers.
     */
    public void fire(E event) {
        List<RegisteredHandler<E>> snapshot;
        synchronized (this.handlers) {
            snapshot = List.copyOf(this.handlers);
        }

        for (RegisteredHandler<E> registered : snapshot) {
            CardboardLifecycleEventRunner.setCurrentOwner(registered.owner());
            try {
                registered.handler().run(event);
            } catch (Throwable throwable) {
                Bukkit.getLogger().log(Level.SEVERE, "Plugin " + ownerName(registered.owner())
                        + " failed handling the '" + this.name + "' lifecycle event", throwable);
            } finally {
                CardboardLifecycleEventRunner.setCurrentOwner(null);
            }
        }
    }

    private static String ownerName(LifecycleEventOwner owner) {
        try {
            return owner.getPluginMeta().getDisplayName();
        } catch (Throwable ignored) {
            return String.valueOf(owner);
        }
    }
}
