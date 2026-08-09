package org.cardboardpowered.impl.command;

import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.registrar.ReloadableRegistrarEvent;

/**
 * The event handed to {@code LifecycleEvents.COMMANDS} handlers.
 */
public record CardboardCommandsEvent(Commands registrar, ReloadableRegistrarEvent.Cause cause)
        implements ReloadableRegistrarEvent<Commands> {
}
