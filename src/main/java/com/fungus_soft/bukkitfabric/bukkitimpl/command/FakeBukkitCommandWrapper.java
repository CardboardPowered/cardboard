package com.fungus_soft.bukkitfabric.bukkitimpl.command;

import com.fungus_soft.bukkitfabric.bukkitimpl.FakeServer;
import com.fungus_soft.bukkitfabric.interfaces.IMixinCommandOutput;
import com.fungus_soft.bukkitfabric.interfaces.IMixinServerCommandSource;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;

import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;

public class FakeBukkitCommandWrapper implements com.mojang.brigadier.Command<ServerCommandSource>, Predicate<ServerCommandSource>, SuggestionProvider<ServerCommandSource> {

    private final FakeServer server;
    private final Command command;

    public FakeBukkitCommandWrapper(FakeServer server, Command command) {
        this.server = server;
        this.command = command;
    }

    public LiteralCommandNode<ServerCommandSource> register(CommandDispatcher<ServerCommandSource> dispatcher, String label) {
        return dispatcher.register(
                LiteralArgumentBuilder.<ServerCommandSource>literal(label).requires(this).executes(this)
                .then(RequiredArgumentBuilder.<ServerCommandSource, String>argument("args", StringArgumentType.greedyString()).suggests(this).executes(this))
        );
    }

    @Override
    public boolean test(ServerCommandSource wrapper) {
        return true;//command.testPermissionSilent(getSender(wrapper));
    }

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        try {
            return server.dispatchCommand(getSender(context.getSource()),context.getInput()) ? 1 : 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public CommandSender getSender(ServerCommandSource source) {
        CommandSender sender = Bukkit.getConsoleSender();
        try {
            ServerPlayerEntity plr = source.getPlayer();
            if (null != plr)
                return ((IMixinCommandOutput)plr).getBukkitSender(source);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Entity e = source.getEntity();
        if (null != e)
            return ((IMixinCommandOutput)e).getBukkitSender(source);

        return sender;
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        List<String> results = server.tabComplete(((IMixinServerCommandSource)(Object)context.getSource()).getBukkitSender(), builder.getInput(), context.getSource().getWorld(), context.getSource().getPosition(), true);

        // Defaults to sub nodes, but we have just one giant args node, so offset accordingly
        builder = builder.createOffset(builder.getInput().lastIndexOf(' ') + 1);

        for (String s : results) {
            builder.suggest(s);
        }

        return builder.buildFuture();
    }

}