package com.javazilla.bukkitfabric.mixin.entity;

import org.bukkit.event.entity.HorseJumpEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.javazilla.bukkitfabric.impl.BukkitEventFactory;

import net.minecraft.entity.passive.HorseBaseEntity;

@Mixin(HorseBaseEntity.class)
public class MixinHorseBaseEntity {

    @Inject(at = @At("HEAD"), method = "startJumping", cancellable = true)
    public void callJumpEvent(int i, CallbackInfo ci) {
        float power = (i >= 90) ? 1.0F : (0.4F + 0.4F * (float) i / 90.0F);

        HorseJumpEvent event = BukkitEventFactory.callHorseJumpEvent((HorseBaseEntity)(Object)this, power);
        if (event.isCancelled()) {
            ci.cancel();
            return;
        }
    }

}