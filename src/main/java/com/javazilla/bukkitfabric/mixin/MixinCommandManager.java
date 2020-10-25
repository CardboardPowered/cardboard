package com.javazilla.bukkitfabric.mixin;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.common.collect.Maps;
import com.javazilla.bukkitfabric.interfaces.IMixinServerEntityPlayer;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;

import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

@Mixin(CommandManager.class)
public class MixinCommandManager {

    @Shadow
    public com.mojang.brigadier.CommandDispatcher<ServerCommandSource> dispatcher;

    @Shadow
    public void makeTreeForSource(CommandNode<ServerCommandSource>a, CommandNode<CommandSource> b, ServerCommandSource c, Map<CommandNode<ServerCommandSource>, CommandNode<CommandSource>> map) {
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Inject(at = @At("HEAD"), method = "sendCommandTree")
    public void bukkitize(ServerPlayerEntity entityplayer, CallbackInfo ci) {
        //if ( SpigotConfig.tabComplete < 0 ) return; // Spigot

        Map<CommandNode<ServerCommandSource>, CommandNode<CommandSource>> map = Maps.newIdentityHashMap();
        RootCommandNode vanillaRoot = new RootCommandNode();

        RootCommandNode<ServerCommandSource> vanilla = entityplayer.server.getCommandManager().getDispatcher().getRoot();
        map.put(vanilla, vanillaRoot);
        this.makeTreeForSource(vanilla, vanillaRoot, entityplayer.getCommandSource(), (Map) map);

        RootCommandNode<CommandSource> rootcommandnode = new RootCommandNode();

        map.put(this.dispatcher.getRoot(), rootcommandnode);
        this.makeTreeForSource(this.dispatcher.getRoot(), rootcommandnode, entityplayer.getCommandSource(), (Map) map);

        Collection<String> bukkit = new LinkedHashSet<>();
        for (CommandNode node : rootcommandnode.getChildren())
            bukkit.add(node.getName());

        PlayerCommandSendEvent event = new PlayerCommandSendEvent((Player) ((IMixinServerEntityPlayer)entityplayer).getBukkitEntity(), new LinkedHashSet<>(bukkit));
        event.getPlayer().getServer().getPluginManager().callEvent(event);

        // Remove labels that were removed during the event
        //for (String orig : bukkit)
        //    if (!event.getCommands().contains(orig))
        //        rootcommandnode.removeCommand(orig);
    }

}