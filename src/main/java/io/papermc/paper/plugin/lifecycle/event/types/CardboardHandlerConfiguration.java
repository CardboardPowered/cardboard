package io.papermc.paper.plugin.lifecycle.event.types;

import io.papermc.paper.plugin.lifecycle.event.LifecycleEventOwner;

/**
 * Implemented by every handler configuration Cardboard hands back to plugins, so that the
 * lifecycle event manager can attach a finished configuration to its owner.
 */
public interface CardboardHandlerConfiguration {

    void registerTo(LifecycleEventOwner owner);
}
