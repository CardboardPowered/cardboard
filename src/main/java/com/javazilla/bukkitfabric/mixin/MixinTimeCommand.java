/**
 * The Bukkit for Fabric Project
 * Copyright (C) 2020 Javazilla Software and contributors
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.javazilla.bukkitfabric.mixin;

import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.event.world.TimeSkipEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.javazilla.bukkitfabric.interfaces.IMixinWorld;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.TimeCommand;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.TranslatableText;

@Mixin(TimeCommand.class)
public class MixinTimeCommand {

    @Inject(at = @At("HEAD"), method = "executeSet", cancellable = true)
    private static void executeSet_Bukkit(ServerCommandSource source, int i, CallbackInfoReturnable<Integer> ci) {
        Iterator<ServerWorld> iterator = source.getMinecraftServer().getWorlds().iterator();

        while (iterator.hasNext()) {
            ServerWorld world = (ServerWorld) iterator.next();
            TimeSkipEvent event = new TimeSkipEvent(((IMixinWorld)world).getWorldImpl(), TimeSkipEvent.SkipReason.COMMAND, i - world.getTimeOfDay());
            Bukkit.getPluginManager().callEvent(event);
            if (!event.isCancelled()) world.setTimeOfDay((long) world.getTimeOfDay() + event.getSkipAmount());
        }
        source.sendFeedback(new TranslatableText("commands.time.set", new Object[]{i}), true);
        ci.setReturnValue(getDayTime(source.getWorld()));
        return;
    }

    @Inject(at = @At("HEAD"), method = "executeAdd", cancellable = true)
    private static void executeAdd_Bukkit(ServerCommandSource source, int i, CallbackInfoReturnable<Integer> ci) {
        Iterator<ServerWorld> iterator = source.getMinecraftServer().getWorlds().iterator();

        while (iterator.hasNext()) {
            ServerWorld world = (ServerWorld) iterator.next();
            TimeSkipEvent event = new TimeSkipEvent(((IMixinWorld)world).getWorldImpl(), TimeSkipEvent.SkipReason.COMMAND, i);
            Bukkit.getPluginManager().callEvent(event);
            if (!event.isCancelled()) world.setTimeOfDay(world.getTimeOfDay() + event.getSkipAmount());
        }
        int j = getDayTime(source.getWorld());
        source.sendFeedback(new TranslatableText("commands.time.set", new Object[]{j}), true);
        ci.setReturnValue(j);
        return;
    }

    @Shadow
    private static int getDayTime(ServerWorld world) {
        return (int) (world.getTimeOfDay() % 24000L);
    }

}