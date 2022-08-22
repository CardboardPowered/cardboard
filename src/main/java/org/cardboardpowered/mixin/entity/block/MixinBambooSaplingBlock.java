package org.cardboardpowered.mixin.entity.block;

import com.javazilla.bukkitfabric.impl.BukkitEventFactory;
import net.minecraft.block.BambooSaplingBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BambooSaplingBlock.class)
public class MixinBambooSaplingBlock {

    @Redirect(method = "grow(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z"))
    public boolean bukkitSpreadEvent(World world, BlockPos pos, BlockState newState, int flags) {
        return BukkitEventFactory.handleBlockSpreadEvent(world, pos.down(), pos, newState, flags);
    }
}
