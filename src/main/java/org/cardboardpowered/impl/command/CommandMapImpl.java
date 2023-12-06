package org.cardboardpowered.impl.command;

import java.util.Map;

import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.command.defaults.PluginsCommand;

public class CommandMapImpl extends SimpleCommandMap {

    public CommandMapImpl(Server server) {
        super(server);

        // Register our commands
        for (String s : new String[] {"version", "ver", "about"})
            register("bukkit", new VersionCommand(s));
        for (String s : new String[] {"fabricmods"})
            register("cardboard", new ModsCommand(s));
        
        setDefaultCommands();
    }
    
    private void setDefaultCommands() {
        this.register("bukkit", new VersionCommand("version"));
        this.register("bukkit", new PluginsCommand("plugins"));
    }

    public Map<String, Command> getKnownCommands() {
        return knownCommands;
    }

}