package org.cardboardpowered.mixin.block;

import com.javazilla.bukkitfabric.impl.BukkitEventFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LilyPadBlock;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LilyPadBlock.class)
public class MixinLilyPadBlock {

    @Inject(method = "onEntityCollision", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;breakBlock(Lnet/minecraft/util/math/BlockPos;ZLnet/minecraft/entity/Entity;)Z"))
    public void entityChangeBlock(BlockState state, World world, BlockPos pos, Entity entity, CallbackInfo ci) {
        if (BukkitEventFactory.callEntityChangeBlockEvent(entity, pos, Blocks.AIR.getDefaultState()).isCancelled()) {
            ci.cancel();
        }
    }
}
