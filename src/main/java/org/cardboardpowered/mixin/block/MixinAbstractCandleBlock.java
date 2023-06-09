package org.cardboardpowered.mixin.block;

import com.javazilla.bukkitfabric.impl.BukkitEventFactory;
import net.minecraft.block.AbstractCandleBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;
import org.bukkit.event.block.BlockIgniteEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractCandleBlock.class)
public class MixinAbstractCandleBlock {

    @Inject(method = "onProjectileHit", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/block/AbstractCandleBlock;" +
                    "setLit(Lnet/minecraft/world/WorldAccess;" +
                    "Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Z)V"),
            cancellable = true)
    private void callBlockIgniteEvent(World world, BlockState state, BlockHitResult hit, ProjectileEntity projectile, CallbackInfo ci) {
        // CraftBukkit start
        if (BukkitEventFactory.callBlockIgniteEvent(world, hit.getBlockPos(), BlockIgniteEvent.IgniteCause.ARROW, projectile).isCancelled()) {
            ci.cancel();
        }
        // CraftBukkit end
    }
}
