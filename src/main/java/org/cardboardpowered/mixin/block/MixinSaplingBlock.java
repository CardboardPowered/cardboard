package org.cardboardpowered.mixin.block;

import com.javazilla.bukkitfabric.interfaces.IMixinWorld;
import net.minecraft.block.BlockState;
import net.minecraft.block.SaplingBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.TreeType;
import org.bukkit.event.world.StructureGrowEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Random;

@Mixin(SaplingBlock.class)
public abstract class MixinSaplingBlock {

    private static TreeType bukkitTreeType;

    @Shadow public abstract void grow(ServerWorld world, Random random, BlockPos pos, BlockState state);

    @Inject(method = "generate", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/sapling/SaplingGenerator;generate(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/world/gen/chunk/ChunkGenerator;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Ljava/util/Random;)Z"))
    public void bukkitTreeGrow(ServerWorld world, BlockPos pos, BlockState state, Random random, CallbackInfo ci) {
        TreeType treeType =  bukkitTreeType;
        Location location = new Location(((IMixinWorld) world).getWorldImpl(), pos.getX(), pos.getY(), pos.getZ());
        List<org.bukkit.block.BlockState> blocks = (List<org.bukkit.block.BlockState>) ((IMixinWorld) world).getCapturedBlockStates_BF();
        StructureGrowEvent event = new StructureGrowEvent(location, treeType, false, null, (List<org.bukkit.block.BlockState>) blocks);
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            for (org.bukkit.block.BlockState blockstate : blocks) {
                blockstate.update(true);
            }
        }
    }
}
