package org.cardboardpowered.mixin.item;

import com.javazilla.bukkitfabric.interfaces.IMixinLivingEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MilkBucketItem;
import net.minecraft.world.World;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = MilkBucketItem.class)
public class MixinMilkBucketItem {

    @Inject(method = "finishUsing", at = @At(value = "INVOKE", remap = false, target = "Lnet/minecraft/entity/LivingEntity;clearStatusEffects()Z"))
    public void bukkitClearReason(ItemStack stack, World world, LivingEntity user, CallbackInfoReturnable<ItemStack> cir) {
        ((IMixinLivingEntity) user).pushEffectCause(EntityPotionEffectEvent.Cause.MILK);
    }
}
