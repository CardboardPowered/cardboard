package org.cardboardpowered.impl.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandRegistrationFlag;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.configuration.PluginMeta;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventOwner;
import io.papermc.paper.plugin.lifecycle.event.types.CardboardLifecycleEventRunner;

import org.bukkit.craftbukkit.CraftServer;

/**
 * Cardboard's {@link Commands} registrar.
 *
 * <p>Plugins build their nodes against the Paper {@link CommandSourceStack}, which the vanilla
 * source stack implements, so a node can be handed straight to the vanilla dispatcher. Every
 * command is also registered into the Bukkit command map, which is what gives it a namespaced
 * alias, help entry and tab completion, and what lets {@code CraftServer.syncCommands()} graft it
 * back into the dispatcher it rebuilds afterwards.
 */
public final class CardboardCommands implements Commands {

    private final net.minecraft.commands.Commands vanilla;

    public CardboardCommands(net.minecraft.commands.Commands vanilla) {
        this.vanilla = vanilla;
    }

    @SuppressWarnings("unchecked")
    @Override
    public CommandDispatcher<CommandSourceStack> getDispatcher() {
        return (CommandDispatcher<CommandSourceStack>) (CommandDispatcher<?>) this.vanilla.getDispatcher();
    }

    @Override
    public Set<String> register(LiteralCommandNode<CommandSourceStack> node, String description, Collection<String> aliases) {
        return this.register(callingPlugin(), node, description, aliases);
    }

    @Override
    public Set<String> register(PluginMeta meta, LiteralCommandNode<CommandSourceStack> node, String description, Collection<String> aliases) {
        return this.registerWithFlags(meta, node, description, aliases, Set.of());
    }

    @SuppressWarnings("unchecked")
    @Override
    public Set<String> registerWithFlags(PluginMeta meta, LiteralCommandNode<CommandSourceStack> node, String description,
                                         Collection<String> aliases, Set<CommandRegistrationFlag> flags) {
        LiteralCommandNode<net.minecraft.commands.CommandSourceStack> vanillaNode =
                (LiteralCommandNode<net.minecraft.commands.CommandSourceStack>) (LiteralCommandNode<?>) node;

        List<String> aliasList = (aliases == null) ? List.of() : List.copyOf(aliases);
        String namespace = meta.getName().toLowerCase(Locale.ROOT);

        // Into the live dispatcher first, so the command works before the command map is next synced.
        this.vanilla.getDispatcher().getRoot().addChild(vanillaNode);
        for (String alias : aliasList)
            this.vanilla.getDispatcher().getRoot().addChild(cloneAs(alias, vanillaNode));

        MinecraftCommandWrapper wrapper = new MinecraftCommandWrapper(this.vanilla, vanillaNode);
        // The node's own requires() already gates the command; a "minecraft.command.*" permission
        // node would only lock plugin commands away from everyone.
        wrapper.setPermission(null);
        if (description != null && !description.isEmpty()) wrapper.setDescription(description);
        if (!aliasList.isEmpty()) wrapper.setAliases(aliasList);

        String label = vanillaNode.getLiteral();
        CraftServer.INSTANCE.getCommandMap().register(label, namespace, wrapper);

        Set<String> registered = new LinkedHashSet<>();
        registered.add(label);
        registered.add(namespace + ":" + label);
        for (String alias : aliasList) {
            registered.add(alias);
            registered.add(namespace + ":" + alias);
        }
        return registered;
    }

    @Override
    public Set<String> register(String label, String description, Collection<String> aliases, BasicCommand basicCommand) {
        return this.register(callingPlugin(), label, description, aliases, basicCommand);
    }

    @Override
    public Set<String> register(PluginMeta meta, String label, String description, Collection<String> aliases, BasicCommand basicCommand) {
        return this.register(meta, toNode(label, basicCommand), description, aliases);
    }

    private static LiteralCommandNode<CommandSourceStack> toNode(String label, BasicCommand basicCommand) {
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal(label)
                .requires(source -> basicCommand.canUse(source.getSender()))
                .executes(context -> {
                    basicCommand.execute(context.getSource(), new String[0]);
                    return com.mojang.brigadier.Command.SINGLE_SUCCESS;
                });

        builder.then(Commands.argument("arguments", StringArgumentType.greedyString())
                .suggests((context, suggestions) -> suggest(basicCommand, context, suggestions))
                .executes(context -> {
                    basicCommand.execute(context.getSource(), splitArguments(StringArgumentType.getString(context, "arguments")));
                    return com.mojang.brigadier.Command.SINGLE_SUCCESS;
                }));

        return builder.build();
    }

    private static CompletableFuture<Suggestions> suggest(BasicCommand basicCommand, CommandContext<CommandSourceStack> context,
                                                          SuggestionsBuilder suggestions) {
        String remaining = suggestions.getRemaining();
        // Brigadier hands us the whole greedy argument, but BasicCommand suggests for the word
        // being typed, so only the trailing word is what the suggestions replace.
        String[] arguments = splitArguments(remaining);
        if (remaining.isEmpty() || remaining.endsWith(" ")) {
            String[] withEmpty = new String[arguments.length + 1];
            System.arraycopy(arguments, 0, withEmpty, 0, arguments.length);
            withEmpty[arguments.length] = "";
            arguments = withEmpty;
        }

        int lastSpace = remaining.lastIndexOf(' ');
        SuggestionsBuilder offset = suggestions.createOffset(suggestions.getStart() + lastSpace + 1);
        for (String suggestion : basicCommand.suggest(context.getSource(), arguments))
            offset.suggest(suggestion);

        return offset.buildFuture();
    }

    private static String[] splitArguments(String arguments) {
        if (arguments.isEmpty()) return new String[0];

        List<String> split = new ArrayList<>();
        for (String part : arguments.split(" "))
            if (!part.isEmpty()) split.add(part);

        return split.toArray(new String[0]);
    }

    private static LiteralCommandNode<net.minecraft.commands.CommandSourceStack> cloneAs(String label,
            LiteralCommandNode<net.minecraft.commands.CommandSourceStack> node) {
        LiteralCommandNode<net.minecraft.commands.CommandSourceStack> clone = new LiteralCommandNode<>(
                label, node.getCommand(), node.getRequirement(), node.getRedirect(), node.getRedirectModifier(), node.isFork());

        for (com.mojang.brigadier.tree.CommandNode<net.minecraft.commands.CommandSourceStack> child : node.getChildren())
            clone.addChild(child);

        return clone;
    }

    private static PluginMeta callingPlugin() {
        LifecycleEventOwner owner = CardboardLifecycleEventRunner.currentOwner();
        if (owner == null)
            throw new IllegalStateException("Commands can only be registered from inside a LifecycleEvents.COMMANDS handler,"
                    + " or by passing the owning plugin's PluginMeta explicitly");

        return owner.getPluginMeta();
    }
}
