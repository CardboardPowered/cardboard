package org.cardboardpowered.mixin.block;

import com.javazilla.bukkitfabric.impl.BukkitEventFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.CocoaBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.cardboardpowered.util.MixinInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@MixinInfo(events = {"BlockGrowEvent"})
@Mixin (CocoaBlock.class)
public class MixinCocoaBlock {

    @Redirect (method = "randomTick", at = @At (value = "INVOKE",
            target = "Lnet/minecraft/server/world/ServerWorld;setBlockState" +
                    "(Lnet/minecraft/util/math/BlockPos;" +
                    "Lnet/minecraft/block/BlockState;I)Z"))
    private boolean bukkit_grow0(ServerWorld world, BlockPos pos, BlockState state, int flags) {
        return BukkitEventFactory.handleBlockGrowEvent(world, pos, state, flags);
    }

    @Redirect (method = "grow", at = @At (value = "INVOKE",
            target = "Lnet/minecraft/server/world/ServerWorld;setBlockState" +
                    "(Lnet/minecraft/util/math/BlockPos;" +
                    "Lnet/minecraft/block/BlockState;I)Z"))
    private boolean bukkit_grow1(ServerWorld world, BlockPos pos, BlockState state, int flags) {
        return BukkitEventFactory.handleBlockGrowEvent(world, pos, state, flags);
    }
}
