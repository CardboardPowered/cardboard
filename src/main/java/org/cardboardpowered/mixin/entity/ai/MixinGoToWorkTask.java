package org.cardboardpowered.mixin.entity.ai;

import net.minecraft.village.VillagerData;
import org.bukkit.event.entity.VillagerCareerChangeEvent;
import org.cardboardpowered.impl.entity.VillagerImpl;
import org.cardboardpowered.util.MixinInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import com.javazilla.bukkitfabric.impl.BukkitEventFactory;

import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.GoToWorkTask;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.village.VillagerProfession;

import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@MixinInfo(events = {"VillagerCareerChangeEvent"})
@Mixin(value = GoToWorkTask.class, priority = 999)
public class MixinGoToWorkTask {

    @Redirect(method = "method_46891", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/passive/VillagerEntity;setVillagerData(Lnet/minecraft/village/VillagerData;)V"))
    private static void banner$cancelJob(VillagerEntity instance, VillagerData villagerData) {}

    @Inject(method = "method_46891", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/passive/VillagerEntity;setVillagerData(Lnet/minecraft/village/VillagerData;)V"), cancellable = true)
    private static void banner$jobChange(VillagerEntity villagerEntity, ServerWorld serverLevel, VillagerProfession profession, CallbackInfo ci) {
        // CraftBukkit start - Fire VillagerCareerChangeEvent where Villager gets employed
        VillagerCareerChangeEvent event = BukkitEventFactory.callVillagerCareerChangeEvent(villagerEntity, VillagerImpl.nmsToBukkitProfession(profession), VillagerCareerChangeEvent.ChangeReason.EMPLOYED);
        if (event.isCancelled()) {
            ci.cancel();
        }

        villagerEntity.setVillagerData(villagerEntity.getVillagerData().withProfession(VillagerImpl.bukkitToNmsProfession(event.getProfession())));
        // CraftBukkit end
    }
    
}