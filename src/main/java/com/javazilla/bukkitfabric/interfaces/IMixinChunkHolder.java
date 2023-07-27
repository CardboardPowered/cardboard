package com.javazilla.bukkitfabric.interfaces;

import java.util.concurrent.CompletableFuture;

import com.mojang.datafixers.util.Either;

import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ChunkLevelType;
import net.minecraft.server.world.ChunkLevels;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;

public interface IMixinChunkHolder {

    // CraftBukkit start
    /*static WorldChunk getFullChunk(ChunkHolder holder) {
        if (!ChunkHolder.getLevelType(holder.lastTickLevel).isAfter(ChunkHolder.LevelType.BORDER)) return null; // note: using oldTicketLevel for isLoaded checks
        CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>> statusFuture = holder.getFutureFor(ChunkStatus.FULL);
        Either<Chunk, ChunkHolder.Unloaded> either = statusFuture.getNow(null);
        return either == null ? null : (WorldChunk) either.left().orElse(null);
    }*/
    // CraftBukkit end

    
    static WorldChunk getFullChunkNow(ChunkHolder holder) {
    	 if (!ChunkLevels.getType(holder.lastTickLevel).isAfter(ChunkLevelType.FULL)) {
             return null; // note: using oldTicketLevel for isLoaded checks
         }
         return getFullChunkNowUnchecked(holder);
    }

    static WorldChunk getFullChunkNowUnchecked(ChunkHolder holder) {
    	CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>> statusFuture = holder.getFutureFor(ChunkStatus.FULL);
        Either<Chunk, ChunkHolder.Unloaded> either = statusFuture.getNow(null);
        return (either == null) ? null : (WorldChunk) either.left().orElse(null);
    }

}