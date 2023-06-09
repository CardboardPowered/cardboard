package org.cardboardpowered.mixin.block;

import com.javazilla.bukkitfabric.impl.BukkitEventFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.BuddingAmethystBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.atomic.AtomicReference;

@Mixin(BuddingAmethystBlock.class)
public class MixinBuddingAmethystBlock {

    private AtomicReference<BlockPos> fromPos = new AtomicReference<>();

    @Inject(method = "randomTick", at = @At("HEAD"))
    private void getFromPos(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
        fromPos.set(pos);
    }
    @Redirect(method = "randomTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)Z"))
    private boolean blockSpread(ServerWorld instance, BlockPos blockPos, BlockState blockState) {
        return BukkitEventFactory.handleBlockSpreadEvent(instance,fromPos.get(), blockPos, blockState, 3);
    }
}
