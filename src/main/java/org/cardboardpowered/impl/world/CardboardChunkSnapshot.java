package org.cardboardpowered.impl.world;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicates;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.chunk.PalettedContainer;
import java.lang.reflect.Field;
import java.util.function.Predicate;

import org.bukkit.ChunkSnapshot;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;

public class CardboardChunkSnapshot implements ChunkSnapshot {

    private final int x, z;
    private final String worldname;
    private final PalettedContainer<BlockState>[] blockids;
    private final byte[][] skylight;
    private final byte[][] emitlight;
    private final boolean[] empty;
    private final Heightmap hmap;
    private final long captureFulltime;
    private final BiomeAccess.Storage biome;

    public CardboardChunkSnapshot(int x, int z, String wname, long wtime, PalettedContainer<BlockState>[] sectionBlockIDs, byte[][] sectionSkyLights,
            byte[][] sectionEmitLights, boolean[] sectionEmpty, Heightmap hmap, BiomeAccess.Storage biome) {
        this.x = x;
        this.z = z;
        this.worldname = wname;
        this.captureFulltime = wtime;
        this.blockids = sectionBlockIDs;
        this.skylight = sectionSkyLights;
        this.emitlight = sectionEmitLights;
        this.empty = sectionEmpty;
        this.hmap = hmap;
        this.biome = biome;
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getZ() {
        return z;
    }

    @Override
    public String getWorldName() {
        return worldname;
    }

    @Override
    public boolean contains(BlockData block) {
        Preconditions.checkArgument(block != null, "Block must not be null");
        Predicate<BlockState> nms = Predicates.equalTo(((CraftBlockData) block).getState());
        for (PalettedContainer<BlockState> palette : blockids) if (palette.hasAny(nms)) return true;
        return false;
    }

    @Override
    public Material getBlockType(int x, int y, int z) {
        CardboardChunk.validateChunkCoordinates(x, y, z);
        return CraftMagicNumbers.getMaterial(blockids[y >> 4].get(x, y & 0xF, z).getBlock());
    }

    @Override
    public final BlockData getBlockData(int x, int y, int z) {
        CardboardChunk.validateChunkCoordinates(x, y, z);
        return CraftBlockData.fromData(blockids[y >> 4].get(x, y & 0xF, z));
    }

    @Override
    public final int getData(int x, int y, int z) {
        CardboardChunk.validateChunkCoordinates(x, y, z);
        return CraftMagicNumbers.toLegacyData(blockids[y >> 4].get(x, y & 0xF, z));
    }

    @Override
    public final int getBlockSkyLight(int x, int y, int z) {
        CardboardChunk.validateChunkCoordinates(x, y, z);
        return (skylight[y >> 4][(((y & 0xF) << 7) | (z << 3) | (x >> 1))] >> ((x & 1) << 2)) & 0xF;
    }

    @Override
    public final int getBlockEmittedLight(int x, int y, int z) {
        CardboardChunk.validateChunkCoordinates(x, y, z);
        return (emitlight[y >> 4][(((y & 0xF) << 7) | (z << 3) | (x >> 1))] >> ((x & 1) << 2)) & 0xF;
    }

    @Override
    public final int getHighestBlockYAt(int x, int z) {
        Preconditions.checkState(hmap != null, "ChunkSnapshot created without height map. Please call getSnapshot with includeMaxblocky=true");
        CardboardChunk.validateChunkCoordinates(x, 0, z);
        return hmap.get(x, z);
    }

    @Override
    public final Biome getBiome(int x, int z) {
        return getBiome(x, 0, z);
    }

    @SuppressWarnings("unchecked")
    @Override
    public final Biome getBiome(int x, int y, int z) {
        Preconditions.checkState(biome != null, "ChunkSnapshot created without biome. Please call getSnapshot with includeBiome=true");
        CardboardChunk.validateChunkCoordinates(x, y, z);

        // Access Widener is broken
        Registry<net.minecraft.world.biome.Biome> reg = null;
        try {
            Field f = biome.getClass().getDeclaredField("field_25831");
            f.setAccessible(true);
            reg = (Registry<net.minecraft.world.biome.Biome>) f.get(biome);
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {e.printStackTrace();}

        return CraftBlock.biomeBaseToBiome(reg, biome.getBiomeForNoiseGen(x >> 2, y >> 2, z >> 2));
    }


    @Override
    public final double getRawBiomeTemperature(int x, int z) {
        return getRawBiomeTemperature(x, 0, z);
    }

    @Override
    public final double getRawBiomeTemperature(int x, int y, int z) {
        Preconditions.checkState(biome != null, "ChunkSnapshot created without biome. Please call getSnapshot with includeBiome=true");
        CardboardChunk.validateChunkCoordinates(x, y, z);
        return biome.getBiomeForNoiseGen(x >> 2, y >> 2, z >> 2).getTemperature(new BlockPos((this.x << 4) | x, y, (this.z << 4) | z));
    }

    @Override
    public final long getCaptureFullTime() {
        return captureFulltime;
    }

    @Override
    public final boolean isSectionEmpty(int sy) {
        return empty[sy];
    }

}