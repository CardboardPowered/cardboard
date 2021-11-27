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

import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

import org.bukkit.craftbukkit.CraftServer;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.world.BiomeColorCache;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.world.Heightmap;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldProperties;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.light.LightingProvider;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.tick.QueryableTickScheduler;

public class FakeWorldAccess implements WorldAccess {

    public static final WorldAccess INSTANCE = new FakeWorldAccess();

    protected FakeWorldAccess() {
    }

    @Override
    public QueryableTickScheduler<Block> getBlockTickScheduler() {
        return null;//TODO
    }

    @Override
    public QueryableTickScheduler<Fluid> getFluidTickScheduler() {
        return null;//TODO
    }

    @Override
    public WorldProperties getLevelProperties() {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public LocalDifficulty getLocalDifficulty(BlockPos blockposition) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public ChunkManager getChunkManager() {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public Random getRandom() {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void playSound(PlayerEntity entityhuman, BlockPos blockposition, SoundEvent soundeffect, SoundCategory soundcategory, float f, float f1) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void addParticle(ParticleEffect particleparam, double d0, double d1, double d2, double d3, double d4, double d5) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void syncWorldEvent(PlayerEntity entityhuman, int i, BlockPos blockposition, int j) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public DynamicRegistryManager getRegistryManager() {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public List<Entity> getOtherEntities(Entity entity, Box aabb, Predicate<? super Entity> prdct) {
        throw new UnsupportedOperationException("Not supported");
    }

   //@Override
   // public <T extends Entity> List<T> getEntitiesByClass(Class<? extends T> type, Box aabb, Predicate<? super T> prdct) {
   //     throw new UnsupportedOperationException("Not supported yet.");
   // }

    @Override
    public List<? extends PlayerEntity> getPlayers() {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public Chunk getChunk(int i, int i1, ChunkStatus cs, boolean bln) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public int getTopY(Heightmap.Type type, int i, int i1) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public int getAmbientDarkness() {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public BiomeAccess getBiomeAccess() {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public Biome getGeneratorStoredBiome(int i, int i1, int i2) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public boolean isClient() {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public int getSeaLevel() {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public DimensionType getDimension() {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public LightingProvider getLightingProvider() {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public BlockEntity getBlockEntity(BlockPos blockposition) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public BlockState getBlockState(BlockPos blockposition) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public FluidState getFluidState(BlockPos blockposition) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public WorldBorder getWorldBorder() {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public boolean testBlockState(BlockPos bp, Predicate<BlockState> prdct) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public boolean setBlockState(BlockPos blockposition, BlockState iblockdata, int i, int j) {
        return false;
    }

    @Override
    public boolean removeBlock(BlockPos blockposition, boolean flag) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public boolean breakBlock(BlockPos blockposition, boolean flag, Entity entity, int i) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public float getBrightness(Direction arg0, boolean arg1) {
        return 0;
    }

    @Override
    public <T extends Entity> List<T> getEntitiesByType(TypeFilter<Entity, T> filter, Box box,
            Predicate<? super T> predicate) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean testFluidState(BlockPos pos, Predicate<FluidState> state) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void emitGameEvent(Entity arg0, GameEvent arg1, BlockPos arg2) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public MinecraftServer getServer() {
        // TODO Auto-generated method stub
        return CraftServer.server;
    }

    // TODO
    public long getTickOrder() {
        // TODO Auto-generated method stub
        return 0;
    }

}