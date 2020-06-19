package com.fungus_soft.bukkitfabric.mixin;

import java.util.Map;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.fungus_soft.bukkitfabric.interfaces.IMixinWorldChunk;

import net.minecraft.entity.Entity;
import net.minecraft.util.TypeFilterableList;
import net.minecraft.world.Heightmap;
import net.minecraft.world.Heightmap.Type;
import net.minecraft.world.chunk.WorldChunk;

@Mixin(WorldChunk.class)
public class MixinWorldChunk implements IMixinWorldChunk {

    @Shadow
    @Final
    public Map<Heightmap.Type, Heightmap> heightmaps;

    @Shadow
    @Final
    public TypeFilterableList<Entity>[] entitySections;

    @Override
    public TypeFilterableList<Entity>[] getEntitySections() {
        return entitySections;
    }

    @Override
    public Map<Type, Heightmap> getHeightMaps() {
        return heightmaps;
    }

}
