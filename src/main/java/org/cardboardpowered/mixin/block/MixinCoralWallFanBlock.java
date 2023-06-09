package org.cardboardpowered.mixin.block;

import com.javazilla.bukkitfabric.impl.BukkitEventFactory;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CoralWallFanBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.cardboardpowered.util.MixinInfo;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.Random;

@MixinInfo(events = {"BlockFadeEvent"})
@Mixin (CoralWallFanBlock.class)
public class MixinCoralWallFanBlock {

    @Shadow @Final private Block deadCoralBlock;

    @Inject (method = "scheduledTick", at = @At (value = "INVOKE",
            target = "Lnet/minecraft/server/world/ServerWorld;setBlockState" +
                    "(Lnet/minecraft/util/math/BlockPos;" +
                    "Lnet/minecraft/block/BlockState;I)Z"))
    private void bukkit_fadeEvent(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
        if (BukkitEventFactory.callBlockFadeEvent(world, pos, this.deadCoralBlock.getDefaultState()
                        .with(CoralWallFanBlock.WATERLOGGED, false)
                        .with(CoralWallFanBlock.FACING, state
                                .get(CoralWallFanBlock.FACING)))
                .isCancelled()) { ci.cancel(); }
    }
}
