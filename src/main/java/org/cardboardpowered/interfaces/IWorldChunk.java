/**
 * Cardboard - Spigot/Paper for Fabric
 * Copyright (C) 2020-2022
 */
package org.cardboardpowered.interfaces;

import org.bukkit.Chunk;

public interface IWorldChunk {

    //Map<Heightmap.Type, Heightmap> getHeightMaps();

    //TypeFilterableList<Entity>[] getEntitySections();

    Chunk getBukkitChunk();

}