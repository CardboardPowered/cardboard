package org.cardboardpowered.mixin.block;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.event.block.LeavesDecayEvent;
import org.cardboardpowered.util.MixinInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.javazilla.bukkitfabric.interfaces.IMixinWorld;

import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

@MixinInfo(events = {"LeavesDecayEvent"})
@Mixin(LeavesBlock.class)
public class MixinLeavesBlock {
    
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/block/LeavesBlock;dropStacks(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V"),
            method = "randomTick", cancellable = true)
    public void cardboard_doLeavesDecayEvent(BlockState state, ServerWorld world, BlockPos pos, Random r, CallbackInfo ci) {
        LeavesDecayEvent event = new LeavesDecayEvent(((IMixinWorld)world).getWorldImpl().getBlockAt(pos.getX(), pos.getY(), pos.getZ()));
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled() || world.getBlockState(pos).getBlock() != (LeavesBlock)(Object)this) {
            ci.cancel();
            return;
        }
    }

}
