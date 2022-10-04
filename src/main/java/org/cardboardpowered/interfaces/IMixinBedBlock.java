package org.cardboardpowered.interfaces;

import net.minecraft.block.BlockState;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IMixinBedBlock {

    ActionResult explodeBed(BlockState iblockdata, World world, BlockPos blockposition);

}
