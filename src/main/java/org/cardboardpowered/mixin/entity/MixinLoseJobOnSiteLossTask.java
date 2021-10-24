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
package org.cardboardpowered.mixin.entity;

import org.bukkit.event.entity.VillagerCareerChangeEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import com.javazilla.bukkitfabric.impl.BukkitEventFactory;
import org.cardboardpowered.impl.entity.VillagerImpl;

import net.minecraft.entity.ai.brain.task.LoseJobOnSiteLossTask;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.village.VillagerProfession;

@Mixin(value = LoseJobOnSiteLossTask.class, priority = 900)
public class MixinLoseJobOnSiteLossTask {

    /**
     * @reason Fire VillagerCareerChangeEvent
     * @author BukkitFabricMod
     */
    @Overwrite
    public void run(ServerWorld worldserver, VillagerEntity entityvillager, long i) {
        VillagerCareerChangeEvent event = BukkitEventFactory.callVillagerCareerChangeEvent(entityvillager, VillagerImpl.nmsToBukkitProfession(VillagerProfession.NONE), VillagerCareerChangeEvent.ChangeReason.EMPLOYED);
        if (event.isCancelled()) return;
        entityvillager.setVillagerData(entityvillager.getVillagerData().withProfession(VillagerImpl.bukkitToNmsProfession(event.getProfession())));

        entityvillager.reinitializeBrain(worldserver);
    }

}