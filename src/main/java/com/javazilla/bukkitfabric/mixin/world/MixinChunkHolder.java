package com.javazilla.bukkitfabric.mixin.world;

import java.util.concurrent.CompletableFuture;

import org.spongepowered.asm.mixin.Mixin;

import com.javazilla.bukkitfabric.interfaces.IMixinChunkHolder;
import com.mojang.datafixers.util.Either;

import net.minecraft.server.world.ChunkHolder;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;

@Mixin(ChunkHolder.class)
public class MixinChunkHolder implements IMixinChunkHolder {


}