package com.javazilla.bukkitfabric.mixin.world;

import java.util.Map;
import org.bukkit.Chunk;
import org.bukkit.craftbukkit.CraftChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.javazilla.bukkitfabric.interfaces.IMixinWorldChunk;

import net.minecraft.entity.Entity;
import net.minecraft.util.collection.TypeFilterableList;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.Heightmap.Type;
import net.minecraft.world.World;
import net.minecraft.world.biome.source.BiomeArray;
import net.minecraft.world.chunk.WorldChunk;

@Mixin(WorldChunk.class)
public class MixinWorldChunk implements IMixinWorldChunk {

    @Shadow
    @Final
    public Map<Heightmap.Type, Heightmap> heightmaps;

    @Shadow
    @Final
    public TypeFilterableList<Entity>[] entitySections;

    private Chunk bukkit;

    @Override
    public TypeFilterableList<Entity>[] getEntitySections() {
        return entitySections;
    }

    @Override
    public Map<Type, Heightmap> getHeightMaps() {
        return heightmaps;
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    public void setBukkitChunk(World world, ChunkPos pos, BiomeArray biomes, CallbackInfo ci) {
        this.bukkit = new CraftChunk((WorldChunk)(Object)this);
    }

    @Override
    public Chunk getBukkitChunk() {
        return bukkit;
    }

}
