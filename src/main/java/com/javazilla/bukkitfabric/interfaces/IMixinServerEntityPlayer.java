package com.javazilla.bukkitfabric.interfaces;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IMixinServerEntityPlayer extends IMixinEntity {

	public void reset();

	public BlockPos getSpawnPoint(World world);

}