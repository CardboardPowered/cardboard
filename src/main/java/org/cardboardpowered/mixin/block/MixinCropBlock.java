package org.cardboardpowered.mixin.block;

import com.javazilla.bukkitfabric.impl.BukkitEventFactory;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropBlock;
import net.minecraft.block.PlantBlock;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.cardboardpowered.util.MixinInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@MixinInfo(events = {"BlockGrowEvent, EntityChangeBlockEvent"})
@Mixin (CropBlock.class)
public abstract class MixinCropBlock extends PlantBlock {

    protected MixinCropBlock(AbstractBlock.Settings settings) {
        super(settings);
    }

    @Redirect (method = "randomTick", at = @At (value = "INVOKE",
            target = "Lnet/minecraft/server/world/ServerWorld;setBlockState" +
                    "(Lnet/minecraft/util/math/BlockPos;" +
                    "Lnet/minecraft/block/BlockState;I)Z"))
    private boolean bukkit_growEvent0(ServerWorld world, BlockPos pos, BlockState state, int flags) {
        return BukkitEventFactory.handleBlockGrowEvent(world, pos, state, flags);
    }

    @Redirect (method = "applyGrowth", at = @At (value = "INVOKE",
            target = "Lnet/minecraft/world/World;setBlockState" +
                    "(Lnet/minecraft/util/math/BlockPos;" +
                    "Lnet/minecraft/block/BlockState;I)Z"))
    private boolean bukkit_growEvent1(World world, BlockPos pos, BlockState state, int flags) {
        return BukkitEventFactory.handleBlockGrowEvent(world, pos, state, flags);
    }

    @Redirect (method = "onEntityCollision", at = @At (value = "INVOKE",
            target = "Lnet/minecraft/world/GameRules;getBoolean(Lnet/minecraft/world/GameRules$Key;)Z"))
    private boolean bukkit_ifHack(GameRules instance, GameRules.Key<GameRules.BooleanRule> rule) {
        return true;
        /*mumfrey pls allow us to add conditions to ifs*/
    }

    @Inject (method = "onEntityCollision", at = @At (value = "INVOKE",
            target = "Lnet/minecraft/world/World;breakBlock" +
                    "(Lnet/minecraft/util/math/BlockPos;" +
                    "ZLnet/minecraft/entity/Entity;)Z"))
    private void bukkit_entityChangeBlockEvent(BlockState state, World world, BlockPos pos, Entity entity,
                                               CallbackInfo ci) {
        if (BukkitEventFactory
                .callEntityChangeBlockEvent(entity, pos, Blocks.AIR.getDefaultState(), !world.getGameRules()
                        .getBoolean(GameRules.DO_MOB_GRIEFING))
                .isCancelled()) {
            super.onEntityCollision(state, world, pos, entity);
            ci.cancel();
        }
    }
}
