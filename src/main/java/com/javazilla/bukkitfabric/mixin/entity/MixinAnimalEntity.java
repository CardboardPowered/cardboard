/**
 * The Bukkit for Fabric Project
 * Copyright (C) 2020 Javazilla Software and contributors
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
package com.javazilla.bukkitfabric.mixin.entity;

import org.bukkit.event.entity.EntityEnterLoveModeEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.javazilla.bukkitfabric.impl.BukkitEventFactory;

import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;

@Mixin(AnimalEntity.class)
public class MixinAnimalEntity {

    @Shadow
    public int loveTicks;

    @Inject(at = @At("HEAD"), method = "lovePlayer", cancellable = true)
    public void callEnterLoveModeEvent(PlayerEntity entityhuman, CallbackInfo ci) {
        EntityEnterLoveModeEvent entityEnterLoveModeEvent = BukkitEventFactory.callEntityEnterLoveModeEvent(entityhuman, (AnimalEntity)(Object)this, 600);
        if (entityEnterLoveModeEvent.isCancelled())
            ci.cancel();
        this.loveTicks = entityEnterLoveModeEvent.getTicksInLove();
    }

}