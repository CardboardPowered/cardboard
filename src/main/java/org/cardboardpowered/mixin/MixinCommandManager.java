/**
 * CardboardPowered - Bukkit/Spigot for Fabric
 * Copyright (C) CardboardPowered.org and contributors
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either 
 * version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.cardboardpowered.mixin;

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
import com.javazilla.bukkitfabric.impl.BukkitEventFactory;
import com.javazilla.bukkitfabric.interfaces.IMixinServerEntityPlayer;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;

import net.minecraft.server.command.CommandManager;
import net.minecraft.command.CommandSource;
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
        BukkitEventFactory.callEvent(event);

        // Remove labels that were removed during the event
        //for (String orig : bukkit)
        //    if (!event.getCommands().contains(orig)) rootcommandnode.removeCommand(orig);
    }

}