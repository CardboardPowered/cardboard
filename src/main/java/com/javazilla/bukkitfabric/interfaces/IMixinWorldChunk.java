package com.javazilla.bukkitfabric.interfaces;

import java.util.Map;

import org.bukkit.Chunk;

import net.minecraft.entity.Entity;
import net.minecraft.util.collection.TypeFilterableList;
import net.minecraft.world.Heightmap;

public interface IMixinWorldChunk {

    public Map<Heightmap.Type, Heightmap> getHeightMaps();

    public TypeFilterableList<Entity>[] getEntitySections();

    public Chunk getBukkitChunk();

}