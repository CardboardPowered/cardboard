package com.javazilla.bukkitfabric.mixin.world;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.RaycastContext;

@Mixin(BlockView.class)
public interface MixinBlockView<T> {

    @Shadow
    public BlockState getBlockState(BlockPos var1);

    @Shadow
    public FluidState getFluidState(BlockPos var1);

    default BlockHitResult raycastBlock(RaycastContext raytrace1, BlockPos blockposition) {
        BlockState iblockdata = this.getBlockState(blockposition);
        FluidState fluid = this.getFluidState(blockposition);
        Vec3d vec3d = raytrace1.getStart();
        Vec3d vec3d1 = raytrace1.getEnd();
        VoxelShape voxelshape = raytrace1.getBlockShape(iblockdata, (BlockView)(Object)this, blockposition);
        BlockHitResult movingobjectpositionblock = this.raycastBlock(vec3d, vec3d1, blockposition, voxelshape, iblockdata);
        VoxelShape voxelshape1 = raytrace1.getFluidShape(fluid, (BlockView)(Object)this, blockposition);
        BlockHitResult movingobjectpositionblock1 = voxelshape1.raycast(vec3d, vec3d1, blockposition);
        double d0 = movingobjectpositionblock == null ? Double.MAX_VALUE : raytrace1.getStart().squaredDistanceTo(movingobjectpositionblock.getPos());
        double d1 = movingobjectpositionblock1 == null ? Double.MAX_VALUE : raytrace1.getStart().squaredDistanceTo(movingobjectpositionblock1.getPos());
        return d0 <= d1 ? movingobjectpositionblock : movingobjectpositionblock1;
    }

    @Shadow
    default public BlockHitResult raycastBlock(Vec3d start, Vec3d end, BlockPos pos, VoxelShape shape, BlockState state) {return null;}

}
