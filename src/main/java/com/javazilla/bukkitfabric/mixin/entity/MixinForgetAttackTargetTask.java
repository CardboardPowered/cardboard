package com.javazilla.bukkitfabric.mixin.entity;

import org.cardboardpowered.impl.entity.LivingEntityImpl;
import org.bukkit.event.entity.EntityTargetEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.javazilla.bukkitfabric.impl.BukkitEventFactory;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.ForgetAttackTargetTask;
import net.minecraft.entity.mob.MobEntity;

@Mixin(ForgetAttackTargetTask.class)
public class MixinForgetAttackTargetTask<E extends MobEntity> {

    @Inject(at = @At("HEAD"), method = "forgetAttackTarget", cancellable = true)
    public void callTargetEvent(E e0, CallbackInfo ci) {
        LivingEntity old = e0.getBrain().getOptionalMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
        EntityTargetEvent event = BukkitEventFactory.callEntityTargetLivingEvent(e0, old, (old != null && !old.isAlive()) ? EntityTargetEvent.TargetReason.TARGET_DIED : EntityTargetEvent.TargetReason.FORGOT_TARGET);
        if (event.isCancelled()) return;

        if (event.getTarget() != null) {
            e0.getBrain().remember(MemoryModuleType.ATTACK_TARGET, ((LivingEntityImpl) event.getTarget()).getHandle());
            ci.cancel();
            return;
        }
        e0.getBrain().forget(MemoryModuleType.ATTACK_TARGET);
    }

}