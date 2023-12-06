package io.papermc.paper.util;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.MathHelper;

public final class CoordinateUtils {
    static final int SECTION_X_BITS = 22;
    static final long SECTION_X_MASK = 0x3FFFFFL;
    static final int SECTION_Y_BITS = 20;
    static final long SECTION_Y_MASK = 1048575L;
    static final int SECTION_Z_BITS = 22;
    static final long SECTION_Z_MASK = 0x3FFFFFL;
    static final int SECTION_Y_SHIFT = 0;
    static final int SECTION_Z_SHIFT = 20;
    static final int SECTION_X_SHIFT = 42;
    static final int SECTION_TO_BLOCK_SHIFT = 4;

    public static int getNeighbourMappedIndex(int dx, int dz, int radius) {
        return dx + radius + (2 * radius + 1) * (dz + radius);
    }

    public static long getChunkKey(BlockPos pos) {
        return (long)(pos.getZ() >> 4) << 32 | (long)(pos.getX() >> 4) & 0xFFFFFFFFL;
    }

    public static long getChunkKey(Entity entity) {
        return MathHelper.lfloor(entity.getZ()) >> 4 << 32 | MathHelper.lfloor(entity.getX()) >> 4 & 0xFFFFFFFFL;
    }

    public static long getChunkKey(ChunkPos pos) {
        return (long)pos.z << 32 | (long)pos.x & 0xFFFFFFFFL;
    }

    public static long getChunkKey(ChunkSectionPos pos) {
        return (long)pos.getZ() << 32 | (long)pos.getX() & 0xFFFFFFFFL;
    }

    public static long getChunkKey(int x2, int z2) {
        return (long)z2 << 32 | (long)x2 & 0xFFFFFFFFL;
    }

    public static int getChunkX(long chunkKey) {
        return (int)chunkKey;
    }

    public static int getChunkZ(long chunkKey) {
        return (int)(chunkKey >>> 32);
    }

    public static int getChunkCoordinate(double blockCoordinate) {
        return MathHelper.floor(blockCoordinate) >> 4;
    }

    public static long getChunkSectionKey(int x2, int y2, int z2) {
        return ((long)x2 & 0x3FFFFFL) << 42 | ((long)y2 & 0xFFFFFL) << 0 | ((long)z2 & 0x3FFFFFL) << 20;
    }

    public static long getChunkSectionKey(ChunkSectionPos pos) {
        return ((long)pos.getX() & 0x3FFFFFL) << 42 | ((long)pos.getY() & 0xFFFFFL) << 0 | ((long)pos.getZ() & 0x3FFFFFL) << 20;
    }

    public static long getChunkSectionKey(ChunkPos pos, int y2) {
        return ((long)pos.x & 0x3FFFFFL) << 42 | ((long)y2 & 0xFFFFFL) << 0 | ((long)pos.z & 0x3FFFFFL) << 20;
    }

    public static long getChunkSectionKey(BlockPos pos) {
        return (long)pos.getX() << 38 & 0xFFFFFC0000000000L | (long)(pos.getY() >> 4) & 0xFFFFFL | (long)pos.getZ() << 16 & 0x3FFFFF00000L;
    }

    public static long getChunkSectionKey(Entity entity) {
        return MathHelper.lfloor(entity.getX()) << 38 & 0xFFFFFC0000000000L | MathHelper.lfloor(entity.getY()) >> 4 & 0xFFFFFL | MathHelper.lfloor(entity.getZ()) << 16 & 0x3FFFFF00000L;
    }

    public static int getChunkSectionX(long key) {
        return (int)(key << 0 >> 42);
    }

    public static int getChunkSectionY(long key) {
        return (int)(key << 44 >> 44);
    }

    public static int getChunkSectionZ(long key) {
        return (int)(key << 22 >> 42);
    }

    public static int getBlockCoordinate(double blockCoordinate) {
        return MathHelper.floor(blockCoordinate);
    }

    public static long getBlockKey(int x2, int y2, int z2) {
        return (long)x2 & 0x7FFFFFFL | ((long)z2 & 0x7FFFFFFL) << 27 | (long)y2 << 54;
    }

    public static long getBlockKey(BlockPos pos) {
        return (long)pos.getX() & 0x7FFFFFFL | ((long)pos.getZ() & 0x7FFFFFFL) << 27 | (long)pos.getY() << 54;
    }

    public static long getBlockKey(Entity entity) {
        return (long)entity.getX() & 0x7FFFFFFL | ((long)entity.getZ() & 0x7FFFFFFL) << 27 | (long)entity.getY() << 54;
    }

    private CoordinateUtils() {
        throw new RuntimeException();
    }
}

