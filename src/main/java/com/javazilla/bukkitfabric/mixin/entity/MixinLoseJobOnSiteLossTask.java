package com.javazilla.bukkitfabric.mixin.entity;

import org.bukkit.craftbukkit.entity.CraftVillager;
import org.bukkit.event.entity.VillagerCareerChangeEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import com.javazilla.bukkitfabric.impl.BukkitEventFactory;

import net.minecraft.entity.ai.brain.task.LoseJobOnSiteLossTask;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.village.VillagerProfession;

@Mixin(LoseJobOnSiteLossTask.class)
public class MixinLoseJobOnSiteLossTask {

    /**
     * @reason Fire VillagerCareerChangeEvent
     * @author BukkitFabricMod
     */
    @Overwrite
    public void run(ServerWorld worldserver, VillagerEntity entityvillager, long i) {
        VillagerCareerChangeEvent event = BukkitEventFactory.callVillagerCareerChangeEvent(entityvillager, CraftVillager.nmsToBukkitProfession(VillagerProfession.NONE), VillagerCareerChangeEvent.ChangeReason.EMPLOYED);
        if (event.isCancelled()) return;
        entityvillager.setVillagerData(entityvillager.getVillagerData().withProfession(CraftVillager.bukkitToNmsProfession(event.getProfession())));

        entityvillager.reinitializeBrain(worldserver);
    }

}