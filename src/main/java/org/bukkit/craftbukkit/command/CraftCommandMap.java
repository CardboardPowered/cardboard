package org.bukkit.craftbukkit.command;

import java.util.Map;

import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;

import com.javazilla.bukkitfabric.command.ModListCommand;
//import com.javazilla.bukkitfabric.command.PluginsCommand;
import com.javazilla.bukkitfabric.command.VersionCommand;

public class CraftCommandMap extends SimpleCommandMap {

    public CraftCommandMap(Server server) {
        super(server);

        // Register our commands
        for (String s : new String[] {"version", "ver", "about"})
            register("bukkit", new VersionCommand(s));
        register("bukkit", new ModListCommand("fabricmods"));
    }

    public Map<String, Command> getKnownCommands() {
        return knownCommands;
    }

}