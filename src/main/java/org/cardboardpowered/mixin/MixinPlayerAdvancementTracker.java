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

import net.minecraft.util.Identifier;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.cardboardpowered.util.MixinInfo;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.javazilla.bukkitfabric.interfaces.IMixinAdvancement;
import com.javazilla.bukkitfabric.interfaces.IMixinEntity;

import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Map;

@MixinInfo(events = {"PlayerAdvancementDoneEvent"})
@Mixin(PlayerAdvancementTracker.class)
public class MixinPlayerAdvancementTracker {

    @Shadow
    public ServerPlayerEntity owner;

    @Shadow @Final private static Logger LOGGER;

    @Redirect(method = "load", at = @At (value = "INVOKE",
            target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V",
            remap = false))
    public void ifModdedThing(Logger logger, String message, Object p0, Object p1) {
        //if ("minecraft".equals(((Map.Entry<Identifier, ?>) p0).getKey().getNamespace())) {
        //    LOGGER.warn("Ignored advancement '{}' in progress file {} - it doesn't exist anymore?", p0, p1);
        //}
    }

    @SuppressWarnings("rawtypes")
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/advancement/AdvancementRewards;apply(Lnet/minecraft/server/network/ServerPlayerEntity;)V"), method = "grantCriterion")
    public void fireBukkitEvent(Advancement advancement, String s, CallbackInfoReturnable ci) {
        Bukkit.getServer().getPluginManager().callEvent(new org.bukkit.event.player.PlayerAdvancementDoneEvent((Player) ((IMixinEntity)this.owner).getBukkitEntity(), ((IMixinAdvancement)advancement).getBukkitAdvancement())); // Bukkit
    }

}