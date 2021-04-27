package org.cardboardpowered.mixin.item;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.javazilla.bukkitfabric.impl.BukkitEventFactory;

import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.item.FireChargeItem;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(FireChargeItem.class)
public class MixinFirechargeItem {

    @Inject(method = "useOnBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/FireChargeItem;playUseSound(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V"))
    public void useOnBlock_BF1(ItemUsageContext context, CallbackInfoReturnable<ActionResult> ci) {
        World world = context.getWorld();
        BlockPos blockpos = context.getBlockPos();
        BlockState state = world.getBlockState(blockpos);

        if (!CampfireBlock.method_30035(state))
            blockpos = blockpos.offset(context.getSide());

        if (BukkitEventFactory.callBlockIgniteEvent(world, blockpos, org.bukkit.event.block.BlockIgniteEvent.IgniteCause.FIREBALL, context.getPlayer()).isCancelled()) {
            if (!context.getPlayer().abilities.creativeMode)
                context.getStack().decrement(1);
            ci.setReturnValue(ActionResult.PASS);
        }
    }

}