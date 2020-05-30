package org.bukkit.craftbukkit.command;

import java.util.Map;

import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;
import com.fungus_soft.bukkitfabric.command.VersionCommand;

public class CraftCommandMap extends SimpleCommandMap {

    public CraftCommandMap(Server server) {
        super(server);

        // Register our own custom version command
        register("bukkit", new VersionCommand("version"));
        register("bukkit", new VersionCommand("ver"));
    }

    public Map<String, Command> getKnownCommands() {
        return knownCommands;
    }

}