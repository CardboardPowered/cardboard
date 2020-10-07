package com.javazilla.bukkitfabric.mixin.entity;

import org.bukkit.event.entity.EntityEnterLoveModeEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.javazilla.bukkitfabric.impl.BukkitEventFactory;

import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;

@Mixin(AnimalEntity.class)
public class MixinAnimalEntity {

    @Shadow
    public int loveTicks;

    @Inject(at = @At("HEAD"), method = "lovePlayer", cancellable = true)
    public void callEnterLoveModeEvent(PlayerEntity entityhuman, CallbackInfo ci) {
        EntityEnterLoveModeEvent entityEnterLoveModeEvent = BukkitEventFactory.callEntityEnterLoveModeEvent(entityhuman, (AnimalEntity)(Object)this, 600);
        if (entityEnterLoveModeEvent.isCancelled())
            ci.cancel();
        this.loveTicks = entityEnterLoveModeEvent.getTicksInLove();
    }

}