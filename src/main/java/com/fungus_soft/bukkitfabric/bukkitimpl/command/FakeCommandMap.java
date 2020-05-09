package com.fungus_soft.bukkitfabric.bukkitimpl.command;

import java.util.Arrays;

import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.command.BukkitCommandWrapper;

import com.fungus_soft.bukkitfabric.bukkitimpl.command.defaults.PluginsCommand;
import com.fungus_soft.bukkitfabric.bukkitimpl.command.defaults.VersionCommand;
import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.server.command.ServerCommandSource;

public class FakeCommandMap extends SimpleCommandMap {

    private Server server;
    public FakeCommandMap(Server server) {
        super(server);
        this.server = server;

        register("plugins", "bukkit", new PluginsCommand("plugins"));
        register("version", "bukkit", new VersionCommand("version"));
        register("pl", "bukkit", new PluginsCommand("pl"));
        register("ver", "bukkit", new VersionCommand("ver"));
    }

    @Override
    public boolean register(String label, String fallbackPrefix, Command command) {
        boolean supe = super.register(label, fallbackPrefix, command);

        CommandDispatcher<ServerCommandSource> dispatcher = CraftServer.server.getCommandManager().getDispatcher();
        BukkitCommandWrapper cmd = new BukkitCommandWrapper((CraftServer)server, command);

        for (String s : command.getAliases()) {
            cmd.register(dispatcher, s);
            cmd.register(dispatcher, fallbackPrefix + ":" + s);
        }
        cmd.register(dispatcher, label);
        cmd.register(dispatcher, fallbackPrefix + ":" + label);

        this.knownCommands.put(label, command);

        return supe;
    }

    @Override
    public boolean dispatch(CommandSender sender, String commandLine) throws CommandException {
        String[] args = commandLine.split(" ");
        if (args.length == 0)
            return false;

        String sentCommandLabel = args[0].toLowerCase(java.util.Locale.ENGLISH);
        if (sentCommandLabel.startsWith("/"))
            sentCommandLabel = sentCommandLabel.substring(1);

        Command target = getCommand(sentCommandLabel);

        if (target == null)
            return false;

        try {
            // Note: we don't return the result of target.execute as thats success / failure, we return handled (true) or not handled (false)
            target.execute(sender, sentCommandLabel, Arrays.copyOfRange(args, 1, args.length));
        } catch (CommandException ex) {
            ex.printStackTrace();
            throw ex;
        } catch (Throwable ex) {
            ex.printStackTrace();
            throw new CommandException("Unhandled exception executing '" + commandLine + "' in " + target, ex);
        }

        // return true as command was handled
        return true;
    }

}