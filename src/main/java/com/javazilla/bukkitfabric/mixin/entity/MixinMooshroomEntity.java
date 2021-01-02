package com.javazilla.bukkitfabric.mixin.entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.javazilla.bukkitfabric.impl.BukkitEventFactory;

import net.minecraft.entity.passive.MooshroomEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

@Mixin(MooshroomEntity.class)
public class MixinMooshroomEntity {

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/passive/MooshroomEntity;sheared(Lnet/minecraft/sound/SoundCategory;)V"), method = "interactMob", cancellable = true)
    public void doBukkitEvent_PlayerShearEntityEvent(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> ci) {
        ItemStack itemstack = player.getStackInHand(hand);
        if (!BukkitEventFactory.handlePlayerShearEntityEvent(player, (SheepEntity)(Object)this, itemstack, hand)) {
            ci.setReturnValue(ActionResult.PASS);
            return;
        }
    }

}