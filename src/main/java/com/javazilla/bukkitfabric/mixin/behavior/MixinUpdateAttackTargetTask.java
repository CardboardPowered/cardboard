package com.javazilla.bukkitfabric.mixin.behavior;

import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.event.entity.EntityTargetEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.javazilla.bukkitfabric.impl.BukkitEventFactory;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.task.UpdateAttackTargetTask;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.network.ServerPlayerEntity;

@Mixin(UpdateAttackTargetTask.class)
public class MixinUpdateAttackTargetTask<E extends MobEntity> {

    @Inject(at = @At("HEAD"), method = "updateAttackTarget", cancellable = true)
    public void callTargetEvent(E e0, LivingEntity entityliving, CallbackInfo ci) {
        EntityTargetEvent event = BukkitEventFactory.callEntityTargetLivingEvent(e0, entityliving, (entityliving instanceof ServerPlayerEntity) ? EntityTargetEvent.TargetReason.CLOSEST_PLAYER : EntityTargetEvent.TargetReason.CLOSEST_ENTITY);
        if (event.isCancelled()) {
            ci.cancel();
            return;
        }
        entityliving = (event.getTarget() != null) ? ((CraftLivingEntity) event.getTarget()).getHandle() : null;
    }

}