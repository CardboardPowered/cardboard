/**
 * Cardboard - Spigot/Paper for Fabric
 * Copyright (C) 2020-2022
 */
package org.cardboardpowered.interfaces;

import org.bukkit.Chunk;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public interface IWorldChunk {

    //Map<Heightmap.Type, Heightmap> getHeightMaps();

    //TypeFilterableList<Entity>[] getEntitySections();

    Chunk getBukkitChunk();

	public default BlockState setBlockState(BlockPos blockposition, BlockState iblockdata, boolean moved, boolean doPlace) {
		return null;
	}

}