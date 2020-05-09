package org.bukkit.craftbukkit.command;

import com.google.common.base.Joiner;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;

public final class VanillaCommandWrapper extends BukkitCommand {

    private final CommandDispatcher dispatcher;
    public final CommandNode vanillaCommand;

    public VanillaCommandWrapper(CommandDispatcher dispatcher, CommandNode vanillaCommand) {
        super(vanillaCommand.getName(), "A Mojang provided command.", vanillaCommand.getUsageText(), Collections.emptyList());
        this.dispatcher = dispatcher;
        this.vanillaCommand = vanillaCommand;
        this.setPermission(getPermission(vanillaCommand));
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!testPermission(sender)) return true;

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args, Location location) throws IllegalArgumentException {
        List<String> results = new ArrayList<>();
        return results;
    }

    public static String getPermission(CommandNode vanillaCommand) {
        return "minecraft.command." + ((vanillaCommand.getRedirect() == null) ? vanillaCommand.getName() : vanillaCommand.getRedirect().getName());
    }

    private String toDispatcher(String[] args, String name) {
        return name + ((args.length > 0) ? " " + Joiner.on(' ').join(args) : "");
    }
}