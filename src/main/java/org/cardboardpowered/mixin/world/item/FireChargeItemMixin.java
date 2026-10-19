package org.cardboardpowered.mixin.world.item;

import org.cardboardpowered.util.MixinInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.FireChargeItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.craftbukkit.event.CraftEventFactory;

@MixinInfo(events = {"BlockIgniteEvent"})
@Mixin(FireChargeItem.class)
public class FireChargeItemMixin {

    @Inject(method = "useOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/FireChargeItem;playSound(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)V"))
    public void cardboard$fireChargeItem_BlockIgniteEvent(UseOnContext context, CallbackInfoReturnable<InteractionResult> ci) {
        Level world = context.getLevel();
        // Client prediction: vanilla only; the server fires BlockIgniteEvent.
        if (world.isClientSide()) return;
        BlockPos blockpos = context.getClickedPos();
        BlockState state = world.getBlockState(blockpos);

        if (!CampfireBlock.canLight(state))
            blockpos = blockpos.relative(context.getClickedFace());

        if (CraftEventFactory.callBlockIgniteEvent(world, blockpos, org.bukkit.event.block.BlockIgniteEvent.IgniteCause.FIREBALL, context.getPlayer()).isCancelled()) {
            if (!context.getPlayer().abilities.instabuild)
                context.getItemInHand().shrink(1);
            ci.setReturnValue(InteractionResult.PASS);
        }
    }

}