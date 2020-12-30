package com.javazilla.bukkitfabric.interfaces;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import net.minecraft.server.world.ChunkHolder;

public interface IMixinThreadedAnvilChunkStorage {

    Long2ObjectLinkedOpenHashMap<ChunkHolder> getChunkHoldersBF();

}