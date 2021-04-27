package org.cardboardpowered.mixin.world;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.javazilla.bukkitfabric.interfaces.IMixinThreadedAnvilChunkStorage;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;

@Mixin(ThreadedAnvilChunkStorage.class)
public class MixinThreadedAnvilChunkStorage implements IMixinThreadedAnvilChunkStorage {

    @Shadow
    public Long2ObjectLinkedOpenHashMap<ChunkHolder> chunkHolders;

    @Override
    public Long2ObjectLinkedOpenHashMap<ChunkHolder> getChunkHoldersBF() {
        return chunkHolders;
    }

}
