package org.cardboardpowered.mixin.block;

import com.javazilla.bukkitfabric.impl.BukkitEventFactory;
import net.minecraft.block.BambooBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.enums.BambooLeaves;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

// import java.util.Random;

@Mixin(BambooBlock.class)
public class MixinBambooBlock extends Block {

    @Shadow @Final public static EnumProperty<BambooLeaves> LEAVES;
    @Shadow @Final public static IntProperty AGE;

    @Shadow @Final public static IntProperty STAGE;

    public MixinBambooBlock(Settings settings) {
        super(settings);
    }

    @Redirect(method = "grow", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;get(Lnet/minecraft/state/property/Property;)Ljava/lang/Comparable;"))
    private <T extends Comparable<T>> T bukkitSkipIfCancel(BlockState state, Property<T> property) {
        if (!state.isOf(Blocks.BAMBOO)) {
            return (T) Integer.valueOf(1);
        } else {
            return state.get(property);
        }
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public void updateLeaves(BlockState state, World world, BlockPos pos, Random random, int height) {
        BlockState blockState = world.getBlockState(pos.down());
        BlockPos blockPos = pos.down(2);
        BlockState blockState2 = world.getBlockState(blockPos);
        BambooLeaves bambooLeaves = BambooLeaves.NONE;
        boolean shouldUpdateOthers = false;
        if (height >= 1) {
            if (blockState.isOf(Blocks.BAMBOO) && blockState.get(LEAVES) != BambooLeaves.NONE) {
                if (blockState.isOf(Blocks.BAMBOO) && blockState.get(LEAVES) != BambooLeaves.NONE) {
                    bambooLeaves = BambooLeaves.LARGE;
                    if (blockState2.isOf(Blocks.BAMBOO)) {
                        shouldUpdateOthers = true;
                    }
                }
            } else {
                bambooLeaves = BambooLeaves.SMALL;
            }
        }
        int i = (Integer)state.get(AGE) != 1 && !blockState2.isOf(Blocks.BAMBOO) ? 0 : 1;
        int j = (height < 11 || !(random.nextFloat() < 0.25F)) && height != 15 ? 0 : 1;
        if (BukkitEventFactory.handleBlockSpreadEvent(world, pos, pos.up(), this.getDefaultState().with(AGE, i).with(LEAVES, bambooLeaves).with(STAGE, j), 3)) {
            if (shouldUpdateOthers) {
                world.setBlockState(pos.down(), blockState.with(BambooBlock.LEAVES, BambooLeaves.SMALL), 3);
                world.setBlockState(blockPos, blockState2.with(BambooBlock.LEAVES, BambooLeaves.NONE), 3);
            }
        }
    }
}
