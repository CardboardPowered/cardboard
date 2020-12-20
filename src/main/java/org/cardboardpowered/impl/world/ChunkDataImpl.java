/**
 * CardboardPowered - Bukkit/Spigot for Fabric
 * Copyright (C) CardboardPowered.org and contributors
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either 
 * version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.cardboardpowered.impl.world;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.ChunkSection;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.material.MaterialData;

@SuppressWarnings("deprecation")
public final class ChunkDataImpl implements ChunkGenerator.ChunkData {

    private final int maxHeight;
    private final ChunkSection[] sections;
    private Set<BlockPos> tiles;

    public ChunkDataImpl(World world) {
        this(world.getMaxHeight());
    }

    ChunkDataImpl(int maxHeight) {
        if (maxHeight > 256) throw new IllegalArgumentException("World height exceeded max chunk height");
        this.maxHeight = maxHeight;
        sections = new ChunkSection[maxHeight >> 4];
    }

    @Override
    public int getMaxHeight() {
        return maxHeight;
    }

    @Override
    public void setBlock(int x, int y, int z, Material material) {
        setBlock(x, y, z, material.createBlockData());
    }

    @Override
    public void setBlock(int x, int y, int z, MaterialData material) {
        setBlock(x, y, z, CraftMagicNumbers.getBlock(material));
    }

    @Override
    public void setBlock(int x, int y, int z, BlockData blockData) {
        setBlock(x, y, z, ((CraftBlockData) blockData).getState());
    }

    @Override
    public void setRegion(int xMin, int yMin, int zMin, int xMax, int yMax, int zMax, Material material) {
        setRegion(xMin, yMin, zMin, xMax, yMax, zMax, material.createBlockData());
    }

    @Override
    public void setRegion(int xMin, int yMin, int zMin, int xMax, int yMax, int zMax, MaterialData material) {
        setRegion(xMin, yMin, zMin, xMax, yMax, zMax, CraftMagicNumbers.getBlock(material));
    }

    @Override
    public void setRegion(int xMin, int yMin, int zMin, int xMax, int yMax, int zMax, BlockData blockData) {
        setRegion(xMin, yMin, zMin, xMax, yMax, zMax, ((CraftBlockData) blockData).getState());
    }

    @Override
    public Material getType(int x, int y, int z) {
        return CraftMagicNumbers.getMaterial(getTypeId(x, y, z).getBlock());
    }

    @Override
    public MaterialData getTypeAndData(int x, int y, int z) {
        return CraftMagicNumbers.getMaterial(getTypeId(x, y, z));
    }

    @Override
    public BlockData getBlockData(int x, int y, int z) {
        return CraftBlockData.fromData(getTypeId(x, y, z));
    }

    public void setRegion(int xMin, int yMin, int zMin, int xMax, int yMax, int zMax, BlockState type) {
        // Clamp to sane values.
        if (xMin > 0xf || yMin >= maxHeight || zMin > 0xf) return;
        if (xMin < 0) xMin = 0;
        if (yMin < 0) yMin = 0;
        if (zMin < 0) zMin = 0;
        if (xMax > 0x10) xMax = 0x10;
        if (yMax > maxHeight) yMax = maxHeight;
        if (zMax > 0x10) zMax = 0x10;
        if (xMin >= xMax || yMin >= yMax || zMin >= zMax) return;

        for (int y = yMin; y < yMax; y++) {
            ChunkSection section = getChunkSection(y, true);
            int offsetBase = y & 0xf;
            for (int x = xMin; x < xMax; x++)
                for (int z = zMin; z < zMax; z++) section.setBlockState(x, offsetBase, z, type);
        }
    }

    public BlockState getTypeId(int x, int y, int z) {
        if (x != (x & 0xf) || y < 0 || y >= maxHeight || z != (z & 0xf)) return Blocks.AIR.getDefaultState();
        ChunkSection section = getChunkSection(y, false);
        return section == null ? Blocks.AIR.getDefaultState() : section.getBlockState(x, y & 0xf, z);
    }

    @Override
    public byte getData(int x, int y, int z) {
        return CraftMagicNumbers.toLegacyData(getTypeId(x, y, z));
    }

    private void setBlock(int x, int y, int z, BlockState type) {
        if (x != (x & 0xf) || y < 0 || y >= maxHeight || z != (z & 0xf)) return;
        ChunkSection section = getChunkSection(y, true);
        section.setBlockState(x, y & 0xf, z, type);

        if (type.getBlock().hasBlockEntity()) {
            if (tiles == null) tiles = new HashSet<>();
            tiles.add(new BlockPos(x, y, z));
        }
    }

    private ChunkSection getChunkSection(int y, boolean create) {
        ChunkSection section = sections[y >> 4];
        if (create && section == null) sections[y >> 4] = section = new ChunkSection(y >> 4 << 4);
        return section;
    }

    public ChunkSection[] getRawChunkData() {
        return sections;
    }

    public Set<BlockPos> getTiles() {
        return tiles;
    }

}