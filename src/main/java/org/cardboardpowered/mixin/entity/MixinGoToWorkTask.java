package org.cardboardpowered.mixin.entity;

import java.util.Optional;

import org.bukkit.event.entity.VillagerCareerChangeEvent;
import org.cardboardpowered.impl.entity.VillagerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import com.javazilla.bukkitfabric.impl.BukkitEventFactory;

import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.GoToWorkTask;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.dynamic.GlobalPos;
import net.minecraft.village.VillagerProfession;

import net.minecraft.util.registry.Registry;

@Mixin(GoToWorkTask.class)
public class MixinGoToWorkTask {

    @Overwrite
    public void run(ServerWorld worldserver, VillagerEntity entityvillager, long i) {
        GlobalPos globalpos = (GlobalPos) entityvillager.getBrain().getOptionalMemory(MemoryModuleType.POTENTIAL_JOB_SITE).get();

        entityvillager.getBrain().forget(MemoryModuleType.POTENTIAL_JOB_SITE);
        entityvillager.getBrain().remember(MemoryModuleType.JOB_SITE, globalpos); // CraftBukkit - decompile error
        worldserver.sendEntityStatus(entityvillager, (byte) 14);
        if (entityvillager.getVillagerData().getProfession() == VillagerProfession.NONE) {
            MinecraftServer minecraftserver = worldserver.getServer();

            Optional.ofNullable(minecraftserver.getWorld(globalpos.getDimension())).flatMap((worldserver1) -> {
                return worldserver1.getPointOfInterestStorage().getType(globalpos.getPos());
            }).flatMap((villageplacetype) -> {
                return Registry.VILLAGER_PROFESSION.stream().filter((villagerprofession) -> {
                    return villagerprofession.getWorkStation() == villageplacetype;
                }).findFirst();
            }).ifPresent((villagerprofession) -> {
                // Fire VillagerCareerChangeEvent where Villager gets employed
                VillagerCareerChangeEvent event = BukkitEventFactory.callVillagerCareerChangeEvent(entityvillager, VillagerImpl.nmsToBukkitProfession(villagerprofession), VillagerCareerChangeEvent.ChangeReason.EMPLOYED);
                if (event.isCancelled()) return;

                entityvillager.setVillagerData(entityvillager.getVillagerData().withProfession(VillagerImpl.bukkitToNmsProfession(event.getProfession())));
                entityvillager.reinitializeBrain(worldserver);
            });
        }
    }

}