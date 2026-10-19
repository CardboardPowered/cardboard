package org.cardboardpowered.mixin.world.item;

import org.cardboardpowered.util.MixinInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import org.bukkit.craftbukkit.event.CraftEventFactory;

import me.isaiah.common.cmixin.IMixinItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.FlintAndSteelItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

@MixinInfo(events = {"BlockIgniteEvent"})
@Mixin(FlintAndSteelItem.class)
public class FlintAndSteelItemMixin {

    @Inject(method = "useOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/BaseFireBlock;getState(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"), cancellable = true)
    public void useOnBlock_BF(UseOnContext context, CallbackInfoReturnable<InteractionResult> ci) {
        Player plr = context.getPlayer();
        Level world = context.getLevel();
        // Client prediction: vanilla only; the server fires BlockIgniteEvent.
        if (world.isClientSide()) return;
        BlockPos blockposition = context.getClickedPos().relative(context.getClickedFace());

        if (CraftEventFactory.callBlockIgniteEvent(world, blockposition, org.bukkit.event.block.BlockIgniteEvent.IgniteCause.FLINT_AND_STEEL, plr).isCancelled()) {
            ((IMixinItemStack)context.getItemInHand()).IC$damage(1, plr, context.getHand());
            
            // context.getStack().damage(1, plr, (plr1) -> plr1.sendToolBreakStatus(context.getHand()));
            // context.getStack().damage(1, plr, LivingEntity.getSlotForHand(context.getHand()));
            
            ci.setReturnValue(InteractionResult.PASS);
            return;
        }
    }

}