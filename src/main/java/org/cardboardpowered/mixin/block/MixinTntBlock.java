package org.cardboardpowered.mixin.block;

import com.javazilla.bukkitfabric.impl.BukkitEventFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.TntBlock;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;
import org.cardboardpowered.util.MixinInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@MixinInfo(events = {"EntityChangeBlockEvent"})
@Mixin (TntBlock.class)
public class MixinTntBlock {

    @Inject (method = "onProjectileHit", at = @At (value = "INVOKE",
            target = "Lnet/minecraft/block/TntBlock;primeTnt" +
                    "(Lnet/minecraft/world/World;" +
                    "Lnet/minecraft/util/math/BlockPos;" +
                    "Lnet/minecraft/entity/LivingEntity;)V"),
            cancellable = true)
    private void bukkit_entityChangeBlockEvent(World world, BlockState state, BlockHitResult hit, ProjectileEntity projectile, CallbackInfo ci) {
        if (BukkitEventFactory.callEntityChangeBlockEvent(projectile, hit.getBlockPos(), Blocks.AIR.getDefaultState())
                .isCancelled()) { ci.cancel(); }
    }
}
