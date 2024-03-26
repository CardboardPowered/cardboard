package org.cardboardpowered.impl.command;

import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;
//<<<<<<< HEAD
import org.jetbrains.annotations.NotNull;

import java.util.Map;
//=======
import org.bukkit.command.defaults.PluginsCommand;
///>>>>>>> upstream/ver/1.20

public class CommandMapImpl extends SimpleCommandMap {
	private static final Class<?> CARDBOARD_VERSION_COMMAND = VersionCommand.class;

	public CommandMapImpl(Server server) {
		super(server);
		registerCardboardCommands();
	}

//<<<<<<< HEAD
	@Override
	public boolean register(@NotNull String label, @NotNull String fallbackPrefix, @NotNull Command command) {
		if(label.equals("version") && fallbackPrefix.equals("bukkit")
				&& !CARDBOARD_VERSION_COMMAND.isInstance(command)) {
			// Let Cardboard version command take priority
			return false;
		}
//=======
        // Register our commands
        for (String s : new String[] {"version", "ver", "about"})
            register("bukkit", new VersionCommand(s));
        for (String s : new String[] {"fabricmods"})
            register("cardboard", new ModsCommand(s));
        
        setDefaultCommands();	
		return super.register(label, fallbackPrefix, command);
	}
//    }
    
    private void setDefaultCommands() {
        this.register("bukkit", new VersionCommand("version"));
        this.register("bukkit", new PluginsCommand("plugins"));
    }
//>>>>>>> upstream/ver/1.20



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
