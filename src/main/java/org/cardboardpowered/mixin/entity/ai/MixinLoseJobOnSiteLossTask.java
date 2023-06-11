/**
 * Cardboard - The Bukkit for Fabric Project
 * Copyright (C) 2020 Cardboard contributors
 */
package org.cardboardpowered.mixin.entity.ai;

import org.bukkit.event.entity.VillagerCareerChangeEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import com.javazilla.bukkitfabric.impl.BukkitEventFactory;
import org.cardboardpowered.impl.entity.VillagerImpl;
import org.cardboardpowered.util.MixinInfo;

import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.LoseJobOnSiteLossTask;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.ai.brain.task.TaskTriggerer;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.village.VillagerData;
import net.minecraft.village.VillagerProfession;

@MixinInfo(events = {"VillagerCareerChangeEvent"})
@Mixin(value = LoseJobOnSiteLossTask.class, priority = 900)
public class MixinLoseJobOnSiteLossTask {

    /**
     * @reason Fire VillagerCareerChangeEvent
     * @author cardboard
     */
    @Overwrite
    public static Task<VillagerEntity> create() {
        return TaskTriggerer.task(context -> context.group(context.queryMemoryAbsent(MemoryModuleType.JOB_SITE)).apply(context, jobSite -> (world, entity, time) -> {
            VillagerData lv = entity.getVillagerData();
            if (lv.getProfession() != VillagerProfession.NONE && lv.getProfession() != VillagerProfession.NITWIT && entity.getExperience() == 0 && lv.getLevel() <= 1) {
                // CraftBukkit start
                VillagerCareerChangeEvent event = BukkitEventFactory.callVillagerCareerChangeEvent(entity, VillagerImpl.nmsToBukkitProfession(VillagerProfession.NONE), VillagerCareerChangeEvent.ChangeReason.LOSING_JOB);
                if (event.isCancelled()) {
                    return false;
                }
                entity.setVillagerData(entity.getVillagerData().withProfession(VillagerImpl.bukkitToNmsProfession(event.getProfession())));
                // CraftBukkit end
                entity.reinitializeBrain(world);
                return true;
            }
            return false;
        }));
    }

}