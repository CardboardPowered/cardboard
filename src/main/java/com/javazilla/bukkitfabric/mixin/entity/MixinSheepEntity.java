package com.javazilla.bukkitfabric.mixin.entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.javazilla.bukkitfabric.impl.BukkitEventFactory;
import com.javazilla.bukkitfabric.interfaces.IMixinEntity;

import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

@Mixin(SheepEntity.class)
public class MixinSheepEntity {

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/passive/SheepEntity;sheared(Lnet/minecraft/sound/SoundCategory;)V") , method = "interactMob", cancellable = true)
    public void doBukkitEvent_PlayerShearEntityEvent(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> ci) {
        ItemStack itemstack = player.getStackInHand(hand);
        if (!BukkitEventFactory.handlePlayerShearEntityEvent(player, (SheepEntity)(Object)this, itemstack, hand)) {
            ci.setReturnValue(ActionResult.PASS);
            return;
        }
    }

    @Inject(at = @At("HEAD"), method = "Lnet/minecraft/entity/passive/SheepEntity;sheared(Lnet/minecraft/sound/SoundCategory;)V")
    public void cardboardForceDrops_START(SoundCategory a, CallbackInfo ci) {
        ((IMixinEntity)(Object)this).cardboard_setForceDrops(true);
    }

    @Inject(at = @At("TAIL"), method = "Lnet/minecraft/entity/passive/SheepEntity;sheared(Lnet/minecraft/sound/SoundCategory;)V")
    public void cardboardForceDrops_END(SoundCategory a, CallbackInfo ci) {
        ((IMixinEntity)(Object)this).cardboard_setForceDrops(false);
    }

}