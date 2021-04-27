package com.javazilla.bukkitfabric.mixin.block;

import org.cardboardpowered.impl.block.DispenserBlockHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.javazilla.bukkitfabric.interfaces.IMixinDispenserBlock;

import net.minecraft.block.DispenserBlock;

@Mixin(DispenserBlock.class)
public class MixinDispenserBlock implements IMixinDispenserBlock {

    /**
     * @author Cardboard
     * @reason Set event fired to false
     */
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/block/DispenserBlock;getBehaviorForItem(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/block/dispenser/DispenserBehavior;"), method = "Lnet/minecraft/block/DispenserBlock;dispense(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/BlockPos;)V")
    public void doBukkit_setEventFired(CallbackInfo ci) {
        DispenserBlockHelper.eventFired = false;
    }

}
