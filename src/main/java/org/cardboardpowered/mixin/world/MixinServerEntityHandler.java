/**
 * This file is a part of Cardboard & iCommonLib
 * Copyright (c) 2020-2021 by Isaiah
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
package org.cardboardpowered.mixin.world;

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent;
import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import com.javazilla.bukkitfabric.impl.BukkitEventFactory;
import com.javazilla.bukkitfabric.interfaces.IMixinEntity;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld.ServerEntityHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerEntityHandler.class)
public class MixinServerEntityHandler {

    @Inject(at = @At("TAIL"), method = "stopTracking(Lnet/minecraft/entity/Entity;)V")
    public void unvalidateEntityBF(Entity entity, CallbackInfo ci) {
        IMixinEntity bf = (IMixinEntity) entity;
        bf.setValid(false);
        BukkitEventFactory.callEvent( new EntityRemoveFromWorldEvent(bf.getBukkitEntity()) );
    }

    @Inject(at = @At("TAIL"), method = "startTicking(Lnet/minecraft/entity/Entity;)V")
    public void validateEntityBF(Entity entity, CallbackInfo ci) {
        IMixinEntity bf = (IMixinEntity) entity;
        bf.setValid(true);
        if (null == bf.getOriginBF() && bf.getBukkitEntity() != null)
            bf.setOriginBF(bf.getBukkitEntity().getLocation()); // Paper Entity Origin API

        BukkitEventFactory.callEvent( new EntityAddToWorldEvent(bf.getBukkitEntity()) );
    } 

}
