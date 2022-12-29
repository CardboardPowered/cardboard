package org.cardboardpowered.mixin.block;

import java.util.Random;

import org.cardboardpowered.util.MixinInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.isaiah.common.events.LeavesDecayCallback;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;

@MixinInfo(events = {"LeavesDecayEvent"})
@Mixin(LeavesBlock.class)
public class MixinLeavesBlock {
    
	// Replaced by LeavesDecayEvent in iCommonLib
	
    /*@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/block/LeavesBlock;dropStacks(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V"),
            method = "randomTick", cancellable = true)
    public void cardboard_doLeavesDecayEvent(BlockState state, ServerWorld world, BlockPos pos, Random ra, CallbackInfo ci) {        
        ActionResult result = LeavesDecayCallback.EVENT.invoker().interact(state, world, pos);
        
        if(result == ActionResult.FAIL) {
            ci.cancel();
        }
    }*/

}
