package org.cardboardpowered.mixin.block;

import com.javazilla.bukkitfabric.impl.BukkitEventFactory;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CoralFanBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

import org.cardboardpowered.util.MixinInfo;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
// import java.util.Random;

@MixinInfo(events = {"BlockFadeEvent"})
@Mixin(CoralFanBlock.class)
public class MixinCoralFanBlock {

    @Shadow @Final private Block deadCoralBlock;

    @Inject (method = "scheduledTick", at = @At (value = "INVOKE",
            target = "Lnet/minecraft/server/world/ServerWorld;setBlockState" +
                    "(Lnet/minecraft/util/math/BlockPos;" +
                    "Lnet/minecraft/block/BlockState;I)Z"))
    private void bukkit_fadeEvent(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
        if (BukkitEventFactory.callBlockFadeEvent(world, pos, this.deadCoralBlock.getDefaultState()
                        .with(CoralFanBlock.WATERLOGGED, false))
                .isCancelled()) { ci.cancel(); }
    }

}
