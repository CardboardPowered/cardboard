package org.bukkit.craftbukkit.command;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.craftbukkit.CraftServer;

import com.fungus_soft.bukkitfabric.command.VersionCommand;
import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.server.command.ServerCommandSource;

public class CraftCommandMap extends SimpleCommandMap {

    private Server server;
    public CraftCommandMap(Server server) {
        super(server);
        this.server = server;

        // Register our own custom version command
        register("bukkit", new VersionCommand("version"));
        register("bukkit", new VersionCommand("ver"));
    }

    @Override
    public boolean register(String label, String fallbackPrefix, Command command) {
        boolean supe = super.register(label, fallbackPrefix, command);

        CommandDispatcher<ServerCommandSource> dispatcher = CraftServer.server.getCommandManager().getDispatcher();
        BukkitCommandWrapper cmd = new BukkitCommandWrapper((CraftServer)Bukkit.getServer(), command);
        cmd.register(dispatcher, label);
        cmd.register(dispatcher, fallbackPrefix + ":" + label);

        for (String s : command.getAliases()) {
            cmd = new BukkitCommandWrapper((CraftServer)server, command);
            cmd.register(dispatcher, s);
            cmd.register(dispatcher, fallbackPrefix + ":" + s);
            this.knownCommands.put(label, command);
        }

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