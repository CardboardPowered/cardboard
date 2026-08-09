package org.cardboardpowered.impl.command;

import com.google.common.base.Joiner;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.tree.CommandNode;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerLevel;

public final class MinecraftCommandWrapper extends BukkitCommand {

    private final Commands dispatcher;
    public final CommandNode<?> vanillaCommand;

    public MinecraftCommandWrapper(Commands dispatcher, CommandNode<?> vanillaCommand) {
        super(vanillaCommand.getName(), "A Minecraft provided command", vanillaCommand.getUsageText(), Collections.emptyList());
        this.dispatcher = dispatcher;
        this.vanillaCommand = vanillaCommand;
        this.setPermission(getPermission(vanillaCommand));
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!testPermission(sender)) return true;

        // Lnet/minecraft/server/command/CommandManager;parseAndExecute  (Lnet/minecraft/server/command/ServerCommandSource;Ljava/lang/String;)V
        // Lnet/minecraft/server/command/CommandManager;executeWithPrefix(Lnet/minecraft/server/command/ServerCommandSource;Ljava/lang/String;)V
        
        CommandSourceStack icommandlistener = MinecraftCommandWrapper.getCommandSource(sender);
        this.dispatcher.performPrefixedCommand(icommandlistener, this.toDispatcher(args, this.getName()));
        
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args, Location location) throws IllegalArgumentException {
        CommandSourceStack icommandlistener = getCommandSource(sender);
        ParseResults<CommandSourceStack> parsed = dispatcher.getDispatcher().parse(toDispatcher(args, getName()), icommandlistener);

        List<String> results = new ArrayList<>();
        dispatcher.getDispatcher().getCompletionSuggestions(parsed).thenAccept(suggestions -> suggestions.getList().forEach(s -> results.add(s.getText())));
        return results;
    }

    public static String getPermission(CommandNode<?> vanillaCommand) {
        return "minecraft.command." + ((vanillaCommand.getRedirect() == null) ? vanillaCommand.getName() : vanillaCommand.getRedirect().getName());
    }

    private String toDispatcher(String[] args, String name) {
        return name + ((args.length > 0) ? " " + Joiner.on(' ').join(args) : "");
    }

    public static CommandSourceStack getCommandSource(CommandSender s) {
        if (s instanceof CraftPlayer)
            return ((CraftPlayer)s).getHandle().createCommandSourceStack();
        if (s instanceof CraftEntity) {
            // getWorld() is the Bukkit world, which is not a ServerLevel - take it off the handle.
            net.minecraft.world.entity.Entity handle = ((CraftEntity) s).getHandle();
            return handle.createCommandSourceStackForNameResolution((ServerLevel) handle.level());
        }
        if (s instanceof ConsoleCommandSender)
            return ((CraftServer) s.getServer()).getServer().createCommandSourceStack();

        return null;
    }

}
