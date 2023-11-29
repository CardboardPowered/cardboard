package org.cardboardpowered.impl.command;

import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class CommandMapImpl extends SimpleCommandMap {
	private static final Class<?> CARDBOARD_VERSION_COMMAND = VersionCommand.class;

	public CommandMapImpl(Server server) {
		super(server);
		registerCardboardCommands();
	}

	@Override
	public boolean register(@NotNull String label, @NotNull String fallbackPrefix, @NotNull Command command) {
		if(label.equals("version") && fallbackPrefix.equals("bukkit")
				&& !CARDBOARD_VERSION_COMMAND.isInstance(command)) {
			// Let Cardboard version command take priority
			return false;
		}

		return super.register(label, fallbackPrefix, command);
	}

	@Override
	public synchronized void clearCommands() {
		super.clearCommands();
		registerCardboardCommands();
	}

	@Override
	public Map<String, Command> getKnownCommands() {
		return knownCommands;
	}

	private void registerCardboardCommands() {
		register("bukkit", new VersionCommand("version"));
		register("cardboard", new ModsCommand("fabricmods"));
	}

}
