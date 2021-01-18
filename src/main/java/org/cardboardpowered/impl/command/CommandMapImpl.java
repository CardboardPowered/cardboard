package org.cardboardpowered.impl.command;

import java.util.Map;

import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;

public class CommandMapImpl extends SimpleCommandMap {

    public CommandMapImpl(Server server) {
        super(server);

        // Register our commands
        for (String s : new String[] {"version", "ver", "about"})
            register("bukkit", new VersionCommand(s));
        for (String s : new String[] {"fabricmods", "mods"})
            register("cardboard", new ModsCommand(s));
    }

    public Map<String, Command> getKnownCommands() {
        return knownCommands;
    }

}