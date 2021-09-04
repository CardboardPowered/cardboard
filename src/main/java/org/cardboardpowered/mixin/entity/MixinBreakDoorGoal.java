/**
 * This file belongs to Cardboard.
 * Copyright (c) 2021 Cardboard Contributors
 */
package org.cardboardpowered.mixin.entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.javazilla.bukkitfabric.impl.BukkitEventFactory;

import net.minecraft.entity.ai.goal.BreakDoorGoal;
import net.minecraft.entity.ai.goal.DoorInteractGoal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.BlockPos;

@Mixin(BreakDoorGoal.class)
public class MixinBreakDoorGoal extends DoorInteractGoal {

    public MixinBreakDoorGoal(MobEntity mob) {
        super(mob);
    }

    /**
     * Implements EntityBreakDoorEvent
     * 
     * @see {@link BukkitEventFactory#callEntityBreakDoorEvent(Entity, BlockPos)}
     */
    @Inject(at = @At(value = "INVOKE", 
                     target = "Lnet/minecraft/world/World;removeBlock(Lnet/minecraft/util/math/BlockPos;Z)Z"), 
            method = "tick", cancellable = true)
    public void cardboard_doEntityBreakDoorEvent(CallbackInfo ci) {
        if (BukkitEventFactory.callEntityBreakDoorEvent(this.mob, this.doorPos).isCancelled()) {
            this.start();
            ci.cancel();
            return;
        }
    }

    @Shadow
    public void start() {}

}