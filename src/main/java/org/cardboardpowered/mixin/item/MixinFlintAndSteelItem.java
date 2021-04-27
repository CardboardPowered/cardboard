package org.cardboardpowered.mixin.item;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.javazilla.bukkitfabric.impl.BukkitEventFactory;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FlintAndSteelItem;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(FlintAndSteelItem.class)
public class MixinFlintAndSteelItem {

    @Inject(method = "useOnBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/AbstractFireBlock;getState(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;"), cancellable = true)
    public void useOnBlock_BF(ItemUsageContext context, CallbackInfoReturnable<ActionResult> ci) {
        PlayerEntity plr = context.getPlayer();
        World world = context.getWorld();
        BlockPos blockposition = context.getBlockPos().offset(context.getSide());

        if (BukkitEventFactory.callBlockIgniteEvent(world, blockposition, org.bukkit.event.block.BlockIgniteEvent.IgniteCause.FLINT_AND_STEEL, plr).isCancelled()) {
            context.getStack().damage(1, plr, (plr1) -> plr1.sendToolBreakStatus(context.getHand()));
            ci.setReturnValue(ActionResult.PASS);
            return;
        }
    }

}